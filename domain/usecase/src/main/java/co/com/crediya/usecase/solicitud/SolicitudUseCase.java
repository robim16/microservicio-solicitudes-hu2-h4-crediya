package co.com.crediya.usecase.solicitud;

import co.com.crediya.model.estados.gateways.EstadosRepository;
import co.com.crediya.model.notificacion.Notificacion;
import co.com.crediya.model.notificacion.gateways.NotificacionRepository;
import co.com.crediya.model.prestamos.Prestamos;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.solicitud.vo.SolicitudConDetalles;
import co.com.crediya.model.tipo_prestamo.gateways.TipoPrestamoRepository;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.model.usuario.security.TokenService;
import co.com.crediya.usecase.solicitud.exceptions.ClientNotFoundException;
import co.com.crediya.usecase.solicitud.exceptions.ErrorFilterException;
import co.com.crediya.usecase.solicitud.exceptions.InvalidUserException;
import co.com.crediya.usecase.solicitud.exceptions.TipoPrestamoNotFoundException;
import co.com.crediya.usecase.solicitud.validators.SolicitudValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;


@RequiredArgsConstructor
public class SolicitudUseCase implements ISolicitudUseCase {
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final NotificacionRepository notificacionRepository;
    private final EstadosRepository estadosRepository;
    private final TokenService tokenService;


    @Override
    public Mono<Solicitud> registrarSolicitud(Solicitud solicitud, String token) {
        return SolicitudValidator.validate(solicitud)
                .flatMap(validSolicitud ->
                        Mono.fromCallable(() -> tokenService.validateToken(token))
                                .flatMap(authenticatedUser -> {
                                    String email = authenticatedUser.getEmail();

                                    if (!email.equals(validSolicitud.getEmail())) {
                                        return Mono.error(new InvalidUserException(
                                                "No se puede crear una solicitud a otro usuario"));
                                    }
                                    validSolicitud.setEmail(email);

                                    return usuarioRepository.getUsuarioByEmail(email)
                                            .switchIfEmpty(Mono.error(new ClientNotFoundException("Cliente no encontrado")))
                                            .flatMap(usuario ->
                                                    tipoPrestamoRepository.getTipoPrestamoById(validSolicitud.getIdTipoPrestamo())
                                                            .switchIfEmpty(Mono.error(new TipoPrestamoNotFoundException("Tipo de préstamo no encontrado")))
                                                            .flatMap(tipoPrestamo ->
                                                                    solicitudRepository.registrarSolicitud(validSolicitud)
                                                                            .flatMap(solicitudNueva -> {
                                                                                if (tipoPrestamo.getValidacionAutomatica()) {
                                                                                    return this.prestamosActivos(solicitudNueva)
                                                                                            .then(Mono.just(solicitudNueva))
                                                                                            .doOnNext(s -> {
                                                                                                System.out.println("Solicitud validada automáticamente: " + s.getId());
                                                                                            })
                                                                                            .onErrorResume(ex -> Mono.error(
                                                                                                    new RuntimeException(
                                                                                                            "Error en validación automática para solicitud " + solicitudNueva.getId(), ex
                                                                                                    )
                                                                                            ));
                                                                                }
                                                                                return Mono.just(solicitudNueva)
                                                                                        .doOnNext(s -> {
                                                                                            System.out.println("Solicitud registrada sin validación automática: " + s.getId());
                                                                                        });
                                                                            })
                                                            )
                                            );
                                })
                );
    }


    @Override
    public Flux<SolicitudConDetalles> filtrarSolicitud(
            String estado, String email, String plazo, String tipoPrestamo, int page, int size
    ) {
        int offset = page * size;

        return solicitudRepository.filtrarSolicitud(estado, email, plazo, tipoPrestamo, size, offset)
                .map(solicitud -> {

                    return new SolicitudConDetalles(
                            solicitud.getId(),
                            solicitud.getMonto(),
                            solicitud.getPlazo(),
                            solicitud.getEmail(),
                            solicitud.getIdEstado(),
                            solicitud.getIdTipoPrestamo(),
                            solicitud.getTasaInteres(),
                            solicitud.getCuotaMensual()
                    );
                })
                .onErrorMap(e -> new ErrorFilterException("Error filtrando solicitudes"));
    }


    @Override
    public Mono<Long> contarSolicitudes(String estado, String email, String plazo, String tipoPrestamo) {
        return solicitudRepository.contarSolicitudes(estado, email, plazo, tipoPrestamo);
    }


    @Override
    public Mono<Solicitud> editarEstado(BigInteger id, BigInteger nuevoEstado) {
        return solicitudRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Solicitud no encontrada con id " + id)))
                .flatMap(solicitud -> {
                    solicitud.setIdEstado(nuevoEstado);
                    return solicitudRepository.updateStatus(solicitud);
                })
                .flatMap(solicitudActualizada ->
                        estadosRepository.findById(solicitudActualizada.getIdEstado())
                                .flatMap(estadoSolicitud -> {
                                    Notificacion notification = Notificacion.builder()
                                            .type("SOLICITUD_ACTUALIZADA")
                                            .payload("Solicitud " + solicitudActualizada.getId() +
                                                    " actualizada a estado " + estadoSolicitud.getNombre())
                                            .destino("SQS")
                                            .build();

                                    return notificacionRepository.enviar(notification, "solicitudes-queue")
                                            .thenReturn(solicitudActualizada);
                                })
                );
    }

    @Override
    public Mono<Void> prestamosActivos(Solicitud solicitudNueva) {
        return solicitudRepository.prestamosActivos()
                .flatMap(prestamo ->
                        usuarioRepository.getUsuarioByEmail(prestamo.getEmail())
                                .switchIfEmpty(Mono.error(new ClientNotFoundException("Cliente no encontrado")))
                                .map(usuario -> {
                                    prestamo.setSalarioBase(usuario.getSalarioBase());
                                    return prestamo;
                                })
                )
                .collectList()
                .flatMap(prestamos -> {
                    String payload = prestamos.stream()
                            .map(Prestamos::toJson)
                            .collect(java.util.stream.Collectors.joining(",", "[", "]"));

                    Notificacion notification = Notificacion.builder()
                            .type("PRESTAMOS_ACTIVOS")
                            .payload(payload)
                            .destino("SQS")
                            .build();

                    return notificacionRepository.enviar(notification, "capacidad-endeudamiento")
                            .doOnNext(n -> {
                                System.out.println("Array de prestamos enviado a cola: total=" + prestamos.size());
                            })
                            .then();
                })
                .onErrorResume(e -> {
                    System.out.println("Error en prestamosActivos: " + e.getMessage());
                    return Mono.empty();
                });
    }

}

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
import co.com.crediya.usecase.validacionautomatica.ValidacionAutomaticaUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
                                                                            .doOnNext(s -> System.out.println("Solicitud registrada en DB con id: " + s.getId()))
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
                .onErrorMap(e -> new ErrorFilterException("Error filtrando solicitudes" +e.getMessage()));
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
                                            .payload(
                                                    "{\"idSolicitud\":" + solicitudActualizada.getId() +
                                                            ",\"estado\":\"" + estadoSolicitud.getNombre() + "\"}"
                                            )
                                            .destino("SQS")
                                            .build();

                                    return notificacionRepository.enviar(notification, "solicitudes-queue")
                                            .thenReturn(solicitudActualizada);
                                })
                ).
                onErrorResume(e ->
                        Mono.error(new RuntimeException("Error al editar la solicitud " + e.getMessage())));
    }

}

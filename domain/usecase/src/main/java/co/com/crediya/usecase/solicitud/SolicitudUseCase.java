package co.com.crediya.usecase.solicitud;

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


@RequiredArgsConstructor
public class SolicitudUseCase implements ISolicitudUseCase {
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
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

                                    return usuarioRepository.getUsuarioByEmail(email
                                            )
                                            .switchIfEmpty(Mono.error(new ClientNotFoundException("Cliente no encontrado")))
                                            .flatMap(usuario ->
                                                    tipoPrestamoRepository.getTipoPrestamoById(validSolicitud.getIdTipoPrestamo())
                                                            .switchIfEmpty(Mono.error(new TipoPrestamoNotFoundException("Tipo de prestamo no encontrado")))
                                                            .flatMap(tipoPrestamo ->
                                                                    solicitudRepository.registrarSolicitud(validSolicitud)
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

}

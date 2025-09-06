package co.com.crediya.usecase.solicitud;

import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.tipo_prestamo.gateways.TipoPrestamoRepository;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.model.usuario.security.AuthenticatedUser;
import co.com.crediya.model.usuario.security.TokenService;
import co.com.crediya.usecase.solicitud.exceptions.InvalidSolicitudException;
import co.com.crediya.usecase.solicitud.validators.SolicitudValidator;
import lombok.RequiredArgsConstructor;
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
                                        return Mono.error(new RuntimeException(
                                                "No se puede crear una solicitud a otro usuario"));
                                    }
                                    validSolicitud.setEmail(email);

                                    return usuarioRepository.getUsuarioByEmail(email, token)
                                            .switchIfEmpty(Mono.error(new RuntimeException("Cliente no encontrado")))
                                            .flatMap(usuario ->
                                                    tipoPrestamoRepository.getTipoPrestamoById(validSolicitud.getIdTipoPrestamo())
                                                            .switchIfEmpty(Mono.error(new RuntimeException("Tipo de préstamo no encontrado")))
                                                            .flatMap(tipoPrestamo ->
                                                                    solicitudRepository.registrarSolicitud(validSolicitud)
                                                            )
                                            );
                                })
                );
    }

}

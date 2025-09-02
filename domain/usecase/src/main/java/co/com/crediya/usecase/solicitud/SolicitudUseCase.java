package co.com.crediya.usecase.solicitud;

import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.tipo_prestamo.gateways.TipoPrestamoRepository;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.usecase.solicitud.validators.SolicitudValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SolicitudUseCase implements ISolicitudUseCase {
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;


    public Mono<Solicitud> registrarSolicitud(Solicitud solicitud) {
        return SolicitudValidator.validate(solicitud)
                .flatMap(validSolicitud ->
                        usuarioRepository.getUsuarioByEmail(validSolicitud.getEmail())
                                .switchIfEmpty(Mono.error(new RuntimeException("Cliente no encontrado")))
                                .flatMap(usuario ->
                                        tipoPrestamoRepository.getTipoPrestamoById(validSolicitud.getIdTipoPrestamo())
                                                .switchIfEmpty(Mono.error(new RuntimeException("Tipo de préstamo no encontrado")))
                                                .flatMap(tipoPrestamo ->
                                                        solicitudRepository.registrarSolicitud(validSolicitud)
                                                )
                                )
                );
    }

}

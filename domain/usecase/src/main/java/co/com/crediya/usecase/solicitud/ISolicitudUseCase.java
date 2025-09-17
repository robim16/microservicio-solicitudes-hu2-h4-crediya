package co.com.crediya.usecase.solicitud;

import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.vo.SolicitudConDetalles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ISolicitudUseCase {
    Mono<Solicitud> registrarSolicitud(Solicitud solicitud, String token);
    Flux<SolicitudConDetalles> filtrarSolicitud(String estado, String email, String plazo, String tipoPrestamo, int page, int size);
    Mono<Long> contarSolicitudes(String estado, String email, String plazo, String tipoPrestamo);
}

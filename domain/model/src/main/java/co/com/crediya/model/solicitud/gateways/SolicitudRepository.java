package co.com.crediya.model.solicitud.gateways;

import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.vo.SolicitudConDetalles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface SolicitudRepository {
    Mono<Solicitud> registrarSolicitud(Solicitud solicitud);
    Flux<SolicitudConDetalles> filtrarSolicitud(String estado, String email, String plazo, String tipoPrestamo, int limit, int offset);
    Mono<Long> contarSolicitudes(String estado, String email, String plazo, String tipoPrestamo);
}

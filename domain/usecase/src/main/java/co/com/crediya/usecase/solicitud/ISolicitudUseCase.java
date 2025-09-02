package co.com.crediya.usecase.solicitud;

import co.com.crediya.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;

public interface ISolicitudUseCase {
    public Mono<Solicitud> registrarSolicitud(Solicitud solicitud);
}

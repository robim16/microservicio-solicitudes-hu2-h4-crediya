package co.com.crediya.model.solicitud.gateways;

import co.com.crediya.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

public interface SolicitudRepository {
    Mono<Solicitud> registrarSolicitud(Solicitud solicitud);
}

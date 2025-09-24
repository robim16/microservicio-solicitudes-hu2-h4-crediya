package co.com.crediya.usecase.validacionautomatica;

import co.com.crediya.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;

public interface IValidacionAutomaticaUseCase {
    Mono<Void> buscarPrestamosActivos(Solicitud solicitudNueva);
}

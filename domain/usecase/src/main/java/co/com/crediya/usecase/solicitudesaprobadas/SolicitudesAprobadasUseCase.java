package co.com.crediya.usecase.solicitudesaprobadas;

import co.com.crediya.model.notificacion.Notificacion;
import co.com.crediya.model.notificacion.gateways.NotificacionRepository;
import co.com.crediya.model.solicitud.Solicitud;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SolicitudesAprobadasUseCase {
    private final NotificacionRepository notificacionRepository;
    public Mono<Void> notificarSolicitudAprobada(Solicitud solicitud) {
        return Mono.fromSupplier(() -> {
                    Solicitud payload = solicitud;
                    return Notificacion.builder()
                            .type("PRESTAMO_APROBADO")
                            .payload(payload)
                            .destino("SQS")
                            .build();
                })
                .flatMap(notification -> notificacionRepository.enviar(notification, "solicitud-aprobada-queue"))
                .doOnNext(n -> System.out.println("Payload: " + solicitud))
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Error en envio a la cola solicitud-aprobada-queue", ex
                )))
                .then();

    }
}

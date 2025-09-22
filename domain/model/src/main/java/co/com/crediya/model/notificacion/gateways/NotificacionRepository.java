package co.com.crediya.model.notificacion.gateways;

import co.com.crediya.model.notificacion.Notificacion;
import reactor.core.publisher.Mono;

public interface NotificacionRepository {
    Mono<Notificacion> enviar(Notificacion notification, String queue);
}

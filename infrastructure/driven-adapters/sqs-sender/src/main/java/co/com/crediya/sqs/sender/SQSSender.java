package co.com.crediya.sqs.sender;

import co.com.crediya.model.notificacion.Notificacion;
import co.com.crediya.model.notificacion.gateways.NotificacionRepository;
import co.com.crediya.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements NotificacionRepository {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;

    /*public Mono<String> send(Notificacion notificacion) {
        return Mono.fromCallable(() -> buildRequest(notificacion))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }*/

    private SendMessageRequest buildRequest(Notificacion notificacion, String queue) {
        String url = queue == "solicitudes-queue" ? properties.queueUrl() : properties.queueUrl2();
        return SendMessageRequest.builder()
                //.queueUrl(properties.queueUrl())
                .queueUrl(url)
                .messageBody(notificacion.getPayload())
                .build();
    }


    @Override
    public Mono<Notificacion> enviar(Notificacion notificacion, String queue) {
        return Mono.fromCallable(() -> buildRequest(notificacion, queue))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .map(response -> {
                    log.info("Mensaje enviado a SQS con ID={}", response.messageId());
                    return notificacion.toBuilder()
                            .id(response.messageId())
                            .build();
                })
                .doOnError(e -> log.error("Error al enviar mensaje a SQS: {}", e.getMessage(), e))
                .onErrorResume(e -> {
                    return Mono.just(
                            notificacion.toBuilder()
                                    .id("ERROR:" + e.getClass().getSimpleName())
                                    .build()
                    );
                });
    }

}
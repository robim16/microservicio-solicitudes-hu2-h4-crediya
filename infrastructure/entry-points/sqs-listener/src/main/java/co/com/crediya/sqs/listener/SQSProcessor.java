package co.com.crediya.sqs.listener;

import co.com.crediya.usecase.solicitud.SolicitudUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    private final SolicitudUseCase solicitudUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> apply(Message message) {
        return Mono.fromCallable(() -> message.body())
                .flatMap(body -> {
                    try {
                        Map<String, Object> payload = objectMapper.readValue(body, Map.class);

                        Integer idSolicitud = (Integer) payload.get("idSolicitud");
                        Integer estado = (Integer) payload.get("estado");

                        System.out.println("Mensaje recibido de SQS:");
                        System.out.println("idSolicitud: " + idSolicitud);
                        System.out.println("estado: " + estado);

                        return solicitudUseCase.editarEstado(
                                BigInteger.valueOf(idSolicitud.longValue()),
                                BigInteger.valueOf(estado.longValue())
                        ).then();

                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Error procesando mensaje SQS", e));
                    }
                });
    }

}

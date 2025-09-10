package co.com.crediya.api.exception;

import co.com.crediya.usecase.solicitud.exceptions.InvalidSolicitudException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Configuration
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> body;

        if (ex instanceof InvalidSolicitudException invalidEx) {
            status = HttpStatus.BAD_REQUEST;
            body = Map.of(
                    "error", "Validación fallida",
                    "detalles", invalidEx.getErrors()
            );
            log.warn("Error de validación en la petición {} {} -> {}",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI(),
                    invalidEx.getErrors(), ex);
        } else {
            body = Map.of(
                    "error", "Error interno",
                    "detalle", "Ha ocurrido un error inesperado. Inténtalo más tarde."
            );
            log.error("Error inesperado en la petición {} {}",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI(), ex);
        }


        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

        try {
            byte[] bytes = mapper.writeValueAsBytes(body);
            return exchange.getResponse().writeWith(Mono.just(bufferFactory.wrap(bytes)));
        } catch (Exception writeEx) {
            log.error("No se pudo escribir la respuesta de error", writeEx);
            return exchange.getResponse().setComplete();
        }
    }
}

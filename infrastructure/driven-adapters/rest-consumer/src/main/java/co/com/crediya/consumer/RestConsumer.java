package co.com.crediya.consumer;

import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RestConsumer implements UsuarioRepository {
    private final WebClient client;
    private static final Logger log = LoggerFactory.getLogger(RestConsumer.class);

    @Override
    @CircuitBreaker(name = "getClientByEmail", fallbackMethod = "fallbackGetUsuarioByEmail")
    public Mono<Usuario> getUsuarioByEmail(String email, String token) {

        return client
                .get()
                .uri("/{email}", email)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Usuario.class);
    }

    private Mono<Usuario> fallbackGetUsuarioByEmail(String email, Throwable ex) {
        log.error("Fallback disparado por getUsuarioByEmail com email={} - causa: {}", email, ex.getMessage());
        return Mono.empty();
    }
}

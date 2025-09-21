package co.com.crediya.model.estados.gateways;

import co.com.crediya.model.estados.Estados;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

public interface EstadosRepository {
    Mono<Estados> findById(BigInteger id);
}

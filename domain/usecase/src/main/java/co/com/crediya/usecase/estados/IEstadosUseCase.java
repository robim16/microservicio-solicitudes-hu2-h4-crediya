package co.com.crediya.usecase.estados;

import co.com.crediya.model.estados.Estados;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

public interface IEstadosUseCase {
    Mono<Estados> findById(BigInteger id);
}

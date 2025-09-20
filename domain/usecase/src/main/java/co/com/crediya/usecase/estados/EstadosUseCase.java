package co.com.crediya.usecase.estados;

import co.com.crediya.model.estados.Estados;
import co.com.crediya.model.estados.gateways.EstadosRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

@RequiredArgsConstructor
public class EstadosUseCase implements IEstadosUseCase {
    private final EstadosRepository estadosRepository;
    @Override
    public Mono<Estados> findById(BigInteger id) {
        return estadosRepository.findById(id);
    }
}

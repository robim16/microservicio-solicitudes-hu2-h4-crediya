package co.com.crediya.usecase.tipoprestamo;

import co.com.crediya.model.tipo_prestamo.TipoPrestamo;
import co.com.crediya.model.tipo_prestamo.gateways.TipoPrestamoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

@RequiredArgsConstructor
public class TipoPrestamoUseCase {
    private final TipoPrestamoRepository tipoPrestamoRepository;
    public Mono<TipoPrestamo> getTipoPrestamoById(BigInteger id) {
        return tipoPrestamoRepository.getTipoPrestamoById(id);
    };
}

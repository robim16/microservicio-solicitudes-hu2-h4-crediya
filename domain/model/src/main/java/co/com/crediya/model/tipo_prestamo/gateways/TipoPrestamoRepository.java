package co.com.crediya.model.tipo_prestamo.gateways;

import co.com.crediya.model.tipo_prestamo.TipoPrestamo;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

public interface TipoPrestamoRepository {
    Mono<TipoPrestamo> getTipoPrestamoById(BigInteger id);
}

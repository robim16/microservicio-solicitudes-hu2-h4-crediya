package co.com.crediya.r2dbc;

import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.solicitud.vo.SolicitudConDetalles;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.r2dbc.entity.SolicitudEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.BigInteger;

@Repository
public class SolicitudReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Solicitud,
        SolicitudEntity,
        BigInteger,
        SolicitudReactiveRepository
> implements SolicitudRepository {
    private final TransactionalOperator transactionalOperator;
    private final DatabaseClient databaseClient;

    //private final UsuarioRepository usuarioRepository;
    private static final Logger log = LoggerFactory.getLogger(SolicitudReactiveRepositoryAdapter.class);
    public SolicitudReactiveRepositoryAdapter(SolicitudReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator, DatabaseClient databaseClient) {
        super(repository, mapper, d -> mapper.map(d, Solicitud.class));
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<Solicitud> registrarSolicitud(Solicitud solicitud) {
        return Mono.fromCallable(() -> mapper.map(solicitud, SolicitudEntity.class))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(entity -> {
                    entity.setIdEstado(BigInteger.valueOf(1));
                    return repository.save(entity)
                            .doOnSuccess(saved -> log.info("Se ha guardado la solicitud"));
                })
                .map(saved -> mapper.map(saved, Solicitud.class))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<SolicitudConDetalles> filtrarSolicitud(
            String estado, String email, String plazo, String tipoPrestamo, int limit, int offset
    ) {
        StringBuilder sql = new StringBuilder("""
                    SELECT\s
                                         s.id,
                                         s.monto,
                                         s.plazo,
                                         s.email,
                                         s.id_estado,
                                         s.id_tipo_prestamo,
                                         t.tasa_interes,
                                          (
                                             s.monto * (
                                                 ( (t.tasa_interes/12) * POWER(1 + (t.tasa_interes/12), CAST(s.plazo AS NUMERIC(20,3))) )
                                                 /
                                                 (POWER(1 + (t.tasa_interes/12), CAST(s.plazo AS NUMERIC(20,3))) - 1)
                                             )
                                         )::numeric(12,2) AS cuota_mensual
                                     FROM solicitudes s
                                     JOIN tipo_prestamos t ON s.id_tipo_prestamo = t.id WHERE 1 = 1 
                """);

        if (estado != null) sql.append("AND s.id_estado = :estado ");
        if (email != null) sql.append("AND s.email = :email ");
        if (plazo != null) sql.append("AND s.plazo = :plazo ");
        if (tipoPrestamo != null) sql.append("AND tp.nombre = :tipoPrestamo ");

        sql.append("ORDER BY s.id DESC LIMIT :limit OFFSET :offset");

        DatabaseClient.GenericExecuteSpec query = databaseClient.sql(sql.toString());

        if (estado != null) query = query.bind("estado", estado);
        if (email != null) query = query.bind("email", email);
        if (plazo != null) query = query.bind("plazo", plazo);
        if (tipoPrestamo != null) query = query.bind("tipoPrestamo", tipoPrestamo);

        query = query.bind("limit", limit).bind("offset", offset);

        return query.map(this::mapRow).all();
    }


    @Override
    public Mono<Long> contarSolicitudes(String estado, String email, String plazo, String tipoPrestamo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) as total FROM solicitudes WHERE 1=1 ");

        if (estado != null) sql.append("AND id_estado = :estado ");
        if (email != null) sql.append("AND email = :email ");
        if (plazo != null) sql.append("AND plazo = :plazo ");
        if (tipoPrestamo != null) sql.append("AND id_tipo_prestamo = :tipoPrestamo ");

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());

        if (estado != null) spec = spec.bind("estado", new BigInteger(estado));
        if (email != null) spec = spec.bind("email", email);
        if (plazo != null) spec = spec.bind("plazo", plazo);
        if (tipoPrestamo != null) spec = spec.bind("tipoPrestamo", tipoPrestamo);

        return spec.map((row, meta) -> row.get("total", Long.class)).one();
    }

    private SolicitudConDetalles mapRow(Row row, RowMetadata meta) {
        return new SolicitudConDetalles(
                row.get("id", BigInteger.class),
                row.get("monto", Long.class),
                row.get("plazo", String.class),
                row.get("email", String.class),
                row.get("id_estado", BigInteger.class),
                row.get("id_tipo_prestamo", BigInteger.class),
                row.get("tasa_interes", BigDecimal.class),
                row.get("cuota_mensual", BigDecimal.class)
        );
    }
}



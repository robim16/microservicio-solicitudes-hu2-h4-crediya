package co.com.crediya.r2dbc;

import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.r2dbc.entity.SolicitudEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigInteger;

@Repository
public class SolicitudReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Solicitud,
        SolicitudEntity,
        BigInteger,
        SolicitudReactiveRepository
> implements SolicitudRepository {
    private final TransactionalOperator transactionalOperator;
    private static final Logger log = LoggerFactory.getLogger(SolicitudReactiveRepositoryAdapter.class);
    public SolicitudReactiveRepositoryAdapter(SolicitudReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, d -> mapper.map(d, Solicitud.class));
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
}

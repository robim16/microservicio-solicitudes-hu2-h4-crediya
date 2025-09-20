package co.com.crediya.r2dbc;

import co.com.crediya.model.estados.Estados;
import co.com.crediya.model.estados.gateways.EstadosRepository;
import co.com.crediya.r2dbc.entity.EstadosEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public class EstadosReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Estados,
        EstadosEntity,
        BigInteger,
        EstadosReactiveRepository
> implements EstadosRepository {
    public EstadosReactiveRepositoryAdapter(EstadosReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Estados.class));
    }
}

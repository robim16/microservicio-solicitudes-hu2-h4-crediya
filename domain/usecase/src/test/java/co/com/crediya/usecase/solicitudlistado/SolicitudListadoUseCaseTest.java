package co.com.crediya.usecase.solicitudlistado;

import co.com.crediya.model.solicitud.vo.SolicitudConDetalles;
import co.com.crediya.model.solicitudconusuario.SolicitudConUsuario;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.usecase.solicitud.SolicitudUseCase;
import co.com.crediya.usecase.user.UserUseCase;
import co.com.crediya.usecase.solicitudlistado.SolicitudListadoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.mockito.Mockito.when;

class SolicitudListadoUseCaseTest {

    @Mock
    private SolicitudUseCase solicitudUseCase;

    @Mock
    private UserUseCase userUseCase;

    @InjectMocks
    private SolicitudListadoUseCase solicitudListadoUseCase;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listarSolicitudes_devuelveSolicitudesConUsuario() {
        var solicitudConDetalles = new SolicitudConDetalles(
                BigInteger.ONE,
                10000L,
                "12",
                "test@correo.com",
                BigInteger.TWO,
                BigInteger.TWO,
                BigDecimal.valueOf(5.5),
                BigDecimal.valueOf(900)
        );

        var usuario = new Usuario(
                BigInteger.ONE,
                "Test User",
                "Arteaga",
                "test@correo.com",
                1L
        );

        when(solicitudUseCase.filtrarSolicitud("A", "test@correo.com", "12", "personal", 0, 10))
                .thenReturn(Flux.just(solicitudConDetalles));

        when(userUseCase.getUsuarioByEmail("test@correo.com"))
                .thenReturn(Mono.just(usuario));

        when(solicitudUseCase.contarSolicitudes("A", "test@correo.com", "12", "personal"))
                .thenReturn(Mono.just(1L));

        Mono<Tuple2<List<SolicitudConUsuario>, Long>> result =
                solicitudListadoUseCase.listarSolicitudes("A", "test@correo.com", "12", "personal", 0, 10);

        StepVerifier.create(result)
                .assertNext(tuple -> {
                    List<SolicitudConUsuario> solicitudes = tuple.getT1();
                    Long total = tuple.getT2();

                    assert total == 1L;
                    assert solicitudes.size() == 1;

                    SolicitudConUsuario sc = solicitudes.get(0);
                    assert sc.getSolicitud().getEmail().equals("test@correo.com");
                    assert sc.getUsuario().getNombre().equals("Test User");
                })
                .verifyComplete();
    }

    @Test
    void listarSolicitudes_sinResultados() {
        when(solicitudUseCase.filtrarSolicitud(null, null, null, null, 0, 10))
                .thenReturn(Flux.empty());

        when(solicitudUseCase.contarSolicitudes(null, null, null, null))
                .thenReturn(Mono.just(0L));

        Mono<Tuple2<List<SolicitudConUsuario>, Long>> result =
                solicitudListadoUseCase.listarSolicitudes(null, null, null, null, 0, 10);

        StepVerifier.create(result)
                .assertNext(tuple -> {
                    assert tuple.getT1().isEmpty();
                    assert tuple.getT2() == 0L;
                })
                .verifyComplete();
    }

    @Test
    void listarSolicitudes_errorEnUserUseCase() {
        var solicitudConDetalles = new SolicitudConDetalles(
                BigInteger.ONE,
               10000L,
                "12",
                "error@correo.com",
                BigInteger.TWO,
                BigInteger.TWO,
                BigDecimal.valueOf(5.5),
                BigDecimal.valueOf(900)
        );

        when(solicitudUseCase.filtrarSolicitud(null, null, null, null, 0, 10))
                .thenReturn(Flux.just(solicitudConDetalles));

        when(userUseCase.getUsuarioByEmail("error@correo.com"))
                .thenReturn(Mono.error(new RuntimeException("Fallo en userUseCase")));

        when(solicitudUseCase.contarSolicitudes(null, null, null, null))
                .thenReturn(Mono.just(1L));

        Mono<Tuple2<List<SolicitudConUsuario>, Long>> result =
                solicitudListadoUseCase.listarSolicitudes(null, null, null, null, 0, 10);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Fallo en userUseCase"))
                .verify();
    }
}

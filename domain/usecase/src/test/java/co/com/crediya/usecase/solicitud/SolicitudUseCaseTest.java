package co.com.crediya.usecase.solicitud;

import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.vo.SolicitudConDetalles;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.tipo_prestamo.TipoPrestamo;
import co.com.crediya.model.tipo_prestamo.gateways.TipoPrestamoRepository;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.model.usuario.security.AuthenticatedUser;
import co.com.crediya.model.usuario.security.TokenService;
import co.com.crediya.usecase.solicitud.exceptions.ErrorFilterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SolicitudUseCaseTest {

    // Dependencias simuladas
    private SolicitudRepository solicitudRepository;
    private UsuarioRepository usuarioRepository;
    private TipoPrestamoRepository tipoPrestamoRepository;
    private TokenService tokenService;
    private SolicitudUseCase solicitudUseCase;

    @BeforeEach
    void setUp() {
        // Creamos mocks de cada dependencia
        solicitudRepository = Mockito.mock(SolicitudRepository.class);
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        tipoPrestamoRepository = Mockito.mock(TipoPrestamoRepository.class);
        tokenService = Mockito.mock(TokenService.class);

        // Inyectamos los mocks en el caso de uso
        solicitudUseCase = new SolicitudUseCase(
                solicitudRepository,
                usuarioRepository,
                tipoPrestamoRepository,
                tokenService
        );
    }

    @Test
    void registrarSolicitud_debeGuardarConEstadoPorDefecto() {

        String token = "token-valido";
        String email = "usuario@test.com";

        AuthenticatedUser authUser = new AuthenticatedUser(email, "ROLE_USER");
        when(tokenService.validateToken(token)).thenReturn(authUser);

        Usuario usuarioMock = Usuario.builder()
                .email(email)
                .nombre("Carlos")
                .apellidos("Arteaga")
                .salarioBase(2_000_000L)
                .build();
        when(usuarioRepository.getUsuarioByEmail(email, token))
                .thenReturn(Mono.just(usuarioMock));

        TipoPrestamo tipoPrestamoMock = TipoPrestamo.builder()
                .id(BigInteger.ONE)
                .nombre("Libre inversión")
                .build();

        when(tipoPrestamoRepository.getTipoPrestamoById(BigInteger.ONE))
                .thenReturn(Mono.just(tipoPrestamoMock));


        Solicitud solicitudEntrada = Solicitud.builder()
                .monto(10_000_000L)
                .plazo("12")
                .email(email)
                .idTipoPrestamo(BigInteger.ONE)
                .tasaInteres("0.05")
                .idEstado(BigInteger.ONE)
                .build();

        Solicitud solicitudGuardada = solicitudEntrada.toBuilder()
                .id(BigInteger.ONE)
                .build();

        when(solicitudRepository.registrarSolicitud(any(Solicitud.class)))
                .thenReturn(Mono.just(solicitudGuardada));

        Mono<Solicitud> resultado = solicitudUseCase.registrarSolicitud(solicitudEntrada, token);

        StepVerifier.create(resultado)
                .assertNext(sol -> {
                    assert sol.getId().equals(BigInteger.ONE);
                    assert sol.getIdEstado().equals(BigInteger.ONE);
                    assert sol.getEmail().equals(email);
                })
                .verifyComplete();

        verify(tokenService, times(1)).validateToken(token);
        verify(usuarioRepository, times(1)).getUsuarioByEmail(email, token);
        verify(solicitudRepository, times(1)).registrarSolicitud(any(Solicitud.class));
    }


    @Test
    void filtrarSolicitud_debeRetornarSolicitudesCorrectamente() {

        // Creamos objetos simulados que devolverá el repositorio
        SolicitudConDetalles solicitud1 = new SolicitudConDetalles(
                BigInteger.ONE,
                1000000L,
                "12",
                "usuario1@test.com",
                BigInteger.valueOf(1),
                BigInteger.valueOf(2),
                new BigDecimal("0.05"),
                new BigDecimal("85607.48")
        );

        SolicitudConDetalles solicitud2 = new SolicitudConDetalles(
                BigInteger.valueOf(2),
                2000000L,
                "24",
                "usuario2@test.com",
                BigInteger.valueOf(1),
                BigInteger.valueOf(2),
                new BigDecimal("0.03"),
                new BigDecimal("85956.23")
        );

        // Configuramos el mock del repositorio para que devuelva esas solicitudes
        when(solicitudRepository.filtrarSolicitud(
                any(), any(), any(), any(), anyInt(), anyInt()
        )).thenReturn(Flux.just(solicitud1, solicitud2));


        Flux<SolicitudConDetalles> resultado = solicitudUseCase.filtrarSolicitud(
                "1", null, null, null, 0, 10
        );

        // Verificamos que se reciben exactamente las 2 solicitudes simuladas
        StepVerifier.create(resultado)
                .expectNext(solicitud1)
                .expectNext(solicitud2)
                .verifyComplete();

        // Verificamos que el repositorio se llamó una vez con los parámetros esperados
        verify(solicitudRepository, times(1))
                .filtrarSolicitud("1", null, null, null, 10, 0);
    }

    @Test
    void filtrarSolicitud_debePropagarErrorComoErrorFilterException() {

        // Simulamos que el repositorio lanza un error
        when(solicitudRepository.filtrarSolicitud(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.error(new RuntimeException("DB error")));

        Flux<SolicitudConDetalles> resultado = solicitudUseCase.filtrarSolicitud(
                "1", null, null, null, 0, 10
        );


        // Validamos que el use case capture el error y lo envuelva en ErrorFilterException
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorFilterException &&
                                throwable.getMessage().contains("Error filtrando solicitudes")
                )
                .verify();

        // Verificamos que el repositorio se llamó una sola vez
        verify(solicitudRepository, times(1))
                .filtrarSolicitud("1", null, null, null, 10, 0);
    }
}

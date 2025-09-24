package co.com.crediya.usecase.solicitud;

import co.com.crediya.model.estados.Estados;
import co.com.crediya.model.estados.gateways.EstadosRepository;
import co.com.crediya.model.notificacion.Notificacion;
import co.com.crediya.model.notificacion.gateways.NotificacionRepository;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.vo.SolicitudConDetalles;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.tipo_prestamo.TipoPrestamo;
import co.com.crediya.model.tipo_prestamo.gateways.TipoPrestamoRepository;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.model.usuario.security.AuthenticatedUser;
import co.com.crediya.model.usuario.security.TokenService;
import co.com.crediya.usecase.solicitud.exceptions.ClientNotFoundException;
import co.com.crediya.usecase.solicitud.exceptions.ErrorFilterException;
import co.com.crediya.usecase.solicitud.exceptions.InvalidUserException;
import co.com.crediya.usecase.solicitud.exceptions.TipoPrestamoNotFoundException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SolicitudUseCaseTest {

    private SolicitudRepository solicitudRepository;
    private UsuarioRepository usuarioRepository;
    private TipoPrestamoRepository tipoPrestamoRepository;
    private NotificacionRepository notificacionRepository;
    private EstadosRepository estadosRepository;
    private TokenService tokenService;
    private SolicitudUseCase solicitudUseCase;
    private final BigInteger solicitudId = BigInteger.ONE;
    private final BigInteger nuevoEstado = BigInteger.TWO;


    @BeforeEach
    void setUp() {

        solicitudRepository = Mockito.mock(SolicitudRepository.class);
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        tipoPrestamoRepository = Mockito.mock(TipoPrestamoRepository.class);
        tokenService = Mockito.mock(TokenService.class);
        estadosRepository = Mockito.mock(EstadosRepository.class);
        notificacionRepository = Mockito.mock(NotificacionRepository.class);


        solicitudUseCase = new SolicitudUseCase(
                solicitudRepository,
                usuarioRepository,
                tipoPrestamoRepository,
                notificacionRepository,
                estadosRepository,
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
        when(usuarioRepository.getUsuarioByEmail(email))
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
        verify(usuarioRepository, times(1)).getUsuarioByEmail(email);
        verify(solicitudRepository, times(1)).registrarSolicitud(any(Solicitud.class));
    }


    @Test
    void registrarSolicitud_debeLanzarInvalidUserExceptionSiEmailNoCoincide() {

        String token = "valid-token";
        String emailToken = "user@test.com";
        String emailSolicitud = "otro@test.com";

        Solicitud solicitudEntrada = new Solicitud();
        solicitudEntrada.setMonto(1000000L);
        solicitudEntrada.setPlazo("12 meses");
        solicitudEntrada.setEmail(emailSolicitud);
        solicitudEntrada.setIdTipoPrestamo(BigInteger.ONE);

        when(tokenService.validateToken(token)).thenReturn(new AuthenticatedUser(emailToken, "ROLE_USER"));

        StepVerifier.create(solicitudUseCase.registrarSolicitud(solicitudEntrada, token))
                .expectErrorSatisfies(throwable -> {
                    assert throwable instanceof InvalidUserException;
                    assert throwable.getMessage().contains("No se puede crear una solicitud a otro usuario");
                })
                .verify();

        verifyNoInteractions(usuarioRepository, tipoPrestamoRepository, solicitudRepository);
    }


    @Test
    void registrarSolicitud_debeLanzarClientNotFoundExceptionCuandoUsuarioNoExiste() {

        String token = "token123";
        String email = "user@test.com";

        Solicitud solicitudEntrada = Solicitud.builder()
                .idTipoPrestamo(BigInteger.ONE)
                .email(email)
                .monto(1000000L)
                .plazo("12")
                .build();

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(email, "USER");

        when(tokenService.validateToken(token)).thenReturn(authenticatedUser);
        when(usuarioRepository.getUsuarioByEmail(email)).thenReturn(Mono.empty());

        Mono<Solicitud> resultado = solicitudUseCase.registrarSolicitud(solicitudEntrada, token);


        StepVerifier.create(resultado)
                .expectErrorMatches(ex -> ex instanceof ClientNotFoundException &&
                        ex.getMessage().contains("Cliente no encontrado"))
                .verify();

        verify(usuarioRepository, times(1)).getUsuarioByEmail(email);
        verifyNoInteractions(tipoPrestamoRepository, solicitudRepository);
    }

    /*@Test
    void registrarSolicitud_debeLanzarTipoPrestamoNotFoundExceptionCuandoTipoPrestamoNoExiste() {
        String token = "token123";
        String email = "user@test.com";

        Solicitud solicitudEntrada = Solicitud.builder()
                .idTipoPrestamo(BigInteger.TEN)
                .email(email)
                .monto(2000000L)
                .plazo("24")
                .build();

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(email, "USER");

        when(tokenService.validateToken(token)).thenReturn(authenticatedUser);
        when(usuarioRepository.getUsuarioByEmail(email))
                .thenReturn(Mono.just(Usuario.builder().email(email).build()));
        when(tipoPrestamoRepository.getTipoPrestamoById(BigInteger.TEN))
                .thenReturn(Mono.empty());

        Mono<Solicitud> resultado = solicitudUseCase.registrarSolicitud(solicitudEntrada, token);

        StepVerifier.create(resultado)
                .expectErrorMatches(ex -> ex instanceof TipoPrestamoNotFoundException &&
                        ex.getMessage().contains("Tipo de préstamo no encontrado"))
                .verify();

        verify(usuarioRepository, times(1)).getUsuarioByEmail(email);
        verify(tipoPrestamoRepository, times(1)).getTipoPrestamoById(BigInteger.TEN);
        verifyNoInteractions(solicitudRepository);
    }*/


    @Test
    void filtrarSolicitud_debeRetornarSolicitudesCorrectamente() {

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


        when(solicitudRepository.filtrarSolicitud(
                any(), any(), any(), any(), anyInt(), anyInt()
        )).thenReturn(Flux.just(solicitud1, solicitud2));


        Flux<SolicitudConDetalles> resultado = solicitudUseCase.filtrarSolicitud(
                "1", null, null, null, 0, 10
        );


        StepVerifier.create(resultado)
                .expectNext(solicitud1)
                .expectNext(solicitud2)
                .verifyComplete();

        verify(solicitudRepository, times(1))
                .filtrarSolicitud("1", null, null, null, 10, 0);
    }

    @Test
    void filtrarSolicitud_debePropagarErrorComoErrorFilterException() {

        when(solicitudRepository.filtrarSolicitud(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.error(new RuntimeException("DB error")));

        Flux<SolicitudConDetalles> resultado = solicitudUseCase.filtrarSolicitud(
                "1", null, null, null, 0, 10
        );

        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorFilterException &&
                                throwable.getMessage().contains("Error filtrando solicitudes")
                )
                .verify();

        verify(solicitudRepository, times(1))
                .filtrarSolicitud("1", null, null, null, 10, 0);
    }

    @Test
    void editarEstado_WhenSolicitudExists_ShouldUpdateAndNotify() {
        Solicitud solicitud = new Solicitud();
        solicitud.setId(solicitudId);
        solicitud.setIdEstado(BigInteger.ONE);

        Solicitud solicitudActualizada = new Solicitud();
        solicitudActualizada.setId(solicitudId);
        solicitudActualizada.setIdEstado(nuevoEstado);

        Estados estado = new Estados();
        estado.setId(nuevoEstado);
        estado.setNombre("APROBADA");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Mono.just(solicitud));
        when(solicitudRepository.updateStatus(any(Solicitud.class))).thenReturn(Mono.just(solicitudActualizada));
        when(estadosRepository.findById(nuevoEstado)).thenReturn(Mono.just(estado));
        when(notificacionRepository.enviar(any(Notificacion.class), anyString()))
                .thenReturn(Mono.just(new Notificacion()));

        StepVerifier.create(solicitudUseCase.editarEstado(solicitudId, nuevoEstado))
                .expectNextMatches(s -> s.getIdEstado().equals(nuevoEstado))
                .verifyComplete();

        verify(solicitudRepository).findById(solicitudId);
        verify(solicitudRepository).updateStatus(any(Solicitud.class));
        verify(estadosRepository).findById(nuevoEstado);

        ArgumentCaptor<Notificacion> notificacionCaptor = ArgumentCaptor.forClass(Notificacion.class);
        ArgumentCaptor<String> destinoCaptor = ArgumentCaptor.forClass(String.class);

        verify(notificacionRepository).enviar(notificacionCaptor.capture(), destinoCaptor.capture());

        Notificacion notificacionEnviada = notificacionCaptor.getValue();
        String destinoEnviado = destinoCaptor.getValue();

        assertNotNull(notificacionEnviada);
        assertEquals("SOLICITUD_ACTUALIZADA", notificacionEnviada.getType());
        assertEquals("SQS", notificacionEnviada.getDestino());
        assertEquals("solicitudes-queue", destinoEnviado);
    }



    @Test
    void editarEstado_WhenSolicitudDoesNotExist_ShouldReturnError() {
        when(solicitudRepository.findById(solicitudId)).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.editarEstado(solicitudId, nuevoEstado))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().contains("Solicitud no encontrada con id"))
                .verify();

        verify(solicitudRepository).findById(solicitudId);
        verifyNoMoreInteractions(solicitudRepository, estadosRepository, notificacionRepository);
    }

    @Test
    void editarEstado_WhenEstadoNotFound_ShouldReturnError() {
        Solicitud solicitud = new Solicitud();
        solicitud.setId(solicitudId);
        solicitud.setIdEstado(BigInteger.ONE);

        Solicitud solicitudActualizada = new Solicitud();
        solicitudActualizada.setId(solicitudId);
        solicitudActualizada.setIdEstado(nuevoEstado);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Mono.just(solicitud));
        when(solicitudRepository.updateStatus(any(Solicitud.class))).thenReturn(Mono.just(solicitudActualizada));
        when(estadosRepository.findById(nuevoEstado)).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.editarEstado(solicitudId, nuevoEstado))
                .verifyComplete();

        verify(solicitudRepository).findById(solicitudId);
        verify(solicitudRepository).updateStatus(any(Solicitud.class));
        verify(estadosRepository).findById(nuevoEstado);
        verifyNoInteractions(notificacionRepository);
    }
}

package co.com.crediya.usecase.validacionautomatica;

import co.com.crediya.model.estados.gateways.EstadosRepository;
import co.com.crediya.model.notificacion.Notificacion;
import co.com.crediya.model.notificacion.gateways.NotificacionRepository;
import co.com.crediya.model.prestamos.Prestamos;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.tipo_prestamo.TipoPrestamo;
import co.com.crediya.model.tipo_prestamo.gateways.TipoPrestamoRepository;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.model.usuario.security.TokenService;
import co.com.crediya.usecase.solicitud.SolicitudUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigInteger;

class ValidacionAutomaticaUseCaseTest {

    private SolicitudRepository solicitudRepository;
    private UsuarioRepository usuarioRepository;
    private TipoPrestamoRepository tipoPrestamoRepository;
    private NotificacionRepository notificacionRepository;
    private ValidacionAutomaticaUseCase validacionAutomaticaUseCase;


    @BeforeEach
    void setUp() {

        solicitudRepository = Mockito.mock(SolicitudRepository.class);
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        tipoPrestamoRepository = Mockito.mock(TipoPrestamoRepository.class);
        notificacionRepository = Mockito.mock(NotificacionRepository.class);


       validacionAutomaticaUseCase = new ValidacionAutomaticaUseCase(
                solicitudRepository,
                usuarioRepository,
                tipoPrestamoRepository,
                notificacionRepository
        );
    }

    @Test
    void buscarPrestamosActivos_debeEnviarNotificacionConPrestamosYSolicitud() {
        String email = "cliente@test.com";
        BigInteger tipoPrestamoId = BigInteger.ONE;

        Solicitud solicitud = Solicitud.builder()
                .id(BigInteger.valueOf(123))
                .email(email)
                .monto(5000000L)
                .plazo("24")
                .idTipoPrestamo(tipoPrestamoId)
                .build();

        Prestamos prestamo1 = Prestamos.builder()
                .id(BigInteger.valueOf(1))
                .email(email)
                .monto(1000000L)
                .plazo("12")
                .build();

        Usuario usuario = Usuario.builder()
                .email(email)
                .salarioBase(2000000L)
                .build();

        TipoPrestamo tipoPrestamo = new TipoPrestamo();
        tipoPrestamo.setId(tipoPrestamoId);
        tipoPrestamo.setTasaInteres(0.02f);


        when(solicitudRepository.prestamosActivos(email)).thenReturn(Flux.just(prestamo1));
        when(usuarioRepository.getUsuarioByEmail(email)).thenReturn(Mono.just(usuario));
        when(tipoPrestamoRepository.getTipoPrestamoById(tipoPrestamoId)).thenReturn(Mono.just(tipoPrestamo));
        when(notificacionRepository.enviar(any(Notificacion.class), eq("capacidad-endeudamiento")))
                .thenReturn(Mono.just(new Notificacion()));


        Mono<Void> resultado = validacionAutomaticaUseCase.buscarPrestamosActivos(solicitud);

        StepVerifier.create(resultado)
                .verifyComplete();


        verify(solicitudRepository).prestamosActivos(email);
        verify(usuarioRepository).getUsuarioByEmail(email);
        verify(tipoPrestamoRepository).getTipoPrestamoById(tipoPrestamoId);

        ArgumentCaptor<Notificacion> notificacionCaptor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).enviar(notificacionCaptor.capture(), eq("capacidad-endeudamiento"));

        Notificacion notificacionEnviada = notificacionCaptor.getValue();
        assertNotNull(notificacionEnviada);
        assertEquals("PRESTAMOS_ACTIVOS", notificacionEnviada.getType());

        String payload = (String) notificacionEnviada.getPayload();
        assertTrue(payload.contains("\"prestamos\""));
        assertTrue(payload.contains("\"solicitud\""));

        assertEquals("SQS", notificacionEnviada.getDestino());
    }


}

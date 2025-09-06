package co.com.crediya.api;

import co.com.crediya.api.config.CorsConfig;
import co.com.crediya.api.config.SecurityHeadersConfig;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.usecase.solicitud.SolicitudUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@Import({CorsConfig.class, SecurityHeadersConfig.class})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SolicitudUseCase solicitudUseCase;

    @Test
    void testRegistrarSolicitud_ok() {
        Solicitud solicitudMock = Solicitud.builder()
                .id(BigInteger.ONE)
                .monto(14_000_000L)
                .plazo("3 años")
                .email("carlos@email.com")
                .idTipoPrestamo(BigInteger.valueOf(2))
                .build();

        when(solicitudUseCase.registrarSolicitud(any(Solicitud.class)))
                .thenReturn(Mono.just(solicitudMock));

        webTestClient.post()
                .uri("/api/v1/solicitudes")
                .bodyValue(solicitudMock)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.monto").isEqualTo(14_000_000)
                .jsonPath("$.plazo").isEqualTo("3 años")
                .jsonPath("$.email").isEqualTo(solicitudMock.getEmail())
                .jsonPath("$.idTipoPrestamo").isEqualTo(solicitudMock.getIdTipoPrestamo().intValue());
    }
}

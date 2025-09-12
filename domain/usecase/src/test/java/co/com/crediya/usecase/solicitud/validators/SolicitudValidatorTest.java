package co.com.crediya.usecase.solicitud.validators;

import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.usecase.solicitud.exceptions.InvalidSolicitudException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitudValidatorTest {

    @Test
    void validate_WhenSolicitudIsValid_ShouldReturnSolicitud() {
        Solicitud solicitud = new Solicitud();
        solicitud.setMonto(10000L);
        solicitud.setPlazo("12 meses");
        solicitud.setEmail("cliente@test.com");
        solicitud.setIdTipoPrestamo(BigInteger.valueOf(1L));

        StepVerifier.create(SolicitudValidator.validate(solicitud))
                .expectNext(solicitud)
                .verifyComplete();
    }

    @Test
    void validate_WhenSolicitudIsInvalid_ShouldReturnErrors() {
        Solicitud solicitud = new Solicitud();

        StepVerifier.create(SolicitudValidator.validate(solicitud))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof InvalidSolicitudException);
                    InvalidSolicitudException ex = (InvalidSolicitudException) error;
                    assertTrue(ex.getErrors().contains("El monto del préstamo es obligatorio"));
                    assertTrue(ex.getErrors().contains("El plazo del préstamo es obligatorio"));
                    assertTrue(ex.getErrors().contains("El email del cliente es obligatorio"));
                    assertTrue(ex.getErrors().contains("El tipo de prestamo es obligatorio"));
                })
                .verify();
    }
}

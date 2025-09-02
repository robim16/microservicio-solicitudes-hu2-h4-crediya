package co.com.crediya.usecase.solicitud.validators;

import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.usecase.solicitud.exceptions.InvalidSolicitudException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class SolicitudValidator {
    public static Mono<Solicitud> validate(Solicitud solicitud) {
        List<String> errors = new ArrayList<>();

        if (solicitud.getMonto() == null) {
            errors.add("El monto del préstamo es obligatorio");
        }

        if (solicitud.getPlazo() == null || solicitud.getPlazo().isBlank()) {
            errors.add("El plazo del préstamo es obligatorio");
        }

        if (solicitud.getEmail() == null || solicitud.getEmail().isBlank()) {
            errors.add("El email del cliente es obligatorio");
        }

        if (solicitud.getIdTipoPrestamo() == null) {
            errors.add("El tipo de prestamo es obligatorio");
        }

        if (!errors.isEmpty()) {
            return Mono.error(new InvalidSolicitudException(errors));
        }

        return Mono.just(solicitud);
    }
}

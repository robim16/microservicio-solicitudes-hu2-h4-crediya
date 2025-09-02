package co.com.crediya.usecase.solicitud.exceptions;

import java.util.List;

public class InvalidSolicitudException extends RuntimeException {

    private final List<String> errors;

    public InvalidSolicitudException(List<String> errors) {
        super("Errores de validación en la solicitud");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}

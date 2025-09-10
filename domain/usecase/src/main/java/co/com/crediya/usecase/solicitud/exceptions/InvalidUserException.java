package co.com.crediya.usecase.solicitud.exceptions;

public class InvalidUserException extends RuntimeException{
    public InvalidUserException(String message) {
        super(message);
    }
}

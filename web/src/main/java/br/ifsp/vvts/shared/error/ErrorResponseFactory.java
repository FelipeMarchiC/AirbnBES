package br.ifsp.vvts.shared.error;

import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.shared.exceptions.UnauthenticatedUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ErrorResponseFactory {
    public static ResponseEntity<ErrorMessage> createErrorResponseFrom(Throwable throwable) {
        HttpStatus status;

        if (throwable instanceof EntityNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (throwable instanceof UnauthenticatedUserException) {
            status = HttpStatus.UNAUTHORIZED;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        var errorMessage = new ErrorMessage(status, throwable);
        return ResponseEntity.status(status).body(errorMessage);
    }
}

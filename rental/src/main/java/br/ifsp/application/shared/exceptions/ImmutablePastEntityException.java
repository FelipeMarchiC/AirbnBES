package br.ifsp.application.shared.exceptions;

public class ImmutablePastEntityException extends Exception {

    public ImmutablePastEntityException() {
        super();
    }

    public ImmutablePastEntityException(String message) {
        super(message);
    }

    public ImmutablePastEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImmutablePastEntityException(Throwable cause) {
        super(cause);
    }
}

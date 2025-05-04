package br.ifsp.vvts.shared.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorMessage {
    private final int code;
    private final String message;
    private final String detail;

    public ErrorMessage(HttpStatus status, Throwable throwable) {
        this.code = status.value();
        this.message = status.getReasonPhrase();
        this.detail = throwable.getMessage();
    }
}
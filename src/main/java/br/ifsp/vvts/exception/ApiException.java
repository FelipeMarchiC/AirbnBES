package br.ifsp.vvts.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class ApiException {
    private final String message;
    private final HttpStatus status;
    private final ZonedDateTime timestamp;
    private final String developerMessage;
}
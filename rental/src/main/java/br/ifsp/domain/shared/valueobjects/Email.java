package br.ifsp.domain.shared.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Email {
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private String email;

    public Email() { }

    public Email(String value) {
        if (value == null || !isValidEmail(value)) {
            throw new IllegalArgumentException("The following email is not valid: " + value);
        }
        this.email = value;
    }

    private boolean isValidEmail(String value) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
}

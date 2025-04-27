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
public class Phone {

    private static final String TELEFONE_REGEX = "^\\+55 \\([0-9]{2}\\) [0-9]{5}-[0-9]{4}$";
    private String phone;

    public Phone() {
    }

    public Phone(String value) {
        if (value == null || !isValidPhone(value)) {
            throw new IllegalArgumentException("The following phone number is not valid: " + value);
        }
        this.phone = value;
    }

    private boolean isValidPhone(String value) {
        Pattern pattern = Pattern.compile(TELEFONE_REGEX);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
}

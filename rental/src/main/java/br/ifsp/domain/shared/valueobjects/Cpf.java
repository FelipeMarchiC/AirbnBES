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
public class Cpf {

    private static final String CPF_REGEX = "^(\\d{3}\\.){2}\\d{3}-\\d{2}$";
    private String cpf;

    public Cpf() { }

    public Cpf(String value) {
        if (value == null || !isValidCPF(value)) {
            throw new IllegalArgumentException("The following cpf is not valid: " + value);
        }
        this.cpf = value;
    }

    private boolean isValidCPF(String value) {
        Pattern pattern = Pattern.compile(CPF_REGEX);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
}

package br.ifsp.domain.shared.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Objects;

@Embeddable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Address {

    @NonNull
    private String number;

    @NonNull
    private String street;

    @NonNull
    private String city;

    @NonNull
    private String state;

    @NonNull
    private String postalCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address other)) return false;
        return number.equals(other.number)
                && street.equals(other.street)
                && city.equals(other.city)
                && state.equals(other.state)
                && postalCode.equals(other.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, street, city, state, postalCode);
    }

    @Override
    public String toString() {
        return String.format("%s, %s - %s, %s, %s", street, number, city, state, postalCode);
    }
}


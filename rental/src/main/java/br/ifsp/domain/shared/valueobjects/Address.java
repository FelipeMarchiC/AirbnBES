package br.ifsp.domain.shared.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.*;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
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
}


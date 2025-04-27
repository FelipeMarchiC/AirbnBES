package br.ifsp.domain.models.user;

import br.ifsp.domain.shared.valueobjects.Cpf;
import br.ifsp.domain.shared.valueobjects.Email;
import br.ifsp.domain.shared.valueobjects.Phone;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "profile", discriminatorType = DiscriminatorType.STRING)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull @Column(nullable = false)
    private String name;

    @NonNull @Embedded
    private Email email;

    @NonNull @Embedded
    private Cpf cpf;

    @NonNull @Embedded
    private Phone phoneNumber;
}
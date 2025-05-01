package br.ifsp.domain.models.rental;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rental")
public class Rental {

    @Id
    @JdbcTypeCode(Types.VARCHAR)
    @NonNull @Column(nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private RentalState state;
}

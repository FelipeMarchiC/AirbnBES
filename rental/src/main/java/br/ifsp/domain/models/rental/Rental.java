package br.ifsp.domain.models.rental;

import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Price;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDate;
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
    @NotNull @Column(nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    @ToString.Exclude
    private Property property;

    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;

    @NotNull
    @Embedded
    private Price value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalState state;
}

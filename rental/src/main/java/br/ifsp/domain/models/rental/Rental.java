package br.ifsp.domain.models.rental;

import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.user.User;
import jakarta.persistence.*;
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
    @NonNull @Column(nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @NonNull
    @Column(nullable = false)
    private LocalDate startDate;

    @NonNull
    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalState state;
}

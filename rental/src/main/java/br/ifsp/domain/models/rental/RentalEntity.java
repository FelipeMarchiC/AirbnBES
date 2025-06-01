package br.ifsp.domain.models.rental;

import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.domain.shared.valueobjects.Price;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDate;
import java.util.UUID;


@Builder
@Entity
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "rental")
public class RentalEntity {

    @Id
    @JdbcTypeCode(Types.VARCHAR)
    @NotNull
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private PropertyEntity propertyEntity;

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

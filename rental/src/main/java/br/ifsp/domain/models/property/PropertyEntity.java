package br.ifsp.domain.models.property;

import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Builder
@Entity
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "property")
public class PropertyEntity {

    @Id
    @JdbcTypeCode(Types.VARCHAR)
    @NonNull
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NonNull
    @Column(nullable = false)
    private String name;

    @NonNull
    @Column(nullable = false)
    private String description;

    @Embedded
    @NonNull
    private Price dailyRate;

    @Embedded
    @NonNull
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @OneToMany(mappedBy = "propertyEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RentalEntity> rentals = new ArrayList<>();
}

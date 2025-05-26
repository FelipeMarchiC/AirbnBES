package br.ifsp.domain.models.property;

import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "property")
public class PropertyEntity {

    @Id
    @JdbcTypeCode(Types.VARCHAR)
    @NonNull @Column(nullable = false)
    private UUID id;

    @NonNull @Column(nullable = false)
    private String name;

    @NonNull @Column(nullable = false)
    private String description;

    @Embedded
    @NonNull
    private Price dailyRate;

    @Embedded
    @NonNull
    private Address address;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<RentalEntity> rentalEntities = new ArrayList<>();

    public void addRental(RentalEntity rentalEntity) {
        if (rentalEntities == null) rentalEntities = new ArrayList<>();

        if (!rentalEntities.contains(rentalEntity)) {
            rentalEntities.add(rentalEntity);
            rentalEntity.setPropertyEntity(this);
        }
    }
}

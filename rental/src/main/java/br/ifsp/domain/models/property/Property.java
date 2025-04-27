package br.ifsp.domain.models.property;

import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "property")
public class Property {

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
}

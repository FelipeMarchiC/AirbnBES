package br.ifsp.domain.models.user;

import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Builder
public class User {
    @Getter private final UUID id;
    @Getter private final String name;
    @Getter private final String lastname;
    @Getter private final String email;

    private final List<Property> ownedProperties = new ArrayList<>();

    public User(UserEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.lastname = entity.getLastname();
        this.email = entity.getEmail();
    }

    public User(UUID id, String name, String lastname, String email) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
    }

    public List<Property> getOwnedProperties() {
        return Collections.unmodifiableList(ownedProperties);
    }

    public Property createProperty(
            UUID propertyId,
            String name,
            String description,
            Price dailyRate,
            Address address
    ) {
        Property property = new Property(
                propertyId,
                name,
                description,
                dailyRate,
                address,
                this,
                new ArrayList<>()
        );
        ownedProperties.add(property);
        return property;
    }

    public void addOwnedProperty(Property property) {
        if (!ownedProperties.contains(property)) {
            ownedProperties.add(property);
        }
    }
}



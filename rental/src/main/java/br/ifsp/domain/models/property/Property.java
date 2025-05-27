package br.ifsp.domain.models.property;

import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Property {
    @Getter
    private final UUID id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String description;

    @Getter
    private final Price dailyRate;

    @Getter
    private final Address address;

    @Getter
    private final User owner;

    private final List<Rental> rentals;

    public Property(PropertyEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.dailyRate = entity.getDailyRate();
        this.address = entity.getAddress();
        this.owner = new User(entity.getOwner());
        this.rentals = entity.getRentals().stream().map(Rental::new).collect(Collectors.toList());
    }

    public Property(
            UUID id,
            String name,
            String description,
            Price dailyRate,
            Address address,
            User owner,
            List<Rental> rentals
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dailyRate = dailyRate;
        this.address = address;
        this.owner = owner;
        this.rentals = new ArrayList<>(rentals != null ? rentals : new ArrayList<>());
    }

    public List<Rental> getRentals() {
        return Collections.unmodifiableList(rentals);
    }

    public void addRental(Rental rental) {
        if (!rentals.contains(rental) && rental.getProperty() == this) {
            rentals.add(rental);
        }
    }
}



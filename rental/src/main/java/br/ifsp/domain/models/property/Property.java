package br.ifsp.domain.models.property;

import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
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

    public Rental createRental(
            UUID id,
            User user,
            LocalDate startDate,
            LocalDate endDate,
            Price value,
            RentalState state
    ) {
        Rental rental = Rental.builder()
                .id(id)
                .user(user)
                .property(this)
                .startDate(startDate)
                .endDate(endDate)
                .value(value)
                .state(state)
                .build();

        this.addRental(rental);
        return rental;
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



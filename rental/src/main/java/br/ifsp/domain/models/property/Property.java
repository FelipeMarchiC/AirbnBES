package br.ifsp.domain.models.property;

import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    private Property(
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
            RentalState state,
            Clock clock
    ) {
        Rental rental = Rental.builder()
                .id(id)
                .user(user)
                .property(this)
                .startDate(startDate)
                .endDate(endDate)
                .value(value)
                .state(state)
                .clock(clock)
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



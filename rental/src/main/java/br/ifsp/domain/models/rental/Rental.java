package br.ifsp.domain.models.rental;

import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Price;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

public class Rental {

    @Getter
    private final UUID id;

    @Getter
    private final User user;

    @Getter
    private final Property property;

    @Getter
    private final LocalDate startDate;

    @Getter
    private final LocalDate endDate;

    @Getter
    private final Price value;

    @Getter
    @Setter
    private RentalState state;

    public Rental(RentalEntity entity) {
        this.id = entity.getId();
        this.user = new User(entity.getUserEntity());
        this.property = new Property(entity.getPropertyEntity());
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.value = entity.getValue();
        this.state = entity.getState();
    }

    public Rental(
            UUID id,
            User user,
            Property property,
            LocalDate startDate,
            LocalDate endDate,
            Price value,
            RentalState state
    ) {
        this.id = id;
        this.user = user;
        this.property = property;
        this.startDate = startDate;
        this.endDate = endDate;
        this.value = value;
        this.state = state;
    }
}

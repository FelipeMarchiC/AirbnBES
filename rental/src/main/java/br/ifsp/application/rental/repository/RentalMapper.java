package br.ifsp.application.rental.repository;

import br.ifsp.application.rental.create.ICreateRentalService;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Price;

import java.math.BigDecimal;
import java.util.UUID;

public class RentalMapper {
    public static Rental fromCreateRequestModel(
            UUID rentalId,
            ICreateRentalService.RequestModel requestModel,
            User user,
            Property property,
            BigDecimal value
    ) {
        return Rental.builder()
                .id(rentalId)
                .user(user)
                .property(property)
                .startDate(requestModel.startDate())
                .endDate(requestModel.endDate())
                .value(new Price(value))
                .state(RentalState.PENDING)
                .build();
    }
}

package br.ifsp.application.rental.repository;

import br.ifsp.application.rental.create.ICreateRentalService;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.services.IUuidGeneratorService;
import br.ifsp.domain.shared.valueobjects.Price;

import java.math.BigDecimal;
import java.util.UUID;

public class RentalMapper {
    private final IUuidGeneratorService uuidGeneratorService;

    public RentalMapper(IUuidGeneratorService uuidGeneratorService) {
        this.uuidGeneratorService = uuidGeneratorService;
    }

    public RentalEntity fromCreateRequestModel(
            ICreateRentalService.RequestModel requestModel,
            User user,
            PropertyEntity propertyEntity,
            BigDecimal value
    ) {
        return RentalEntity.builder()
                .id(uuidGeneratorService.generate())
                .user(user)
                .propertyEntity(propertyEntity)
                .startDate(requestModel.startDate())
                .endDate(requestModel.endDate())
                .value(new Price(value))
                .state(RentalState.PENDING)
                .build();
    }

    public static RentalEntity fromCreateRequestModel(
            UUID rentalId,
            ICreateRentalService.RequestModel requestModel,
            User user,
            PropertyEntity propertyEntity,
            BigDecimal value
    ) {
        return RentalEntity.builder()
                .id(rentalId)
                .user(user)
                .propertyEntity(propertyEntity)
                .startDate(requestModel.startDate())
                .endDate(requestModel.endDate())
                .value(new Price(value))
                .state(RentalState.PENDING)
                .build();
    }
}

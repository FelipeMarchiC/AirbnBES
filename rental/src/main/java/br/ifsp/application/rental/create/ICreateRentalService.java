package br.ifsp.application.rental.create;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface ICreateRentalService {
    void registerRental(CreateRentalPresenter presenter, RequestModel request);

    record RequestModel(
            @NotNull(message = "User ID is required") UUID userId,
            @NotNull(message = "Property ID is required") UUID propertyId,
            @NotNull(message = "Start date is required") LocalDate startDate,
            @NotNull(message = "End date is required") LocalDate endDate
    ){}

    record ResponseModel(
            UUID rentalId,
            UUID tenantId
    ){}
}
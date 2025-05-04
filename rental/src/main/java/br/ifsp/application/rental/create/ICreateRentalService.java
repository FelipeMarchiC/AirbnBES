package br.ifsp.application.rental.create;

import java.time.LocalDate;
import java.util.UUID;

public interface ICreateRentalService {
    void registerRental(CreateRentalPresenter presenter, RequestModel request);

    record RequestModel(
            UUID userId,
            UUID propertyId,
            LocalDate startDate,
            LocalDate endDate
    ){}

    record ResponseModel(
            UUID rentalId,
            UUID tenantId
    ){}
}
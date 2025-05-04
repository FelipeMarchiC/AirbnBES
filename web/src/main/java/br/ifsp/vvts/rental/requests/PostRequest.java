package br.ifsp.vvts.rental.requests;

import br.ifsp.application.rental.create.ICreateRentalService.RequestModel;

import java.time.LocalDate;
import java.util.UUID;

public record PostRequest(
        UUID propertyId,
        LocalDate startDate,
        LocalDate endDate
) {
    public RequestModel toCreateRequestModel(UUID userId) {
        return new RequestModel(userId, propertyId, startDate, endDate);
    }
}

package br.ifsp.application.rental.update.owner;

import java.time.LocalDate;

public interface IOwnerUpdateRentalService {
    void confirmRental(OwnerUpdateRentalPresenter presenter, RequestModel request);
    void denyRental(OwnerUpdateRentalPresenter presenter, RequestModel request);
    void cancelRental(OwnerUpdateRentalPresenter presenter, RequestModel request, LocalDate cancelDate);

    record RequestModel(java.util.UUID ownerId, java.util.UUID rentalId) {}
    record ResponseModel(java.util.UUID ownerId, java.util.UUID tenantId) {}
}

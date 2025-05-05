package br.ifsp.application.rental.delete;

import java.util.UUID;

public interface IDeleteRentalService {
    void delete(DeleteRentalPresenter presenter, RequestModel request);

    record RequestModel(UUID ownerId, UUID rentalId) {}
    record ResponseModel(UUID ownerId, UUID tenantId) {}
}

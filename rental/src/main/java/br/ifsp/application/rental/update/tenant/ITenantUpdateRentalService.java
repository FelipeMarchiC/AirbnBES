package br.ifsp.application.rental.update.tenant;

import java.util.UUID;

public interface ITenantUpdateRentalService {
    void cancelRental(TenantUpdateRentalPresenter presenter, RequestModel request);

    record RequestModel(
            UUID tenantId,
            UUID rentalId
    ){}

    record ResponseModel(
            UUID rentalId,
            UUID tenantId
    ){}
}
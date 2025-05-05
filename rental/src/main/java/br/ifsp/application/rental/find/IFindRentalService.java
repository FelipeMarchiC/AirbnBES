package br.ifsp.application.rental.find;

import br.ifsp.domain.models.rental.Rental;

import java.util.List;
import java.util.UUID;

public interface IFindRentalService {
    void getRentalHistoryByProperty(FindByPropertyIdRequestModel findByPropertyIdRequestModel, FindRentalPresenter presenter);
    void getRentalHistoryByTenant(FindByTenantIdRequestModel requestModel, FindRentalPresenter presenter);
    record FindByPropertyIdRequestModel(UUID propertyId) {}
    record FindByTenantIdRequestModel(UUID tenantId){}
    record ResponseModel(List<Rental> rentalList) {}

}

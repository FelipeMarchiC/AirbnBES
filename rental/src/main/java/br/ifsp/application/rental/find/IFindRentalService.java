package br.ifsp.application.rental.find;

import br.ifsp.domain.models.rental.Rental;

import java.util.List;
import java.util.UUID;

public interface IFindRentalService {
    void getRentalHistoryByProperty(RequestModel requestModel, FindRentalPresenter presenter);
    record RequestModel(UUID propertyId) {}
    record ResponseModel(List<Rental> rentalList) {}
}

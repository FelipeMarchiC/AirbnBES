package br.ifsp.application.rental.delete;

import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;

public class DeleteRentalService {
    public void delete(Rental rental){
        if (rental.getState() != RentalState.PENDING && rental.getState() != RentalState.DENIED) {
            throw new IllegalArgumentException("State must be PENDING or DENIED");
        }
    }
}

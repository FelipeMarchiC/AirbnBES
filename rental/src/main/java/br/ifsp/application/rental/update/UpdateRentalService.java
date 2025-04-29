package br.ifsp.application.rental.update;

import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;

public class UpdateRentalService {
    public void deny(Rental rental){
        rental.setState(RentalState.DENIED);
    }
}

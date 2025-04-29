package br.ifsp.application.rental.update;

import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;

public class UpdateRentalService {
    public void deny(Rental rental){
        if(!rental.getState().equals(RentalState.PENDING) && !rental.getState().equals(RentalState.RESTRAINED)){
            throw new UnsupportedOperationException(String.format("it´s not possible to deny a rental that is %s",rental.getState().toString()));
        }
        rental.setState(RentalState.DENIED);
    }
}

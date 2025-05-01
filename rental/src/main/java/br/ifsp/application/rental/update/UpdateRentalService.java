package br.ifsp.application.rental.update;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;

import java.util.UUID;

public class UpdateRentalService {

    private final JpaRentalRepository rentalRepository;

    public UpdateRentalService(JpaRentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }


    public void deny(Rental rental){
        if(!rental.getState().equals(RentalState.PENDING) && !rental.getState().equals(RentalState.RESTRAINED)){
            throw new UnsupportedOperationException(String.format("it´s not possible to deny a rental that is %s",rental.getState().toString()));
        }
        rental.setState(RentalState.DENIED);
    }


    public void confirmRental(UUID rentalId) {

    }

}

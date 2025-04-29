package br.ifsp.application.rental.delete;

import br.ifsp.application.rental.repository.RentalRepository;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;

public class DeleteRentalService {

    private final RentalRepository rentalRepository;

    public DeleteRentalService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public String delete(Rental rental){
        if (rental.getState() != RentalState.PENDING && rental.getState() != RentalState.DENIED) {
            throw new IllegalArgumentException("State must be PENDING or DENIED");
        }
        return null;
    }
}

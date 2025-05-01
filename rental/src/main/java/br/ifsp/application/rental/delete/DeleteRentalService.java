package br.ifsp.application.rental.delete;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;

import java.util.UUID;

public class DeleteRentalService {

    private final JpaRentalRepository JpaRentalRepository;

    public DeleteRentalService(JpaRentalRepository JpaRentalRepository) {
        this.JpaRentalRepository = JpaRentalRepository;
    }

    public UUID delete(Rental rental){
        if (rental.getState() != RentalState.PENDING && rental.getState() != RentalState.DENIED) {
            throw new IllegalArgumentException("State must be PENDING or DENIED");
        }

        JpaRentalRepository.deleteById(rental.getId());
        return rental.getId();
    }
}

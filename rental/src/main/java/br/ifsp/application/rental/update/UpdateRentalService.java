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
            throw new UnsupportedOperationException(String.format("it´s not possible to deny a rental that is %s",rental.getState()));
        }
        rental.setState(RentalState.DENIED);
    }


    public void confirmRental(UUID rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found"));

        if (rental.getState() != RentalState.PENDING) {
            throw new IllegalStateException("Rental is not in pending state");
        }

        var conflictingRentals = rentalRepository.findRentalsByOverlapAndState(
                rental.getProperty().getId(),
                RentalState.CONFIRMED,
                rental.getStartDate(),
                rental.getEndDate(),
                rental.getId()
        );

        if (!conflictingRentals.isEmpty()) {
            throw new IllegalStateException("Cannot confirm rental due to conflict with an already confirmed rental");
        }

        rental.setState(RentalState.CONFIRMED);
        rentalRepository.save(rental);
    }

    public void restrainPendingRentalsInConflict(Rental confirmedRental) {
        var conflictingRentals = rentalRepository.findRentalsByOverlapAndState(
                confirmedRental.getProperty().getId(),
                RentalState.PENDING,
                confirmedRental.getStartDate(),
                confirmedRental.getEndDate(),
                confirmedRental.getId()
        );

        for (Rental conflictingRental : conflictingRentals) {
            conflictingRental.setState(RentalState.RESTRAINED);
            rentalRepository.save(conflictingRental);
        }
    }
}



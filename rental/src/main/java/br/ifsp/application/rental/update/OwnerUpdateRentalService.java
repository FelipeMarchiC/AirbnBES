package br.ifsp.application.rental.update;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public class OwnerUpdateRentalService {

    private final JpaRentalRepository rentalRepository;

    public OwnerUpdateRentalService(JpaRentalRepository rentalRepository) {
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
                .orElseThrow(() -> new IllegalArgumentException("Rental not found."));

        if (!rental.getState().equals(RentalState.PENDING)) {
            throw new UnsupportedOperationException("Rental is not in a PENDING state and cannot be confirmed.");
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
        UUID propertyId = confirmedRental.getProperty().getId();
        UUID confirmedRentalId = confirmedRental.getId();
        LocalDate confirmedStart = confirmedRental.getStartDate();
        LocalDate confirmedEnd = confirmedRental.getEndDate();

        List<Rental> conflictingRentals = findPendingRentalsInConflict(propertyId, confirmedStart, confirmedEnd, confirmedRentalId);

        conflictingRentals.forEach(this::restrainRental);
    }

    private List<Rental> findPendingRentalsInConflict(UUID propertyId, LocalDate startDate, LocalDate endDate, UUID excludedRentalId) {
        return rentalRepository.findRentalsByOverlapAndState(
                propertyId,
                RentalState.PENDING,
                startDate,
                endDate,
                excludedRentalId
        );
    }

    private void restrainRental(Rental rental) {
        rental.setState(RentalState.RESTRAINED);
        rentalRepository.save(rental);
    }

    public void cancel(Rental rental) {
        if(!rental.getState().equals(RentalState.CONFIRMED)) throw new IllegalArgumentException("The Rental is not confirmed to be canceled");

    }
}



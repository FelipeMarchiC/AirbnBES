package br.ifsp.application.rental.update.owner;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.shared.presenter.PreconditionChecker;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class OwnerUpdateRentalService implements IOwnerUpdateRentalService {

    private final JpaRentalRepository rentalRepository;
    private final Clock clock;

    public OwnerUpdateRentalService(JpaRentalRepository rentalRepository, Clock clock) {
        this.rentalRepository = rentalRepository;
        this.clock = clock;
    }

    @Override
    public void confirmRental(OwnerUpdateRentalPresenter presenter, RequestModel request) {
        try {
            Rental rental = rentalRepository.findById(request.rentalId())
                    .orElseThrow(() -> new EntityNotFoundException("Rental not found."));

            if (!rental.getProperty().getOwner().getId().equals(request.ownerId())) {
                throw new SecurityException("Only the property owner can confirm the rental.");
            }

            if (!rental.getState().equals(RentalState.PENDING)) {
                throw new UnsupportedOperationException("Rental must be in a PENDING state to be confirmed.");
            }

            var conflicts = rentalRepository.findRentalsByOverlapAndState(
                    rental.getProperty().getId(),
                    RentalState.CONFIRMED,
                    rental.getStartDate(),
                    rental.getEndDate(),
                    rental.getId()
            );

            if (!conflicts.isEmpty()) {
                throw new IllegalStateException("Cannot confirm rental due to conflict with another confirmed rental.");
            }

            rental.setState(RentalState.CONFIRMED);
            rentalRepository.save(rental);
            restrainPendingRentalsInConflict(rental);

            presenter.prepareSuccessView(new ResponseModel(request.ownerId(), rental.getUser().getId()));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    @Override
    public void denyRental(OwnerUpdateRentalPresenter presenter, RequestModel request) {
        try {
            Rental rental = rentalRepository.findById(request.rentalId())
                    .orElseThrow(() -> new EntityNotFoundException("Rental not found."));

            if (!rental.getProperty().getOwner().getId().equals(request.ownerId())) {
                throw new SecurityException("Only the property owner can deny the rental.");
            }

            if (!List.of(RentalState.PENDING, RentalState.RESTRAINED).contains(rental.getState())) {
                throw new UnsupportedOperationException(
                        String.format("Cannot deny a rental that is %s.", rental.getState())
                );
            }

            rental.setState(RentalState.DENIED);
            rentalRepository.save(rental);

            presenter.prepareSuccessView(new ResponseModel(request.ownerId(), rental.getUser().getId()));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    @Override
    public void cancelRental(OwnerUpdateRentalPresenter presenter, RequestModel request, LocalDate cancelDate) {
        try {
            Rental rental = rentalRepository.findById(request.rentalId())
                    .orElseThrow(() -> new EntityNotFoundException("Rental not found."));

                if (!rental.getProperty().getOwner().getId().equals(request.ownerId())) {
                throw new SecurityException("Only the property owner can cancel the rental.");
            }

            if (cancelDate == null) LocalDate.now(clock);
            PreconditionChecker.prepareIfTheDateIsInThePast(presenter, clock, rental.getStartDate());
            if (presenter.isDone()) return;

            if (!rental.getState().equals(RentalState.CONFIRMED)) {
                throw new IllegalArgumentException("Only confirmed rentals can be cancelled.");
            }

            rental.setState(RentalState.CANCELLED);

            List<Rental> restrainedConflicts = findRestrainedConflictingRentals(rental);
            restrainedConflicts.forEach(r -> r.setState(RentalState.PENDING));

            rentalRepository.save(rental);
            rentalRepository.saveAll(restrainedConflicts);

            presenter.prepareSuccessView(new ResponseModel(request.ownerId(), rental.getUser().getId()));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    public void restrainPendingRentalsInConflict(Rental confirmedRental) {
        List<Rental> pendingConflicts = rentalRepository.findRentalsByOverlapAndState(
                confirmedRental.getProperty().getId(),
                RentalState.PENDING,
                confirmedRental.getStartDate(),
                confirmedRental.getEndDate(),
                confirmedRental.getId()
        );
        pendingConflicts.forEach(r -> {
            r.setState(RentalState.RESTRAINED);
            rentalRepository.save(r);
        });
    }

    private List<Rental> findRestrainedConflictingRentals(Rental rental) {
        return rentalRepository.findRentalsByOverlapAndState(
                rental.getProperty().getId(),
                RentalState.RESTRAINED,
                rental.getStartDate(),
                rental.getEndDate(),
                rental.getId()
        );
    }
}

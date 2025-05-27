package br.ifsp.application.rental.update.owner;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.shared.presenter.PreconditionChecker;
import br.ifsp.domain.models.rental.RentalEntity;
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
            RentalEntity rentalEntity = rentalRepository.findById(request.rentalId())
                    .orElseThrow(() -> new EntityNotFoundException("Rental not found."));

            if (!rentalEntity.getPropertyEntity().getOwner().getId().equals(request.ownerId())) {
                throw new SecurityException("Only the property owner can confirm the rental.");
            }

            if (!rentalEntity.getState().equals(RentalState.PENDING)) {
                throw new UnsupportedOperationException("Rental must be in a PENDING state to be confirmed.");
            }

            var conflicts = rentalRepository.findRentalsByOverlapAndState(
                    rentalEntity.getPropertyEntity().getId(),
                    RentalState.CONFIRMED,
                    rentalEntity.getStartDate(),
                    rentalEntity.getEndDate(),
                    rentalEntity.getId()
            );

            if (!conflicts.isEmpty()) {
                throw new IllegalStateException("Cannot confirm rental due to conflict with another confirmed rental.");
            }

            rentalEntity.setState(RentalState.CONFIRMED);
            rentalRepository.save(rentalEntity);
            restrainPendingRentalsInConflict(rentalEntity);

            presenter.prepareSuccessView(new ResponseModel(request.ownerId(), rentalEntity.getUserEntity().getId()));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    @Override
    public void denyRental(OwnerUpdateRentalPresenter presenter, RequestModel request) {
        try {
            RentalEntity rentalEntity = rentalRepository.findById(request.rentalId())
                    .orElseThrow(() -> new EntityNotFoundException("Rental not found."));

            if (!rentalEntity.getPropertyEntity().getOwner().getId().equals(request.ownerId())) {
                throw new SecurityException("Only the property owner can deny the rental.");
            }

            if (!List.of(RentalState.PENDING, RentalState.RESTRAINED).contains(rentalEntity.getState())) {
                throw new UnsupportedOperationException(
                        String.format("Cannot deny a rental that is %s.", rentalEntity.getState())
                );
            }

            rentalEntity.setState(RentalState.DENIED);
            rentalRepository.save(rentalEntity);

            presenter.prepareSuccessView(new ResponseModel(request.ownerId(), rentalEntity.getUserEntity().getId()));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    @Override
    public void cancelRental(OwnerUpdateRentalPresenter presenter, RequestModel request, LocalDate cancelDate) {
        try {
            RentalEntity rentalEntity = rentalRepository.findById(request.rentalId())
                    .orElseThrow(() -> new EntityNotFoundException("Rental not found."));

                if (!rentalEntity.getPropertyEntity().getOwner().getId().equals(request.ownerId())) {
                throw new SecurityException("Only the property owner can cancel the rental.");
            }

            if (cancelDate == null) LocalDate.now(clock);
            PreconditionChecker.prepareIfTheDateIsInThePast(presenter, clock, rentalEntity.getStartDate());
            if (presenter.isDone()) return;

            if (!rentalEntity.getState().equals(RentalState.CONFIRMED)) {
                throw new IllegalArgumentException("Only confirmed rentals can be cancelled.");
            }

            rentalEntity.setState(RentalState.CANCELLED);

            List<RentalEntity> restrainedConflicts = findRestrainedConflictingRentals(rentalEntity);
            restrainedConflicts.forEach(r -> r.setState(RentalState.PENDING));

            rentalRepository.save(rentalEntity);
            rentalRepository.saveAll(restrainedConflicts);

            presenter.prepareSuccessView(new ResponseModel(request.ownerId(), rentalEntity.getUserEntity().getId()));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    public void restrainPendingRentalsInConflict(RentalEntity confirmedRentalEntity) {
        List<RentalEntity> pendingConflicts = rentalRepository.findRentalsByOverlapAndState(
                confirmedRentalEntity.getPropertyEntity().getId(),
                RentalState.PENDING,
                confirmedRentalEntity.getStartDate(),
                confirmedRentalEntity.getEndDate(),
                confirmedRentalEntity.getId()
        );
        pendingConflicts.forEach(r -> {
            r.setState(RentalState.RESTRAINED);
            rentalRepository.save(r);
        });
    }

    private List<RentalEntity> findRestrainedConflictingRentals(RentalEntity rentalEntity) {
        return rentalRepository.findRentalsByOverlapAndState(
                rentalEntity.getPropertyEntity().getId(),
                RentalState.RESTRAINED,
                rentalEntity.getStartDate(),
                rentalEntity.getEndDate(),
                rentalEntity.getId()
        );
    }
}

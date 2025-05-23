package br.ifsp.application.rental.update.tenant;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.shared.presenter.PreconditionChecker;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
public class TenantUpdateRentalService implements ITenantUpdateRentalService {
    private final JpaRentalRepository rentalRepository;
    private final JpaUserRepository userRepository;
    private final Clock clock;

    public TenantUpdateRentalService(
            JpaRentalRepository rentalRepository,
            JpaUserRepository userRepository,
            Clock clock
    ) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    public void cancelRental(TenantUpdateRentalPresenter presenter, RequestModel request) {
        User user = userRepository.findById(request.tenantId()).orElse(null);
        PreconditionChecker.prepareIfFailsPreconditions(presenter, user);
        if (presenter.isDone()) return;

        try {
            Rental rental = rentalRepository.findById(request.rentalId())
                    .orElseThrow(() -> new IllegalArgumentException("Rental not found"));

            PreconditionChecker.prepareIfTheDateIsInThePast(presenter, clock, rental.getStartDate());
            if (presenter.isDone()) return;

            if (rental.getState() != RentalState.CONFIRMED) {
                throw new IllegalArgumentException("Rental is not in a valid state to be cancelled");
            }

            rental.setState(RentalState.CANCELLED);
            Rental updatedRental = rentalRepository.save(rental);

            unrestrainConflitingRentals(updatedRental);

            presenter.prepareSuccessView(
                    new ITenantUpdateRentalService.ResponseModel(rental.getId(), user.getId())
            );
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    private void unrestrainConflitingRentals(Rental confirmedRental) {
        List<Rental> pendingConflicts = rentalRepository.findRentalsByOverlapAndState(
                confirmedRental.getProperty().getId(),
                RentalState.RESTRAINED,
                confirmedRental.getStartDate(),
                confirmedRental.getEndDate(),
                confirmedRental.getId()
        );

        pendingConflicts.forEach(r -> {
            r.setState(RentalState.PENDING);
            rentalRepository.save(r);
        });
    }
}

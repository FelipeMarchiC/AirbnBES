package br.ifsp.application.rental.update.tenant;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.shared.presenter.PreconditionChecker;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.UserEntity;
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
        UserEntity userEntity = userRepository.findById(request.tenantId()).orElse(null);
        PreconditionChecker.prepareIfFailsPreconditions(presenter, userEntity);
        if (presenter.isDone()) return;

        try {
            RentalEntity rentalEntity = rentalRepository.findById(request.rentalId())
                    .orElseThrow(() -> new IllegalArgumentException("Rental not found"));

            PreconditionChecker.prepareIfTheDateIsInThePast(presenter, clock, rentalEntity.getStartDate());
            if (presenter.isDone()) return;

            if (rentalEntity.getState() != RentalState.CONFIRMED) {
                throw new IllegalArgumentException("Rental is not in a valid state to be cancelled");
            }

            rentalEntity.setState(RentalState.CANCELLED);
            RentalEntity updatedRentalEntity = rentalRepository.save(rentalEntity);

            unrestrainConflitingRentals(updatedRentalEntity);

            presenter.prepareSuccessView(
                    new ITenantUpdateRentalService.ResponseModel(rentalEntity.getId(), userEntity.getId())
            );
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    private void unrestrainConflitingRentals(RentalEntity confirmedRentalEntity) {
        List<RentalEntity> pendingConflicts = rentalRepository.findRentalsByOverlapAndState(
                confirmedRentalEntity.getPropertyEntity().getId(),
                RentalState.RESTRAINED,
                confirmedRentalEntity.getStartDate(),
                confirmedRentalEntity.getEndDate(),
                confirmedRentalEntity.getId()
        );

        pendingConflicts.forEach(r -> {
            r.setState(RentalState.PENDING);
            rentalRepository.save(r);
        });
    }
}

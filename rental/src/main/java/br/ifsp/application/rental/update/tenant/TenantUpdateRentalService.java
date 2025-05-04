package br.ifsp.application.rental.update.tenant;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.shared.presenter.PreconditionChecker;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;

public class TenantUpdateRentalService implements ITenantUpdateRentalService {
    private final JpaRentalRepository rentalRepository;
    private final JpaUserRepository userRepository;

    public TenantUpdateRentalService(
            JpaRentalRepository rentalRepository,
            JpaUserRepository userRepository
    ) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void cancelRental(TenantUpdateRentalPresenter presenter, RequestModel request) {
        User user = userRepository.findById(request.tenantId()).orElse(null);
        PreconditionChecker.prepareIfFailsPreconditions(presenter, user);
        if (presenter.isDone()) return;

        try {
            Rental rental = rentalRepository.findById(request.rentalId())
                    .orElseThrow(() -> new IllegalArgumentException("Rental not found"));

            if (rental.getState() != RentalState.CONFIRMED) {
                throw new IllegalArgumentException("Rental is not in a valid state to be cancelled");
            }

            rental.setState(RentalState.CANCELLED);
            rentalRepository.save(rental);

            assert user != null;
            presenter.prepareSuccessView(
                    new ITenantUpdateRentalService.ResponseModel(rental.getId(), user.getId())
            );
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }
}

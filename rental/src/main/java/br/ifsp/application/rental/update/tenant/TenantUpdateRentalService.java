package br.ifsp.application.rental.update.tenant;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.repository.RentalMapper;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.shared.presenter.PreconditionChecker;
import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.application.user.repository.UserMapper;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.models.user.UserEntity;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

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
        User user = getUser(request).orElse(null);
        PreconditionChecker.prepareIfFailsPreconditions(presenter, user);
        if (presenter.isDone()) return;

        try {
            RentalEntity rentalEntity = rentalRepository.findById(request.rentalId())
                    .orElseThrow(() -> new EntityNotFoundException("Rental not found"));

            Rental rental = RentalMapper.toDomain(rentalEntity, clock);

            PreconditionChecker.prepareIfTheDateIsInThePast(presenter, clock, rental.getStartDate());
            if (presenter.isDone()) return;

            if (rental.getState() != RentalState.CONFIRMED) {
                throw new IllegalArgumentException("Rental is not in a valid state to be cancelled");
            }

            rental.setState(RentalState.CANCELLED);
            rentalRepository.save(RentalMapper.toEntity(rental));

            setRestrainedRentalsToPending(rental);

            presenter.prepareSuccessView(new ResponseModel(rental.getId(), user.getId()));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    private Optional<User> getUser(RequestModel request) {
        UserEntity userEntity = userRepository.findById(request.tenantId()).orElse(null);

        return userEntity != null ? Optional.of(UserMapper.toDomain(userEntity)) : Optional.empty();
    }

    private void setRestrainedRentalsToPending(Rental confirmedRental) {
        List<RentalEntity> pendingConflicts = rentalRepository.findRentalsByOverlapAndState(
                confirmedRental.getProperty().getId(),
                RentalState.RESTRAINED,
                confirmedRental.getStartDate(),
                confirmedRental.getEndDate(),
                confirmedRental.getId()
        );

        pendingConflicts.forEach(r -> {
            var rental = RentalMapper.toDomain(r, clock);

            if (rental.getState() != RentalState.EXPIRED) {
                rental.setState(RentalState.PENDING);
            }

            rentalRepository.save(RentalMapper.toEntity(rental));
        });
    }
}

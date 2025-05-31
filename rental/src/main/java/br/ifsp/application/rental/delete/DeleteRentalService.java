package br.ifsp.application.rental.delete;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.repository.RentalMapper;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.shared.presenter.PreconditionChecker;
import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.application.user.repository.UserMapper;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalEntity; // Mantido para o .orElseThrow original, mas será removido no próximo commit
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.models.user.UserEntity;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class DeleteRentalService implements IDeleteRentalService {

    private final JpaRentalRepository rentalRepository;
    private final JpaUserRepository userRepository;
    private final Clock clock;

    public DeleteRentalService(JpaRentalRepository rentalRepository,
                               JpaUserRepository userRepository,
                               Clock clock) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    public void delete(DeleteRentalPresenter presenter, RequestModel request) {
        User user = getUser(request).orElse(null);
        PreconditionChecker.prepareIfFailsPreconditions(presenter, user);
        if (presenter.isDone()) return;

        try {
            var rentalEntity = rentalRepository.findById(request.rentalId())
                    .orElseThrow(() -> new IllegalArgumentException("Rental not found."));
            Rental rental = RentalMapper.toDomain(rentalEntity, clock);

            if (rental.getState() != RentalState.PENDING && rental.getState() != RentalState.DENIED) {
                throw new IllegalArgumentException("Rental state must be PENDING or DENIED");
            }

            if (!rental.getStartDate().isAfter(LocalDate.now(clock))) {
                throw new IllegalArgumentException("Cannot delete rentals that have already started");
            }

            rentalRepository.deleteById(rental.getId());

            var response = new ResponseModel(request.ownerId(), rental.getUser().getId());
            presenter.prepareSuccessView(response);
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    private Optional<User> getUser(RequestModel request) {
        UserEntity userEntity = userRepository.findById(request.ownerId()).orElse(null);
        return userEntity != null ? Optional.of(UserMapper.toDomain(userEntity)) : Optional.empty();
    }
}
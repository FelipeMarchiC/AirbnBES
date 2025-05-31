package br.ifsp.application.rental.delete;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.shared.presenter.PreconditionChecker; // Adicionado
import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.application.user.repository.UserMapper; // Adicionado
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User; // Adicionado
import br.ifsp.domain.models.user.UserEntity; // Adicionado
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional; // Adicionado


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
            RentalEntity rentalEntity = rentalRepository.findById(request.rentalId())
                    .orElseThrow(() -> new IllegalArgumentException("Rental not found."));

            if (rentalEntity.getState() != RentalState.PENDING && rentalEntity.getState() != RentalState.DENIED) {
                throw new IllegalArgumentException("Rental state must be PENDING or DENIED");
            }

            rentalRepository.deleteById(rentalEntity.getId());

            var response = new ResponseModel(request.ownerId(), rentalEntity.getUserEntity().getId());
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
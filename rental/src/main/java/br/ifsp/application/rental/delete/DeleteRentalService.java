package br.ifsp.application.rental.delete;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import org.springframework.stereotype.Service;
import br.ifsp.application.user.repository.JpaUserRepository; // Adicionado
import java.time.Clock; // Adicionado


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
}
package br.ifsp.application.rental.delete;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import org.springframework.stereotype.Service;


@Service
public class DeleteRentalService implements IDeleteRentalService {

    private final JpaRentalRepository repository;

    public DeleteRentalService(JpaRentalRepository repository) {
        this.repository = repository;
    }

    @Override
    public void delete(DeleteRentalPresenter presenter, RequestModel request) {
        try {
            RentalEntity rentalEntity = repository.findById(request.rentalId())
                    .orElseThrow(() -> new IllegalArgumentException("Rental not found."));

            if (rentalEntity.getState() != RentalState.PENDING && rentalEntity.getState() != RentalState.DENIED) {
                throw new IllegalArgumentException("Rental state must be PENDING or DENIED");
            }

            repository.deleteById(rentalEntity.getId());

            var response = new ResponseModel(request.ownerId(), rentalEntity.getUser().getId());
            presenter.prepareSuccessView(response);
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }
}

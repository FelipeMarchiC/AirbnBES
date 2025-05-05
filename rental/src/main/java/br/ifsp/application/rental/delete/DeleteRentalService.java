package br.ifsp.application.rental.delete;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.Rental;
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
            Rental rental = repository.findById(request.rentalId())
                    .orElseThrow(() -> new IllegalArgumentException("Rental not found."));

            if (rental.getState() != RentalState.PENDING && rental.getState() != RentalState.DENIED) {
                throw new IllegalArgumentException("Rental state must be PENDING or DENIED");
            }

            repository.deleteById(rental.getId());

            var response = new ResponseModel(request.ownerId(), rental.getUser().getId());
            presenter.prepareSuccessView(response);
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }
}

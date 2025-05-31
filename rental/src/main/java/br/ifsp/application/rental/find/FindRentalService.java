package br.ifsp.application.rental.find;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.domain.models.rental.RentalEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindRentalService implements IFindRentalService {
    private final JpaRentalRepository jpaRentalRepository;

    public FindRentalService(JpaRentalRepository jpaRentalRepository) {
        this.jpaRentalRepository = jpaRentalRepository;
    }

    @Override
    public void getRentalHistoryByProperty(FindByPropertyIdRequestModel findByPropertyIdRequestModel, FindRentalPresenter presenter) {
        if (findByPropertyIdRequestModel == null || findByPropertyIdRequestModel.propertyId() == null)
            throw new IllegalArgumentException("propertyId cannot be null");

        try {
            List<RentalEntity> rentalEntityHistory = jpaRentalRepository.findByPropertyEntityId(findByPropertyIdRequestModel.propertyId());
            presenter.prepareSuccessView(new ResponseModel(rentalEntityHistory));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    public void getRentalHistoryByTenant(FindByTenantIdRequestModel requestModel, FindRentalPresenter presenter) {
        if (requestModel == null || requestModel.tenantId() == null)
            throw new IllegalArgumentException("tenantId cannot be null");

        try {
            List<RentalEntity> rentalEntityHistory = jpaRentalRepository.findByUserEntityId(requestModel.tenantId());
            presenter.prepareSuccessView(new ResponseModel(rentalEntityHistory));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    public void findAll(FindRentalPresenter presenter) {
        try {
            List<RentalEntity> allRentalEntities = jpaRentalRepository.findAll();
            if (allRentalEntities.isEmpty()) throw new EntityNotFoundException("There are no rentals registered");
            presenter.prepareSuccessView(new ResponseModel(allRentalEntities));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }
}

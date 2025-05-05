package br.ifsp.application.rental.find;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.Rental;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FindRentalService implements IFindRentalService{
    private final JpaRentalRepository jpaRentalRepository;

    public FindRentalService(JpaRentalRepository jpaRentalRepository) {
        this.jpaRentalRepository = jpaRentalRepository;
    }
    @Override
    public void getRentalHistoryByProperty(RequestModel requestModel, FindRentalPresenter presenter) {
        if (requestModel.propertyId() == null) throw new IllegalArgumentException("propertyId cannot be null");
        try{
            List<Rental> rentalHistory = jpaRentalRepository.findByPropertyId(requestModel.propertyId());
            presenter.prepareSuccessView(new ResponseModel(rentalHistory));
        }catch(Exception e){
            presenter.prepareFailView(e);
        }



    }

    public List<Rental> getRentalHistoryByTenant(UUID tenantId) {
        if (tenantId == null)
            throw new IllegalArgumentException("tenantId cannot be null");
        return jpaRentalRepository.findByUserId(tenantId);
    }
    public List<Rental> findAll() {
        return jpaRentalRepository.findAll();
    }

}

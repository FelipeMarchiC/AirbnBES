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
    public void getRentalHistoryByProperty(FindByPropertyIdRequestModel findByPropertyIdRequestModel, FindRentalPresenter presenter) {
        if (findByPropertyIdRequestModel.propertyId() == null) throw new IllegalArgumentException("propertyId cannot be null");
        try{
            List<Rental> rentalHistory = jpaRentalRepository.findByPropertyId(findByPropertyIdRequestModel.propertyId());
            presenter.prepareSuccessView(new ResponseModel(rentalHistory));
        }catch(Exception e){
            presenter.prepareFailView(e);
        }



    }

    public void getRentalHistoryByTenant(FindByTenantIdRequestModel requestModel, FindRentalPresenter presenter) {
        if (requestModel.tenantId() == null)
            throw new IllegalArgumentException("tenantId cannot be null");
        try{
            List<Rental> rentalHistory = jpaRentalRepository.findByUserId(requestModel.tenantId());
            presenter.prepareSuccessView(new IFindRentalService.ResponseModel(rentalHistory));
        }
        catch(Exception e){
            presenter.prepareFailView(e);

        }
    }
    public List<Rental> findAll() {
        return jpaRentalRepository.findAll();
    }

}

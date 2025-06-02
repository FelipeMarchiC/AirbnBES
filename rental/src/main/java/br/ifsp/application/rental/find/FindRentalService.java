package br.ifsp.application.rental.find;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.repository.RentalMapper;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalEntity;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

@Service
public class FindRentalService implements IFindRentalService {
    private final JpaRentalRepository jpaRentalRepository;
    private final Clock clock;

    public FindRentalService(JpaRentalRepository jpaRentalRepository, Clock clock) {
        this.jpaRentalRepository = jpaRentalRepository;
        this.clock= clock;
    }

    @Override
    public void getRentalHistoryByProperty(FindByPropertyIdRequestModel findByPropertyIdRequestModel, FindRentalPresenter presenter) {
        if (findByPropertyIdRequestModel == null || findByPropertyIdRequestModel.propertyId() == null)
            throw new IllegalArgumentException("propertyId cannot be null");

        try {
            List<RentalEntity> rentalEntityHistory = jpaRentalRepository.findByPropertyEntityId(findByPropertyIdRequestModel.propertyId());
            List<Rental> rentals = new ArrayList<>();
            rentalEntityHistory.forEach(rentalEntity ->
                    rentals.add(RentalMapper.toDomain(rentalEntity,clock))
                    );
            presenter.prepareSuccessView(new ResponseModel(rentals));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    public void getRentalHistoryByTenant(FindByTenantIdRequestModel requestModel, FindRentalPresenter presenter) {
        if (requestModel == null || requestModel.tenantId() == null)
            throw new IllegalArgumentException("tenantId cannot be null");

        try {
            List<RentalEntity> rentalEntityHistory = jpaRentalRepository.findByUserEntityId(requestModel.tenantId());
            List<Rental> rentals = new ArrayList<>();
            rentalEntityHistory.forEach(rentalEntity ->
                    rentals.add(RentalMapper.toDomain(rentalEntity,clock))
            );
            presenter.prepareSuccessView(new ResponseModel(rentals));

        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    public void findAll(FindRentalPresenter presenter) {
        try {
            List<RentalEntity> allRentalEntities = jpaRentalRepository.findAll();
            List<Rental> allRentals = new ArrayList<>();
            allRentalEntities.forEach(rentalEntity ->
                    allRentals.add(RentalMapper.toDomain(rentalEntity,clock)));
            if (allRentalEntities.isEmpty()) throw new EntityNotFoundException("There are no rentals registered");
            presenter.prepareSuccessView(new ResponseModel(allRentals));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }
}

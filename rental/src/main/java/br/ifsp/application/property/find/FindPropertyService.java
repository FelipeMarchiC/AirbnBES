package br.ifsp.application.property.find;

import br.ifsp.application.property.repository.JpaPropertyRepository;
import br.ifsp.application.property.repository.PropertyMapper;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.property.PropertyEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindPropertyService implements IFindPropertyService {
    private final JpaPropertyRepository propertyRepository;

    public FindPropertyService(JpaPropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    public void findById(FindPropertyByIdPresenter presenter, FindByIdRequestModel request) {
        try {
            if (request.propertyId() == null) {
                throw new IllegalArgumentException("PropertyId cannot be null");
            }

            PropertyEntity propertyEntity = propertyRepository
                    .findById(request.propertyId())
                    .orElseThrow(() -> new EntityNotFoundException("Property not found"));

            presenter.prepareSuccessView(new PropertyResponseModel(PropertyMapper.toDomain(propertyEntity)));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    @Override
    public void findByLocation(FindPropertyPresenter presenter, LocationRequestModel request) {
        try {
            if (request.location() == null || request.location().isBlank()) {
                throw new IllegalArgumentException("Location cannot be null or blank");
            }

            List<Property> properties = propertyRepository.findByLocation(request.location())
                    .stream()
                    .map(PropertyMapper::toDomain)
                    .toList();

            presenter.prepareSuccessView(new PropertyListResponseModel(properties));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }


    @Override
    public void findByPriceRange(FindPropertyPresenter presenter, PriceRangeRequestModel request) {
        try {
            if (request.min() < 0 || request.max() < 0) {
                throw new IllegalArgumentException("Prices must be non-negative");
            }

            if (request.min() > request.max()) {
                throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
            }

            List<Property> properties = propertyRepository
                    .findByDailyRateBetween(request.min(), request.max())
                    .stream()
                    .map(PropertyMapper::toDomain)
                    .toList();

            presenter.prepareSuccessView(new PropertyListResponseModel(properties));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }
    @Override
    public void findByPeriod(FindPropertyPresenter presenter, PeriodRequestModel request){
        try{
            List<Property> properties = propertyRepository
                    .findAvailablePropertiesByPeriod(request.startDate(), request.endDate())
                    .stream()
                    .map(PropertyMapper::toDomain)
                    .toList();

            if(properties.isEmpty()) throw new EntityNotFoundException();

            presenter.prepareSuccessView(new PropertyListResponseModel(properties));
        }catch(Exception e){
            presenter.prepareFailView(e);
        }
    }

    @Override
    public void findAll(FindPropertyPresenter presenter) {
        try {
            List<Property> properties = propertyRepository.findAll()
                    .stream()
                    .map(PropertyMapper::toDomain)
                    .toList();

            presenter.prepareSuccessView(new PropertyListResponseModel(properties));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }
}

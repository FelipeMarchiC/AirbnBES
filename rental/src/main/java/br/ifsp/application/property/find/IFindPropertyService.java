package br.ifsp.application.property.find;

import br.ifsp.domain.models.property.Property;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IFindPropertyService {
    void findById(FindPropertyByIdPresenter presenter, FindByIdRequestModel request);
    void findByLocation(FindPropertyPresenter presenter, LocationRequestModel request);
    void findByPriceRange(FindPropertyPresenter presenter, PriceRangeRequestModel request);
    void findAll(FindPropertyPresenter presenter);

    void findByPeriod(FindPropertyPresenter presenter, PeriodRequestModel request);

    record FindByIdRequestModel(UUID propertyId) {}
    record LocationRequestModel(String location) {}
    record PriceRangeRequestModel(double min, double max) {}
    record PropertyListResponseModel(List<Property> properties) {}
    record PropertyResponseModel(Property property) {}

    record PeriodRequestModel(LocalDate startDate, LocalDate endDate){}
}

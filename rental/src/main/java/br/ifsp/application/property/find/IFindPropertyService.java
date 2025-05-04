package br.ifsp.application.property.find;

import br.ifsp.domain.models.property.Property;

import java.time.LocalDate;
import java.util.List;

public interface IFindPropertyService {
    void findByLocation(FindPropertyPresenter presenter, LocationRequestModel request);
    void findByPriceRange(FindPropertyPresenter presenter, PriceRangeRequestModel request);
    void findAll(FindPropertyPresenter presenter);

    void findByPeriod(FindPropertyPresenter presenter, PeriodRequestModel request);

    record LocationRequestModel(String location) {}
    record PriceRangeRequestModel(double min, double max) {}
    record PropertyListResponseModel(List<Property> properties) {}

    record PeriodRequestModel(LocalDate startDate, LocalDate endDate){}
}

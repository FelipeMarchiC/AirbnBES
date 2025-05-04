package br.ifsp.application.property.find;

import br.ifsp.domain.models.property.Property;

import java.util.List;

public interface IFindPropertyService {
    void findByLocation(FindPropertyPresenter presenter, LocationRequestModel request);
    void findByPriceRange(FindPropertyPresenter presenter, PriceRangeRequestModel request);
    void findAll(FindPropertyPresenter presenter);

    record LocationRequestModel(String location) {}
    record PriceRangeRequestModel(double min, double max) {}
    record PropertyListResponseModel(List<Property> properties) {}
}

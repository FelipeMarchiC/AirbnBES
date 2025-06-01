package br.ifsp.vvts.property.requests;

import br.ifsp.application.property.find.IFindPropertyService;

import java.util.UUID;

public record GetRequest() {
    public IFindPropertyService.FindByIdRequestModel toFindByPropertyId(UUID propertyId) {
        return new IFindPropertyService.FindByIdRequestModel(propertyId);
    }

    public IFindPropertyService.LocationRequestModel toFindLocationRequestModel(String location) {
        return new IFindPropertyService.LocationRequestModel(location);
    }

    public IFindPropertyService.PriceRangeRequestModel toFindRangeRequestModel(double min, double max) {
        return new IFindPropertyService.PriceRangeRequestModel(min, max);
    }
}


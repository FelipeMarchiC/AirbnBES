package br.ifsp.vvts.property.requests;

import br.ifsp.application.property.find.IFindPropertyService;

public record GetRequest() {
    public IFindPropertyService.LocationRequestModel toFindLocationRequestModel(String location) {
        return new IFindPropertyService.LocationRequestModel(location);
    }

    public IFindPropertyService.PriceRangeRequestModel toFindRangeRequestModel(double min, double max) {
        return new IFindPropertyService.PriceRangeRequestModel(min, max);
    }
}


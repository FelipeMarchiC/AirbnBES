package br.ifsp.vvts.property.controller;

import br.ifsp.application.property.find.IFindPropertyService;
import br.ifsp.vvts.property.presenter.RestFindPropertyPresenter;
import br.ifsp.vvts.property.requests.GetRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/property")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
@Tag(name = "Property Routes")
public class PropertyController {
    private final IFindPropertyService findPropertyService;

    @GetMapping
    public ResponseEntity<?> FindAllProperties() {
        var presenter = new RestFindPropertyPresenter();

        findPropertyService.findAll(presenter);

        return presenter.responseEntity() != null ?
                presenter.responseEntity()
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("/location")
    public ResponseEntity<?> FindByLocation(@RequestParam String location) {
        var presenter = new RestFindPropertyPresenter();
        var request = new GetRequest();
        var requestModel = request.toFindLocationRequestModel(location);

        findPropertyService.findByLocation(presenter, requestModel);

        return presenter.responseEntity() != null ?
                presenter.responseEntity()
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("/price-range")
    public ResponseEntity<?> FindByPriceRange(@RequestParam double min, @RequestParam double max) {
        var presenter = new RestFindPropertyPresenter();
        var request = new GetRequest();
        var requestModel = request.toFindRangeRequestModel(min, max);

        findPropertyService.findByPriceRange(presenter, requestModel);

        return presenter.responseEntity() != null ?
                presenter.responseEntity()
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
package br.ifsp.vvts.property.presenter;

import br.ifsp.application.property.find.FindPropertyPresenter;
import br.ifsp.application.property.find.IFindPropertyService;
import br.ifsp.domain.models.property.Property;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

import static br.ifsp.vvts.shared.error.ErrorResponseFactory.createErrorResponseFrom;

public class RestFindPropertyPresenter implements FindPropertyPresenter {
    private ResponseEntity<?> responseEntity;

    @Override
    public void prepareSuccessView(IFindPropertyService.PropertyListResponseModel response) {
        List<Property> properties = response.properties();

        List<String> propertyNames = properties.stream()
                .map(Property::getName)
                .collect(Collectors.toList());

        List<String> propertyDescriptions = properties.stream()
                .map(Property::getDescription)
                .collect(Collectors.toList());

        List<Double> propertyDailyRates = properties.stream()
                .map(property -> property.getDailyRate().getAmount().doubleValue())
                .collect(Collectors.toList());

        List<String> ownerNames = properties.stream()
                .map(property -> property.getOwner().getUsername())
                .collect(Collectors.toList());

        ViewModel viewModel = new ViewModel(
                propertyNames,
                propertyDescriptions,
                propertyDailyRates,
                ownerNames
        );

        this.responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(viewModel);
    }

    @Override
    public void prepareFailView(Throwable throwable) {
        this.responseEntity = createErrorResponseFrom(throwable);
    }

    @Override
    public boolean isDone() {
        return responseEntity != null;
    }

    public ResponseEntity<?> responseEntity() {
        return responseEntity;
    }

    private record ViewModel(
            List<String> propertyNames,
            List<String> propertyDescriptions,
            List<Double> propertyDailyRates,
            List<String> ownerNames
    ) {}
}

package br.ifsp.vvts.property.presenter;

import br.ifsp.application.property.find.FindPropertyPresenter;
import br.ifsp.application.property.find.IFindPropertyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static br.ifsp.vvts.shared.error.ErrorResponseFactory.createErrorResponseFrom;

public class RestFindPropertyPresenter implements FindPropertyPresenter {
    private ResponseEntity<?> responseEntity;

    @Override
    public void prepareSuccessView(IFindPropertyService.PropertyListResponseModel response) {
        List<PropertyViewModel> viewModelList = response.properties().stream()
                .map(property -> new PropertyViewModel(
                        property.getId(),
                        property.getName(),
                        property.getDescription(),
                        property.getDailyRate().getAmount().doubleValue(),
                        property.getAddress().getCity(),
                        property.getAddress().getState(),
                        property.getOwner().getName() + " " + property.getOwner().getLastname(),
                        property.getOwner().getId()
                ))
                .toList();

        this.responseEntity = ResponseEntity.status(HttpStatus.OK).body(viewModelList);
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

    private record PropertyViewModel(
            UUID id,
            String name,
            String description,
            Double dailyRate,
            String city,
            String state,
            String ownerName,
            UUID ownerId
    ) {}
}
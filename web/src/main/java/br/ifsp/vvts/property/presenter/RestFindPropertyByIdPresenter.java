package br.ifsp.vvts.property.presenter;

import br.ifsp.application.property.find.FindPropertyByIdPresenter;
import br.ifsp.application.property.find.IFindPropertyService;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static br.ifsp.vvts.shared.error.ErrorResponseFactory.createErrorResponseFrom;

public class RestFindPropertyByIdPresenter implements FindPropertyByIdPresenter {
    private ResponseEntity<?> responseEntity;

    @Override
    public void prepareSuccessView(IFindPropertyService.PropertyResponseModel response) {
        val property = response.property();

        PropertyViewModel viewModel = new PropertyViewModel(
                property.getId(),
                property.getName(),
                property.getDescription(),
                property.getDailyRate().getAmount().doubleValue(),
                property.getAddress().getCity(),
                property.getAddress().getState(),
                property.getOwner().getName() + " " + property.getOwner().getLastname(),
                property.getOwner().getId()
        );

        this.responseEntity = ResponseEntity.status(HttpStatus.OK).body(viewModel);
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
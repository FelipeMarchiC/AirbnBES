package br.ifsp.vvts.property.presenter;

import br.ifsp.application.property.find.FindPropertyPresenter;
import br.ifsp.application.property.find.IFindPropertyService;
import br.ifsp.domain.models.property.Property;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static br.ifsp.vvts.shared.error.ErrorResponseFactory.createErrorResponseFrom;

public class RestFindPropertyPresenter implements FindPropertyPresenter {
    private ResponseEntity<?> responseEntity;

    @Override
    public void prepareSuccessView(IFindPropertyService.PropertyListResponseModel response) {
        ViewModel viewModel = new ViewModel(response.properties());

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

    public ResponseEntity<?> responseEntity() { return responseEntity; }

    private record ViewModel(List<Property> properties) {}
}
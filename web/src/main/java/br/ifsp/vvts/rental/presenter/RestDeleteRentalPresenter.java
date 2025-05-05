package br.ifsp.vvts.rental.presenter;

import br.ifsp.application.rental.delete.DeleteRentalPresenter;
import br.ifsp.application.rental.delete.IDeleteRentalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static br.ifsp.vvts.shared.error.ErrorResponseFactory.createErrorResponseFrom;

public class RestDeleteRentalPresenter implements DeleteRentalPresenter {
    private ResponseEntity<?> responseEntity;

    @Override
    public void prepareSuccessView(IDeleteRentalService.ResponseModel response) {
        var viewModel = new ViewModel(response.ownerId(), response.tenantId());
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

    private record ViewModel(UUID ownerId, UUID tenantId) {}
}

package br.ifsp.vvts.rental.presenter;

import br.ifsp.application.rental.update.tenant.ITenantUpdateRentalService;
import br.ifsp.application.rental.update.tenant.TenantUpdateRentalPresenter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static br.ifsp.vvts.shared.error.ErrorResponseFactory.createErrorResponseFrom;

public class RestTenantUpdateRentalPresenter implements TenantUpdateRentalPresenter {
    private ResponseEntity<?> responseEntity;

    @Override
    public void prepareSuccessView(ITenantUpdateRentalService.ResponseModel response) {
        RestTenantUpdateRentalPresenter.ViewModel viewModel = new RestTenantUpdateRentalPresenter.ViewModel(
                response.rentalId(),
                response.tenantId()
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
            UUID rentalId,
            UUID tenantId
    ){}
}

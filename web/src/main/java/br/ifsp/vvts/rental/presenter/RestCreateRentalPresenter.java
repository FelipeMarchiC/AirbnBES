package br.ifsp.vvts.rental.presenter;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import br.ifsp.application.rental.create.CreateRentalPresenter;
import br.ifsp.application.rental.create.ICreateRentalService.ResponseModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static br.ifsp.vvts.shared.error.ErrorResponseFactory.createErrorResponseFrom;

@Component
public class RestCreateRentalPresenter implements CreateRentalPresenter {
    private ResponseEntity<?> responseEntity;

    @Override
    public void prepareSuccessView(ResponseModel response) {
        ViewModel viewModel = new ViewModel(
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

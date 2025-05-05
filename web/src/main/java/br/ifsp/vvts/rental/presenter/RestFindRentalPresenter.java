package br.ifsp.vvts.rental.presenter;

import br.ifsp.application.rental.find.FindRentalPresenter;
import br.ifsp.application.rental.find.IFindRentalService;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pitest.fasterxml.jackson.core.JsonFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static br.ifsp.vvts.shared.error.ErrorResponseFactory.createErrorResponseFrom;

public class RestFindRentalPresenter implements FindRentalPresenter {
    private ResponseEntity<?> responseEntity;

    @Override
    public void prepareSuccessView(IFindRentalService.ResponseModel response) {
        List<RentalViewModel> viewModelList = response.rentalList().stream()
                .map(rental -> new RentalViewModel(
                        rental.getId(),
                        rental.getUser().getUsername(),
                        rental.getProperty().getName(),
                        rental.getStartDate(),
                        rental.getEndDate(),
                        rental.getValue().getAmount().doubleValue()
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

    private record RentalViewModel(
            UUID id,
            String username,
            String propertyName,
            LocalDate startDate,
            LocalDate endDate,
            Double price
    ) {}
}

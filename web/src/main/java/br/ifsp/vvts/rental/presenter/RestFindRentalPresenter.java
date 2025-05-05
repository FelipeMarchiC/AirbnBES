package br.ifsp.vvts.rental.presenter;

import br.ifsp.application.rental.find.FindRentalPresenter;
import br.ifsp.application.rental.find.IFindRentalService;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static br.ifsp.vvts.shared.error.ErrorResponseFactory.createErrorResponseFrom;
@NoArgsConstructor
public class RestFindRentalPresenter implements FindRentalPresenter {
    private ResponseEntity<?> responseEntity;

    private final List<UUID> rentalIdList = new ArrayList<>();
    private final List<String> userNameList = new ArrayList<>();
    private final List<String> propertyNameList = new ArrayList<>();
    private final List<LocalDate> startDateList = new ArrayList<>();
    private final List<LocalDate> endDateList = new ArrayList<>();
    private final List<Double> prices = new ArrayList<>();


    @Override
    public void prepareSuccessView(IFindRentalService.ResponseModel response) {
        response.rentalList().forEach(rental -> rentalIdList.add(rental.getId()));
        response.rentalList().forEach(rental -> userNameList.add(rental.getUser().getUsername()));
        response.rentalList().forEach(rental -> propertyNameList.add(rental.getProperty().getName()));
        response.rentalList().forEach(rental -> startDateList.add(rental.getStartDate()));
        response.rentalList().forEach(rental -> endDateList.add(rental.getEndDate()));
        response.rentalList().forEach(rental -> prices.add(rental.getValue().getAmount().doubleValue()));

        RestFindRentalPresenter.ViewModel viewModel = new RestFindRentalPresenter.ViewModel(
                rentalIdList,
                userNameList,
                propertyNameList,
                startDateList,
                endDateList,
                prices
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
            List<UUID> rentalIdList,

            List<String> userNameList,
            List<String> propertyNameList,
            List<LocalDate> startDateList,
            List<LocalDate> endDateList,
            List<Double> prices

    ) {}
}

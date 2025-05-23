package br.ifsp.application.rental.create;

import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.repository.RentalMapper;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.shared.presenter.PreconditionChecker;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.services.IUuidGeneratorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class CreateRentalService implements ICreateRentalService {
    private final JpaUserRepository userRepository;
    private final JpaPropertyRepository propertyRepository;
    private final JpaRentalRepository rentalRepository;
    private final IUuidGeneratorService uuidGeneratorService;
    private final Clock clock;

    public CreateRentalService(
            JpaUserRepository userRepository,
            JpaPropertyRepository propertyRepository,
            JpaRentalRepository rentalRepository,
            IUuidGeneratorService uuidGeneratorService,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.rentalRepository = rentalRepository;
        this.uuidGeneratorService = uuidGeneratorService;
        this.clock = clock;
    }

    @Override
    public void registerRental(CreateRentalPresenter presenter, RequestModel request) {
        User user = userRepository.findById(request.userId()).orElse(null);
        PreconditionChecker.prepareIfFailsPreconditions(presenter, user);
        if (presenter.isDone()) return;

        try {
            validateRequestedDates(request.startDate(), request.endDate());

            Property property = propertyRepository.findById(request.propertyId())
                    .orElseThrow(() -> new EntityNotFoundException("Property not found"));

            validateOverlappingDates(request.startDate(), request.endDate(), property);

            Rental rental = rentalRepository.save(buildRental(request, user, property));

            presenter.prepareSuccessView(
                    new ResponseModel(
                            rental.getId(),
                            user.getId()
                    )
            );
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }


    private Rental buildRental(RequestModel requestModel, User user, Property property) {
        long days = ChronoUnit.DAYS.between(requestModel.startDate(), requestModel.endDate());
        BigDecimal totalCost = property.getDailyRate().getAmount().multiply(BigDecimal.valueOf(days));

        return RentalMapper.fromCreateRequestModel(
                uuidGeneratorService.generate(),
                requestModel,
                user,
                property,
                totalCost
        );
    }

    private void validateRequestedDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isEqual(endDate)) {
            throw new IllegalArgumentException("Rental cannot have the same date to start and end");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        if (startDate.isBefore(LocalDate.now(clock))) {
            throw new IllegalArgumentException("Rental cannot start in the past.");
        }

        if (endDate.isAfter(startDate.plusYears(1))) {
            throw new IllegalArgumentException("Rental duration must be 1 year or less");
        }
    }

    private static void validateOverlappingDates(LocalDate startDate, LocalDate endDate, Property property) {
        for (Rental existingRental : property.getRentals()) {
            if (!existingRental.getState().equals(RentalState.CONFIRMED)) {
                continue;
            }

            boolean isBefore = endDate.isBefore(existingRental.getStartDate());
            boolean isAfter = startDate.isAfter(existingRental.getEndDate());

            boolean overlaps = !(isBefore || isAfter);
            if (overlaps) {
                throw new IllegalArgumentException("Property is already rented during the requested period");
            }
        }
    }
}

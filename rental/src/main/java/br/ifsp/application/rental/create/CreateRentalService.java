package br.ifsp.application.rental.create;

import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.repository.RentalMapper;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.shared.presenter.PreconditionChecker;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.RentalEntity;
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

            PropertyEntity propertyEntity = propertyRepository.findById(request.propertyId())
                    .orElseThrow(() -> new EntityNotFoundException("Property not found"));

            validateOverlappingDates(request.startDate(), request.endDate(), propertyEntity);

            RentalEntity rentalEntity = rentalRepository.save(buildRental(request, user, propertyEntity));

            presenter.prepareSuccessView(
                    new ResponseModel(
                            rentalEntity.getId(),
                            user.getId()
                    )
            );
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }


    private RentalEntity buildRental(RequestModel requestModel, User user, PropertyEntity propertyEntity) {
        long days = ChronoUnit.DAYS.between(requestModel.startDate(), requestModel.endDate());
        BigDecimal totalCost = propertyEntity.getDailyRate().getAmount().multiply(BigDecimal.valueOf(days));

        return RentalMapper.fromCreateRequestModel(
                uuidGeneratorService.generate(),
                requestModel,
                user,
                propertyEntity,
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

    private static void validateOverlappingDates(LocalDate startDate, LocalDate endDate, PropertyEntity propertyEntity) {
        for (RentalEntity existingRentalEntity : propertyEntity.getRentalEntities()) {
            if (!existingRentalEntity.getState().equals(RentalState.CONFIRMED)) {
                continue;
            }

            boolean isBefore = endDate.isBefore(existingRentalEntity.getStartDate());
            boolean isAfter = startDate.isAfter(existingRentalEntity.getEndDate());

            boolean overlaps = !(isBefore || isAfter);
            if (overlaps) {
                throw new IllegalArgumentException("Property is already rented during the requested period");
            }
        }
    }
}

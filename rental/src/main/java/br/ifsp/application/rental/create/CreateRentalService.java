package br.ifsp.application.rental.create;

import br.ifsp.application.property.repository.JpaPropertyRepository;
import br.ifsp.application.property.repository.PropertyMapper;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.repository.RentalMapper;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.shared.presenter.PreconditionChecker;
import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.application.user.repository.UserMapper;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.domain.services.IUuidGeneratorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

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
        User user = getUser(request).orElse(null);
        PreconditionChecker.prepareIfFailsPreconditions(presenter, user);
        if (presenter.isDone()) return;

        try {
            validateRequestedDates(request.startDate(), request.endDate());

            PropertyEntity propertyEntity = propertyRepository.findById(request.propertyId())
                    .orElseThrow(() -> new EntityNotFoundException("Property not found"));

            Property property = PropertyMapper.toDomain(propertyEntity, clock);

            validateOverlappingDates(request.startDate(), request.endDate(), property);

            Rental rental = buildRental(request, user, property);

            rentalRepository.save(RentalMapper.toEntity(rental));

            presenter.prepareSuccessView(new ResponseModel(rental.getId(), user.getId()));
        } catch (Exception e) {
            presenter.prepareFailView(e);
        }
    }

    private Optional<User> getUser(RequestModel request) {
        UserEntity userEntity = userRepository.findById(request.userId()).orElse(null);

        return userEntity != null ? Optional.of(UserMapper.toDomain(userEntity)) : Optional.empty();
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

    static void validateOverlappingDates(LocalDate startDate, LocalDate endDate, Property property) {
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

    private Rental buildRental(RequestModel request, User user, Property property) {
        long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
        BigDecimal totalCost = property.getDailyRate().getAmount().multiply(BigDecimal.valueOf(days));

        return RentalMapper.fromCreateRequestModel(
                uuidGeneratorService.generate(),
                request,
                user,
                property,
                totalCost,
                clock
        );
    }
}

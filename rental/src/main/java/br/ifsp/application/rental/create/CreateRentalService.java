package br.ifsp.application.rental.create;

import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

public class CreateRentalService implements ICreateRentalService {
    private final JpaUserRepository userRepository;
    private final JpaPropertyRepository propertyRepository;
    private final JpaRentalRepository rentalRepository;
    private final Clock clock;

    public CreateRentalService(
            JpaUserRepository userRepository,
            JpaPropertyRepository propertyRepository,
            JpaRentalRepository rentalRepository,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.rentalRepository = rentalRepository;
        this.clock = clock;
    }

    @Override
    public Rental registerRental(UUID userId, UUID propertyId, LocalDate startDate, LocalDate endDate) {
        CheckForRequestedDatesValidity(startDate, endDate);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        CheckForOverlappingDates(startDate, endDate, property);

        Rental rental = new Rental(UUID.randomUUID(), user, property, startDate, endDate, RentalState.PENDING);

        return rentalRepository.save(rental);
    }

    private void CheckForRequestedDatesValidity(LocalDate startDate, LocalDate endDate) {
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

    private static void CheckForOverlappingDates(LocalDate startDate, LocalDate endDate, Property property) {
        for (Rental existingRental : property.getRentals()) {
            if (!existingRental.getState().equals(RentalState.CONFIRMED)) {
                continue;
            }

            boolean overlaps = !(endDate.isBefore(existingRental.getStartDate()) || startDate.isAfter(existingRental.getEndDate()));
            if (overlaps) {
                throw new IllegalArgumentException("Property is already rented during the requested period");
            }
        }
    }
}

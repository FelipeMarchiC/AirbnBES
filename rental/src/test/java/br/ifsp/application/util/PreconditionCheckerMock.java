package br.ifsp.application.util;

import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.shared.exceptions.UnauthenticatedUserException;
import br.ifsp.application.shared.presenter.GenericPresenter;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import com.github.javafaker.Faker;
import lombok.Getter;
import lombok.val;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;

public class PreconditionCheckerMock {
    private final GenericPresenter<?> presenter;
    private final JpaUserRepository userRepository;
    private final JpaRentalRepository rentalRepository;
    private final JpaPropertyRepository propertyRepository;
    private final UUID rentalId;
    private final UUID tenantId;
    private final UUID ownerId;
    private final UUID propertyId;

    private final Faker faker = new Faker();
    @Getter private final Rental rental;

    public PreconditionCheckerMock(
            GenericPresenter<?> presenter,
            JpaRentalRepository jpaRentalRepository,
            JpaUserRepository jpaUserRepository,
            JpaPropertyRepository jpaPropertyRepository,
            UUID rentalId,
            UUID tenantId,
            UUID ownerId,
            UUID propertyId
    ) {
        this.presenter = presenter;
        this.rentalRepository = jpaRentalRepository;
        this.userRepository = jpaUserRepository;
        this.propertyRepository = jpaPropertyRepository;
        this.rentalId = rentalId;
        this.tenantId = tenantId;
        this.ownerId = ownerId;
        this.propertyId = propertyId;
        this.rental = generateRental();
    }

    public void allMocksWorksAsExpected() {
        when(userRepository.findById(tenantId)).thenReturn(Optional.ofNullable(rental.getUser()));
        when(userRepository.findById(ownerId)).thenReturn(Optional.ofNullable(rental.getProperty().getOwner()));
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(rental.getProperty()));
        when(presenter.isDone()).thenReturn(false);
    }

    public void makeUserUnauthenticated() {
        when(userRepository.findById(tenantId)).thenReturn(Optional.ofNullable(rental.getUser()));
        when(userRepository.findById(ownerId)).thenReturn(Optional.ofNullable(rental.getProperty().getOwner()));
//        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
//        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(rental.getProperty()));
        when(presenter.isDone()).thenReturn(true);
    }

    public void makeRentalNonexistent() {
        when(userRepository.findById(tenantId)).thenReturn(Optional.ofNullable(rental.getUser()));
        when(userRepository.findById(ownerId)).thenReturn(Optional.ofNullable(rental.getProperty().getOwner()));
        when(rentalRepository.findById(rentalId)).thenReturn(null);
        when(presenter.isDone()).thenReturn(false).thenReturn(true);
    }

    public <T> void testForUnauthenticatedUser(GenericPresenter<?> presenter, T request, ServiceFunction<T> service) {
        makeUserUnauthenticated();
        service.execute(presenter, request);
        verify(presenter).prepareFailView(isA(UnauthenticatedUserException.class));
        verify(presenter).isDone();
    }

    public <T> void testForNonexistentRental(GenericPresenter<?> presenter, T request, ServiceFunction<T> service) {
        makeRentalNonexistent();
        service.execute(presenter, request);
        verify(presenter).isDone();
        verify(presenter).prepareFailView(isA(EntityNotFoundException.class));
    }


    private Rental generateRental() {
        val tenant = generateUser(tenantId);
        val owner = generateUser(ownerId);
        val property = generateProperty(owner);

        LocalDate startDate = LocalDate.parse("2025-01-01");
        LocalDate endDate = LocalDate.parse("2025-01-08");
        BigDecimal value = BigDecimal.valueOf(1850.99);

        return Rental.builder()
                .id(rentalId)
                .user(tenant)
                .property(property)
                .startDate(startDate)
                .endDate(endDate)
                .value(new Price(value))
                .state(RentalState.CONFIRMED)
                .build();
    }

    private User generateUser(UUID userId) {
        return User.builder()
                .id(userId)
                .name(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .password(faker.internet().password())
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();
    }

    private Property generateProperty(User owner) {
        return Property.builder()
                .id(propertyId)
                .name(faker.name().name())
                .description(faker.lorem().paragraph())
                .dailyRate(new Price(BigDecimal.valueOf(faker.number().randomNumber())))
                .address(Address.builder()
                        .number(faker.address().buildingNumber())
                        .street(faker.address().streetAddress())
                        .city(faker.address().city())
                        .state(faker.address().state())
                        .postalCode(faker.address().zipCode())
                        .build())
                .owner(owner)
                .rentals(new ArrayList<>())
                .build();
    }

    @FunctionalInterface
    public interface ServiceFunction<T> {
        void execute(GenericPresenter<?> presenter, T requestModel);
    }
}
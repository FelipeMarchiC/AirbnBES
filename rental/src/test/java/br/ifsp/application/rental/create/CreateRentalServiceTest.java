package br.ifsp.application.rental.create;

import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateRentalServiceTest {
    @Mock private JpaUserRepository userRepositoryMock;
    @Mock private JpaPropertyRepository propertyRepositoryMock;
    @Mock private JpaRentalRepository rentalRepositoryMock;
    @InjectMocks private CreateRentalService sut;

    private AutoCloseable closeable;

    private User owner;
    private User tenant;
    private Property property;

    @BeforeEach
    void setupService() {
        closeable = MockitoAnnotations.openMocks(this);

        Clock fixedClock = Clock.fixed(
                LocalDate.of(1801, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant(),
                ZoneOffset.UTC
        );

        sut = new CreateRentalService(
                userRepositoryMock,
                propertyRepositoryMock,
                rentalRepositoryMock,
                fixedClock
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setup() {
        tenant = User.builder()
                .id(UUID.fromString("e924925c-2a7b-4cab-b938-0d6398ecc78a"))
                .name("Hindley")
                .lastname("Earnshaw")
                .email("hindleyearnshaw@outlook.com")
                .password("bGV0c2dvZ2FtYmxpbmc=")
                .role(Role.USER)
                .ownedProperties(new ArrayList<>())
                .build();

        property = Property.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .name("Wuthering Heights")
                .description("An isolated farmhouse on the Yorkshire moors.")
                .dailyRate(new Price(new BigDecimal("250.00")))
                .address(Address.builder()
                        .number("1")
                        .street("Moor Lane")
                        .city("Haworth")
                        .state("West Yorkshire")
                        .postalCode("BD22")
                        .build())
                .owner(owner)
                .rentals(new ArrayList<>())
                .build();

        owner = User.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .name("Catherine")
                .lastname("Earnshaw")
                .email("cathy_earnshaw@outlook.com")
                .password("ZGVsZXRlYWxsY2F0aHk=")
                .role(Role.USER)
                .ownedProperties(List.of(property))
                .build();
    }


    @Nested
    @Tag("UnitTest")
    @DisplayName("Testing valid equivalent classes")
    class TestingValidEquivalentClasses {
        @Tag("TDD")
        @ParameterizedTest(name = "[{index}]: should create rental from {2} to {3}")
        @CsvSource({
                "e924925c-2a7b-4cab-b938-0d6398ecc78a, 123e4567-e89b-12d3-a456-426614174000, 1801-02-22, 1801-03-22"
        })
        @DisplayName("Should create rental when start date is before endDate")
        void shouldCreateRentalWhenStartDateIsBeforeEndDate(
                UUID userId,
                UUID propertyId,
                LocalDate startDate,
                LocalDate endDate
        ) {
            when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(tenant));
            when(propertyRepositoryMock.findById(propertyId)).thenReturn(Optional.of(property));
            when(rentalRepositoryMock.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Rental rental = sut.registerRental(userId, propertyId, startDate, endDate);

            assertThat(rental).isNotNull();
            assertThat(rental.getUser()).isEqualTo(tenant);
            assertThat(rental.getProperty()).isEqualTo(property);
            assertThat(rental.getStartDate()).isEqualTo(startDate);
            assertThat(rental.getEndDate()).isEqualTo(endDate);
            assertThat(rental.getStartDate()).isBefore(rental.getEndDate());
            assertThat(rental.getState()).isEqualTo(RentalState.PENDING);
        }

        @Tag("TDD")
        @ParameterizedTest(name = "[{index}]: should calculate rental total value from {2} to {3}")
        @CsvSource({
                "e924925c-2a7b-4cab-b938-0d6398ecc78a, 123e4567-e89b-12d3-a456-426614174000, 1801-02-22, 1801-02-23, ",
                "e924925c-2a7b-4cab-b938-0d6398ecc78a, 123e4567-e89b-12d3-a456-426614174000, 1801-02-22, 1802-02-22",
        })
        @DisplayName("Should calculate rental total value")
        void shouldCalculateRentalTotalValue(
                UUID userId,
                UUID propertyId,
                LocalDate startDate,
                LocalDate endDate
        ) {
            when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(tenant));
            when(propertyRepositoryMock.findById(propertyId)).thenReturn(Optional.of(property));
            when(rentalRepositoryMock.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

            long daysRented = ChronoUnit.DAYS.between(startDate, endDate);
            BigDecimal totalPrice = property.getDailyRate().getAmount().multiply(BigDecimal.valueOf(daysRented));

            Rental rental = sut.registerRental(userId, propertyId, startDate, endDate);

            assertThat(rental.getValue().getAmount()).isEqualTo(totalPrice);
        }
    }

    @Nested
    @Tag("UnitTest")
    @DisplayName("Testing invalid equivalent classes")
    class TestingInvalidEquivalentClasses {
        @Tag("TDD")
        @ParameterizedTest(
                name = "[{index}]: has rental from {2} to {3} and is requested from {4} to {5}"
        )
        @CsvSource({
                "e924925c-2a7b-4cab-b938-0d6398ecc78a, 123e4567-e89b-12d3-a456-426614174000, 1801-02-22, 1801-03-22, 1801-02-22, 1801-03-22",
                "e924925c-2a7b-4cab-b938-0d6398ecc78a, 123e4567-e89b-12d3-a456-426614174000, 1801-02-22, 1801-04-30, 1801-04-30, 1801-05-10",
        })
        @DisplayName("Should throw exception when property is already rented in any period between start and end dates")
        void shouldThrowExceptionWhenPropertyIsAlreadyRentedInAnyPeriodBetweenStartAndEndDates(
                UUID userId,
                UUID propertyId,
                LocalDate startOfRentedPeriod,
                LocalDate endOfRentedPeriod,
                LocalDate startDateOfRentRequest,
                LocalDate endDateOfRentRequest
        ) {
             var currentRental = Rental.builder()
                    .id(UUID.fromString("607a3cdc-19b4-450f-8a70-cb135b2cf26f"))
                    .user(tenant)
                    .property(property)
                    .startDate(startOfRentedPeriod)
                    .endDate(endOfRentedPeriod)
                    .state(RentalState.CONFIRMED)
                     .value(new Price(BigDecimal.valueOf(5000)))
                    .build();

             property.addRental(currentRental);

            when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(tenant));
            when(propertyRepositoryMock.findById(propertyId)).thenReturn(Optional.of(property));

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() ->
                            sut.registerRental(
                                    userId,
                                    propertyId,
                                    startDateOfRentRequest,
                                    endDateOfRentRequest))
                    .withMessageContaining("Property is already rented during the requested period");
        }

        @Tag("TDD")
        @Test()
        @DisplayName("Should throw exception when rental duration is bigger than one year")
        void shouldThrowExceptionWhenRentalDurationIsBiggerThanOneYear() {
            var startDate = LocalDate.parse("1801-02-22");
            var endDate = LocalDate.parse("1802-02-23");

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> sut.registerRental(tenant.getId(), property.getId(), startDate, endDate))
                    .withMessageContaining("Rental duration must be 1 year or less");
        }

        @Tag("TDD")
        @Test()
        @DisplayName("Should throw exception when start date is after end date")
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            var startDate = LocalDate.parse("1801-03-22");
            var endDate = LocalDate.parse("1801-02-22");

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> sut.registerRental(tenant.getId(), property.getId(), startDate, endDate))
                    .withMessageContaining("Start date must be before end date");
        }

        @Tag("TDD")
        @Test()
        @DisplayName("Should throw exception when start date is in the past")
        void shouldThrowExceptionWhenStartDateIsInThePast() {
            var startDate = LocalDate.parse("1800-02-22");
            var endDate = LocalDate.parse("1802-03-22");

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> sut.registerRental(tenant.getId(), property.getId(), startDate, endDate))
                    .withMessageContaining("Rental cannot start in the past");
        }
    }
}

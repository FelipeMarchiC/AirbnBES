package br.ifsp.application.rental.create;

import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.util.TestDataFactory;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.services.IUuidGeneratorService;
import lombok.val;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class CreateRentalServiceTest {
    @Mock private JpaUserRepository userRepositoryMock;
    @Mock private JpaPropertyRepository propertyRepositoryMock;
    @Mock private JpaRentalRepository rentalRepositoryMock;
    @Mock private CreateRentalPresenter presenter;
    @Mock private IUuidGeneratorService uuidGeneratorService;
    @InjectMocks private CreateRentalService sut;
    private TestDataFactory factory;

    private AutoCloseable closeable;

    private User owner;
    private User tenant;
    private Property property;

    @BeforeEach
    void setupService() {
        closeable = MockitoAnnotations.openMocks(this);

        Clock fixedClock = Clock.fixed(
                LocalDate.of(2025, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant(),
                ZoneOffset.UTC
        );

        sut = new CreateRentalService(
                userRepositoryMock,
                propertyRepositoryMock,
                rentalRepositoryMock,
                uuidGeneratorService,
                fixedClock
        );

        factory = new TestDataFactory();
        tenant = factory.generateTenant();
        owner = factory.generateOwner();
        property = factory.generateProperty(owner);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Nested
    @Tag("UnitTest")
    @DisplayName("Testing valid classes")
    class TestingValidClasses {
        @Tag("TDD")
        @Test
        @DisplayName("Should successfully create rental")
        void shouldSuccessfullyCreateRental() {
            val request = factory.createRequestModel();
            val response = factory.createResponseModel();
            val rental = factory.entityFromCreateRequest(request, tenant, property);
            property.addRental(
                    factory.generateRental(
                            factory.generateTenant(),
                            property,
                            rental.getStartDate(),
                            rental.getEndDate(),
                            RentalState.PENDING
                    )
            );

            when(userRepositoryMock.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(presenter.isDone()).thenReturn(false);
            when(propertyRepositoryMock.findById(property.getId())).thenReturn(Optional.of(property));
            when(uuidGeneratorService.generate()).thenReturn(rental.getId());
            when(rentalRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);

            sut.registerRental(presenter, request);

            verify(userRepositoryMock).findById(tenant.getId());
            verify(propertyRepositoryMock).findById(property.getId());
            verify(rentalRepositoryMock).save(rentalCaptor.capture());
            verify(presenter).prepareSuccessView(response);

            Rental savedRental = rentalCaptor.getValue();
            assertThat(savedRental.getUser()).isEqualTo(tenant);
            assertThat(savedRental.getProperty()).isEqualTo(property);
            assertThat(savedRental.getStartDate()).isEqualTo(request.startDate());
            assertThat(savedRental.getEndDate()).isEqualTo(request.endDate());
            assertThat(savedRental.getState()).isEqualTo(RentalState.PENDING);
        }

        @Tag("TDD")
        @ParameterizedTest(name = "[{index}]: should calculate rental total value from {0} to {1}")
        @CsvSource({
                "2025-02-22, 2025-02-23",
                "2025-02-22, 2026-02-22",
        })
        @DisplayName("Should calculate rental total value")
        void shouldSuccessfullyCalculateRentalTotalValue(
                LocalDate startDate,
                LocalDate endDate
        ) {
            val request = factory.createRequestModel(startDate, endDate);
            val response = factory.createResponseModel();

            when(userRepositoryMock.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(presenter.isDone()).thenReturn(false);
            when(propertyRepositoryMock.findById(property.getId())).thenReturn(Optional.of(property));
            when(uuidGeneratorService.generate()).thenReturn(factory.rentalId);

            ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
            when(rentalRepositoryMock.save(rentalCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

            long daysRented = ChronoUnit.DAYS.between(startDate, endDate);
            BigDecimal expectedTotal = property.getDailyRate().getAmount().multiply(BigDecimal.valueOf(daysRented));

            sut.registerRental(presenter, request);

            Rental savedRental = rentalCaptor.getValue();
            assertThat(savedRental.getValue().getAmount()).isEqualByComparingTo(expectedTotal);

            verify(userRepositoryMock).findById(tenant.getId());
            verify(propertyRepositoryMock).findById(property.getId());
            verify(rentalRepositoryMock).save(any(Rental.class));
            verify(presenter).prepareSuccessView(response);
        }
    }

    @Nested
    @DisplayName("Testing invalid classes")
    class TestingInvalidClasses {
        @Tag("TDD")
        @ParameterizedTest(name = "[{index}]: has rental from {0} to {1} and is requested from {2} to {3}")
        @CsvSource({
                "2025-02-22, 2025-03-22, 2025-02-22, 2025-03-22",
                "2025-02-22, 2025-04-30, 2025-04-30, 2025-05-10",
        })
        @DisplayName("Should throw exception when property is already rented in any period between start and end dates")
        void shouldThrowExceptionWhenPropertyIsAlreadyRentedInAnyPeriodBetweenStartAndEndDates(
                LocalDate startOfRentedPeriod,
                LocalDate endOfRentedPeriod,
                LocalDate startDateOfRentRequest,
                LocalDate endDateOfRentRequest
        ) {
            factory.generateRental(
                    UUID.randomUUID(),
                    owner,
                    property,
                    startDateOfRentRequest.minusDays(7),
                    startDateOfRentRequest.minusDays(5),
                    RentalState.CONFIRMED
            );

            factory.generateRental(
                    UUID.randomUUID(),
                    owner,
                    property,
                    endDateOfRentRequest.plusDays(5),
                    endDateOfRentRequest.plusDays(7),
                    RentalState.CONFIRMED
            );

             factory.generateRental(
                     owner,
                     property,
                     startOfRentedPeriod,
                     endOfRentedPeriod,
                     RentalState.CONFIRMED
             );

            val request = factory.createRequestModel(startDateOfRentRequest, endDateOfRentRequest);

            when(userRepositoryMock.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(presenter.isDone()).thenReturn(false);
            when(propertyRepositoryMock.findById(property.getId())).thenReturn(Optional.of(property));

            sut.registerRental(presenter, request);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            verify(presenter).prepareFailView(exceptionCaptor.capture());

            Exception captured = exceptionCaptor.getValue();
            assertThat(captured)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Property is already rented during the requested period");
        }

        @Tag("TDD")
        @Test()
        @DisplayName("Should throw exception when rental duration is bigger than one year")
        void shouldThrowExceptionWhenRentalDurationIsBiggerThanOneYear() {
            var startDate = LocalDate.parse("2025-02-22");
            var endDate = LocalDate.parse("2026-02-23");

            val request = factory.createRequestModel(startDate, endDate);

            when(userRepositoryMock.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(presenter.isDone()).thenReturn(false);

            sut.registerRental(presenter, request);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            verify(presenter).prepareFailView(exceptionCaptor.capture());

            Exception captured = exceptionCaptor.getValue();
            assertThat(captured)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rental duration must be 1 year or less");
        }

        @Tag("TDD")
        @Test()
        @DisplayName("Should throw exception when start date is after end date")
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            var startDate = LocalDate.parse("2025-03-22");
            var endDate = LocalDate.parse("2025-02-22");

            val request = factory.createRequestModel(startDate, endDate);

            when(userRepositoryMock.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(presenter.isDone()).thenReturn(false);

            sut.registerRental(presenter, request);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            verify(presenter).prepareFailView(exceptionCaptor.capture());

            Exception captured = exceptionCaptor.getValue();
            assertThat(captured)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Start date must be before end date");
        }

        @Tag("TDD")
        @Test()
        @DisplayName("Should throw exception when start date is in the past")
        void shouldThrowExceptionWhenStartDateIsInThePast() {
            var startDate = LocalDate.parse("2024-02-22");
            var endDate = LocalDate.parse("2025-03-22");

            val request = factory.createRequestModel(startDate, endDate);

            when(userRepositoryMock.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(presenter.isDone()).thenReturn(false);

            sut.registerRental(presenter, request);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            verify(presenter).prepareFailView(exceptionCaptor.capture());

            Exception captured = exceptionCaptor.getValue();
            assertThat(captured)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rental cannot start in the past");
        }

        @Test
        @DisplayName("should throw exception when startDate and endDate are equals")
        void shouldThrowExceptionWhenStartDateAndEndDateAreEquals() {
            val date = LocalDate.of(2025, 2, 22);
            val request = factory.createRequestModel(date, date);

            when(userRepositoryMock.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(presenter.isDone()).thenReturn(false);

            sut.registerRental(presenter, request);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            verify(presenter).prepareFailView(exceptionCaptor.capture());

            Exception captured = exceptionCaptor.getValue();
            assertThat(captured)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rental cannot have the same date to start and end");
        }

        @Test()
        @DisplayName("Should throw exception when property is null")
        void shouldThrowExceptionWhenPropertyIsNull() {
            val request = factory.createRequestModel();

            when(userRepositoryMock.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(presenter.isDone()).thenReturn(false);

            sut.registerRental(presenter, request);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            verify(presenter).prepareFailView(exceptionCaptor.capture());

            Exception captured = exceptionCaptor.getValue();
            assertThat(captured)
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Property not found");
        }

        @Test()
        @DisplayName("Should throw exception when user is null")
        void shouldThrowExceptionWhenUserIsNull() {
            val request = factory.createRequestModel();

            when(userRepositoryMock.findById(tenant.getId())).thenReturn(Optional.empty());
            when(presenter.isDone()).thenReturn(true);

            sut.registerRental(presenter, request);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            verify(presenter).prepareFailView(exceptionCaptor.capture());

            Exception captured = exceptionCaptor.getValue();
            assertThat(captured)
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User does not exist");
        }

        @Test
        @DisplayName("Should return early if presenter is already done after precondition check")
        void shouldReturnEarlyIfPresenterIsAlreadyDone() {
            val request = factory.createRequestModel();

            when(userRepositoryMock.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(presenter.isDone()).thenReturn(true);

            sut.registerRental(presenter, request);

            verify(propertyRepositoryMock, never()).findById(any());
            verify(rentalRepositoryMock, never()).save(any());
            verify(presenter, never()).prepareSuccessView(any());
        }
    }
}

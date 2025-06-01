package br.ifsp.application.rental.create;

import br.ifsp.application.property.repository.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.util.TestDataFactory;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.models.user.UserEntity;
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
    @Mock private CreateRentalPresenter presenter;
    @Mock private IUuidGeneratorService uuidGeneratorService;
    @InjectMocks private CreateRentalService sut;
    private TestDataFactory factory;

    private AutoCloseable closeable;

    private UserEntity ownerEntity;
    private User tenant;
    private UserEntity tenantEntity;
    private Property property;
    private PropertyEntity propertyEntity;

    @BeforeEach
    void setupService() {
        closeable = MockitoAnnotations.openMocks(this);

        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);

        sut = new CreateRentalService(
                userRepositoryMock,
                propertyRepositoryMock,
                rentalRepositoryMock,
                uuidGeneratorService,
                fixedClock
        );

        factory = new TestDataFactory(fixedClock);
        tenant = factory.generateTenant();
        tenantEntity = factory.generateTenantEntity(tenant);
        User owner = factory.generateOwner();
        ownerEntity = factory.generateOwnerEntity(owner);
        property = factory.generateProperty(owner);
        propertyEntity = factory.generatePropertyEntity(property);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Nested
    @DisplayName("Testing valid classes")
    class TestingValidClasses {
        @Tag("TDD")
        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        @DisplayName("Should successfully create rental")
        void shouldSuccessfullyCreateRental() {
            val request = factory.createRequestModel();
            val response = factory.createResponseModel();
            val rental = factory.entityFromCreateRequest(request, tenant, property);
            factory.generateRental(
                    factory.generateTenant(),
                    property,
                    rental.getStartDate(),
                    rental.getEndDate(),
                    RentalState.PENDING
            );

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
            when(presenter.isDone()).thenReturn(false);
            when(propertyRepositoryMock.findById(propertyEntity.getId())).thenReturn(Optional.of(propertyEntity));
            when(uuidGeneratorService.generate()).thenReturn(rental.getId());
            when(rentalRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<RentalEntity> rentalCaptor = ArgumentCaptor.forClass(RentalEntity.class);

            sut.registerRental(presenter, request);

            verify(userRepositoryMock).findById(tenantEntity.getId());
            verify(propertyRepositoryMock).findById(propertyEntity.getId());
            verify(rentalRepositoryMock).save(rentalCaptor.capture());
            verify(presenter).prepareSuccessView(response);

            RentalEntity savedRentalEntity = rentalCaptor.getValue();
            assertThat(savedRentalEntity.getUserEntity()).isEqualTo(tenantEntity);
            assertThat(savedRentalEntity.getPropertyEntity()).isEqualTo(propertyEntity);
            assertThat(savedRentalEntity.getStartDate()).isEqualTo(request.startDate());
            assertThat(savedRentalEntity.getEndDate()).isEqualTo(request.endDate());
            assertThat(savedRentalEntity.getState()).isEqualTo(RentalState.PENDING);
            assertThat(savedRentalEntity.getValue()).isEqualTo(rental.getValue());
        }

        @Tag("TDD")
        @Tag("UnitTest")
        @Tag("Functional")
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

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
            when(presenter.isDone()).thenReturn(false);
            when(propertyRepositoryMock.findById(propertyEntity.getId())).thenReturn(Optional.of(propertyEntity));
            when(uuidGeneratorService.generate()).thenReturn(factory.rentalId);

            ArgumentCaptor<RentalEntity> rentalCaptor = ArgumentCaptor.forClass(RentalEntity.class);
            when(rentalRepositoryMock.save(rentalCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

            long daysRented = ChronoUnit.DAYS.between(startDate, endDate);
            BigDecimal expectedTotal = propertyEntity.getDailyRate().getAmount().multiply(BigDecimal.valueOf(daysRented));

            sut.registerRental(presenter, request);

            RentalEntity savedRentalEntity = rentalCaptor.getValue();
            assertThat(savedRentalEntity.getValue().getAmount()).isEqualByComparingTo(expectedTotal);

            verify(userRepositoryMock).findById(tenantEntity.getId());
            verify(propertyRepositoryMock).findById(propertyEntity.getId());
            verify(rentalRepositoryMock).save(any(RentalEntity.class));
            verify(presenter).prepareSuccessView(response);
        }
    }

    @Nested
    @DisplayName("Testing invalid classes")
    class TestingInvalidClasses {
        @Tag("TDD")
        @Tag("UnitTest")
        @Tag("Functional")
        @ParameterizedTest(name = "[{index}]: has rental from {0} to {1} and is requested from {2} to {3}")
        @CsvSource({
                "2025-02-22, 2025-03-22, 2025-02-22, 2025-03-22",
                "2025-02-22, 2025-04-30, 2025-04-30, 2025-05-10",
                "2025-02-22, 2025-03-22, 2025-03-01, 2025-03-10"
        })
        @DisplayName("Should throw exception when property is already rented in any period between start and end dates")
        void shouldThrowExceptionWhenPropertyIsAlreadyRentedInAnyPeriodBetweenStartAndEndDates(
                LocalDate startOfRentedPeriod,
                LocalDate endOfRentedPeriod,
                LocalDate startDateOfRentRequest,
                LocalDate endDateOfRentRequest
        ) {
            factory.generateRentalEntity(
                    UUID.randomUUID(),
                    ownerEntity,
                    propertyEntity,
                    startDateOfRentRequest.minusDays(7),
                    startDateOfRentRequest.minusDays(5),
                    RentalState.CONFIRMED
            );

            factory.generateRentalEntity(
                    UUID.randomUUID(),
                    ownerEntity,
                    propertyEntity,
                    endDateOfRentRequest.plusDays(5),
                    endDateOfRentRequest.plusDays(7),
                    RentalState.CONFIRMED
            );

            factory.generateRentalEntity(
                    UUID.randomUUID(),
                    ownerEntity,
                    propertyEntity,
                    startDateOfRentRequest.minusDays(15),
                    startDateOfRentRequest.minusDays(10),
                    RentalState.PENDING
            );

            factory.generateRentalEntity(
                     UUID.randomUUID(),
                     ownerEntity,
                     propertyEntity,
                     startOfRentedPeriod,
                     endOfRentedPeriod,
                     RentalState.CONFIRMED
             );

            val request = factory.createRequestModel(startDateOfRentRequest, endDateOfRentRequest);

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
            when(presenter.isDone()).thenReturn(false);
            when(propertyRepositoryMock.findById(propertyEntity.getId())).thenReturn(Optional.of(propertyEntity));

            sut.registerRental(presenter, request);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            verify(presenter).prepareFailView(exceptionCaptor.capture());

            Exception captured = exceptionCaptor.getValue();
            assertThat(captured)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Property is already rented during the requested period");
        }

        @Tag("TDD")
        @Tag("UnitTest")
        @Tag("Functional")
        @Test()
        @DisplayName("Should throw exception when rental duration is bigger than one year")
        void shouldThrowExceptionWhenRentalDurationIsBiggerThanOneYear() {
            var startDate = LocalDate.parse("2025-02-22");
            var endDate = LocalDate.parse("2026-02-23");

            val request = factory.createRequestModel(startDate, endDate);

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
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
        @Tag("UnitTest")
        @Tag("Functional")
        @Test()
        @DisplayName("Should throw exception when start date is after end date")
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            var startDate = LocalDate.parse("2025-03-22");
            var endDate = LocalDate.parse("2025-02-22");

            val request = factory.createRequestModel(startDate, endDate);

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
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
        @Tag("UnitTest")
        @Tag("Functional")
        @Test()
        @DisplayName("Should throw exception when start date is in the past")
        void shouldThrowExceptionWhenStartDateIsInThePast() {
            var startDate = LocalDate.parse("2024-02-22");
            var endDate = LocalDate.parse("2025-03-22");

            val request = factory.createRequestModel(startDate, endDate);

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
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
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("should throw exception when startDate and endDate are equals")
        void shouldThrowExceptionWhenStartDateAndEndDateAreEquals() {
            val date = LocalDate.of(2025, 2, 22);
            val request = factory.createRequestModel(date, date);

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
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
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("Should throw exception when property is null")
        void shouldThrowExceptionWhenPropertyIsNull() {
            val request = factory.createRequestModel();

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
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
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("Should throw exception when user is null")
        void shouldThrowExceptionWhenUserIsNull() {
            val request = factory.createRequestModel();

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.empty());
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
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("Should return early if presenter is already done after precondition check")
        void shouldReturnEarlyIfPresenterIsAlreadyDone() {
            val request = factory.createRequestModel();

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
            when(presenter.isDone()).thenReturn(true);

            sut.registerRental(presenter, request);

            verify(propertyRepositoryMock, never()).findById(any());
            verify(rentalRepositoryMock, never()).save(any());
            verify(presenter, never()).prepareSuccessView(any());
        }

        @Test
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("should throw exception when user owns the property")
        void shouldThrowExceptionWhenUserOwnsTheProperty() {
            val request = factory.createRequestModel();

            tenantEntity.setOwnedProperties(List.of(factory.generatePropertyEntity()));
            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
            when(presenter.isDone()).thenReturn(false);

            sut.registerRental(presenter, request);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            verify(presenter).prepareFailView(exceptionCaptor.capture());

            Exception captured = exceptionCaptor.getValue();
            assertThat(captured)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Owner cannot rent its property");
        }
    }

    @Tag("Structural")
    @Tag("UnitTest")
    @Nested
    @DisplayName("Testing for structural integrity")
    class TestingStructure {
        @Test
        @DisplayName("User has owned property but is not renting it")
        void userHasOwnedPropertyButIsNotRentingIt() {
            val request = factory.createRequestModel();
            val response = factory.createResponseModel();

            val unrelatedProperty = factory.generatePropertyEntity(UUID.randomUUID());
            tenantEntity.setOwnedProperties(List.of(unrelatedProperty));

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
            when(presenter.isDone()).thenReturn(false);
            when(propertyRepositoryMock.findById(propertyEntity.getId())).thenReturn(Optional.of(propertyEntity));
            when(uuidGeneratorService.generate()).thenReturn(factory.rentalId);

            ArgumentCaptor<RentalEntity> rentalCaptor = ArgumentCaptor.forClass(RentalEntity.class);
            when(rentalRepositoryMock.save(rentalCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            sut.registerRental(presenter, request);

            verify(userRepositoryMock).findById(tenantEntity.getId());
            verify(propertyRepositoryMock).findById(propertyEntity.getId());
            verify(rentalRepositoryMock).save(any(RentalEntity.class));
            verify(presenter).prepareSuccessView(response);
        }
    }

    @Tag("Mutation")
    @Tag("UnitTest")
    @Nested
    @DisplayName("Testing against mutations")
    class TestingMutations {
        @CsvSource({
                "2025-02-22, 2025-03-22, 2025-03-01, 2025-03-10, true"
        })
        @ParameterizedTest(name = "[{index}]: existing = ({0} to {1}), request = ({2} to {3}), throwException = {4}")
        @DisplayName("Should properly validate overlapping logic")
        void testValidateOverlappingDates(
                LocalDate existingStart,
                LocalDate existingEnd,
                LocalDate requestStart,
                LocalDate requestEnd,
                boolean shouldThrow
        ) {
            factory.generateRental(
                    UUID.fromString("87619d8d-0ea5-4a06-9f29-660bcebc711f"),
                    tenant,
                    property,
                    existingStart,
                    existingEnd,
                    RentalState.CONFIRMED
            );

            if (shouldThrow) {
                assertThatThrownBy(() ->
                        CreateRentalService.validateOverlappingDates(requestStart, requestEnd, property)
                )
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Property is already rented during the requested period");
            } else {
                assertThatCode(() ->
                        CreateRentalService.validateOverlappingDates(requestStart, requestEnd, property)
                )
                        .doesNotThrowAnyException();
            }
        }
    }
}

package br.ifsp.application.rental.update;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.update.tenant.TenantUpdateRentalPresenter;
import br.ifsp.application.rental.update.tenant.TenantUpdateRentalService;
import br.ifsp.application.rental.util.TestDataFactory;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.shared.exceptions.ImmutablePastEntityException;
import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.domain.shared.valueobjects.Price;
import lombok.val;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
public class TenantUpdateRentalEntityServiceTest {
    @Mock private JpaUserRepository userRepositoryMock;
    @Mock private JpaRentalRepository rentalRepositoryMock;
    @Mock private TenantUpdateRentalPresenter presenter;
    @InjectMocks private TenantUpdateRentalService sut;

    private TestDataFactory factory;

    private AutoCloseable closeable;

    private UserEntity tenantEntity;
    private PropertyEntity propertyEntity;
    private RentalEntity rentalEntity;

    @BeforeEach
    void setupService() {
        closeable = MockitoAnnotations.openMocks(this);
        Clock clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        sut = new TenantUpdateRentalService(rentalRepositoryMock, userRepositoryMock, clock);

        factory = new TestDataFactory(clock);
        tenantEntity = factory.generateTenantEntity();
        UserEntity owner = factory.generateOwnerEntity();
        propertyEntity = factory.generatePropertyEntity(owner);
        rentalEntity = factory.generateRentalEntity(
                factory.rentalId,
                tenantEntity,
                propertyEntity,
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-04-30"),
                RentalState.CONFIRMED
        );
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
        @DisplayName("Should successfully cancel rental")
        void shouldSuccessfullyCancelRental(
        ) {
            val request = factory.tenantUpdateRequestModel();
            val response = factory.tenantUpdateResponseModel();

            UUID tenantId = tenantEntity.getId();
            UUID rentalId = rentalEntity.getId();

            when(userRepositoryMock.findById(tenantId)).thenReturn(Optional.of(tenantEntity));
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rentalEntity));
            when(presenter.isDone()).thenReturn(false, false);
            when(rentalRepositoryMock.save(any(RentalEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            sut.cancelRental(presenter, request);

            ArgumentCaptor<RentalEntity> rentalCaptor = ArgumentCaptor.forClass(RentalEntity.class);
            verify(rentalRepositoryMock).save(rentalCaptor.capture());

            RentalEntity savedRentalEntity = rentalCaptor.getValue();
            assertThat(savedRentalEntity.getState()).isEqualTo(RentalState.CANCELLED);

            verify(userRepositoryMock).findById(tenantId);
            verify(rentalRepositoryMock).findById(rentalId);
            verify(rentalRepositoryMock).save(any(RentalEntity.class));
            verify(presenter).prepareSuccessView(response);
        }

        @Tag("TDD")
        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        @DisplayName("Should successfully change restrained rentals in range to pending")
        void shouldSuccessfullyChangeRestrainedRentalsInRangeToPending() {
            // ---------- Arrange ----------
            val request = factory.tenantUpdateRequestModel();
            val response = factory.tenantUpdateResponseModel();

            val existingRental1 = factory.generateRentalEntity(
                    UUID.fromString("ba68477c-8d00-4a0b-961e-4f8877f7c74f"),
                    factory.generateTenantEntity(),
                    propertyEntity,
                    LocalDate.parse("2025-01-02"),
                    LocalDate.parse("2025-01-15"),
                    RentalState.RESTRAINED
            );

            val existingRental2 = factory.generateRentalEntity(
                    UUID.fromString("d19e7621-5514-476a-a528-c09b56bef71e"),
                    factory.generateTenantEntity(),
                    propertyEntity,
                    LocalDate.parse("2025-02-15"),
                    LocalDate.parse("2025-03-27"),
                    RentalState.RESTRAINED
            );

            UUID tenantId = tenantEntity.getId();
            UUID rentalId = rentalEntity.getId();

            when(userRepositoryMock.findById(tenantId)).thenReturn(Optional.of(tenantEntity));
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rentalEntity));
            when(presenter.isDone()).thenReturn(false, false);
            when(rentalRepositoryMock.findRentalsByOverlapAndState(
                    propertyEntity.getId(),
                    RentalState.RESTRAINED,
                    rentalEntity.getStartDate(),
                    rentalEntity.getEndDate(),
                    rentalId
            )).thenReturn(List.of(existingRental1, existingRental2));
            when(rentalRepositoryMock.save(any(RentalEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            // ---------- Act ----------
            sut.cancelRental(presenter, request);

            // ---------- Assert ----------
            ArgumentCaptor<RentalEntity> rentalCaptor = ArgumentCaptor.forClass(RentalEntity.class);
            verify(rentalRepositoryMock, times(3)).save(rentalCaptor.capture());

            List<RentalEntity> savedRentals = rentalCaptor.getAllValues();
            List<RentalEntity> restrainedToPending = savedRentals.stream()
                    .filter(r -> !r.getId().equals(rentalEntity.getId()))
                    .toList();

            RentalEntity cancelledRental = savedRentals.stream()
                    .filter(r -> r.getId().equals(rentalEntity.getId()))
                    .findFirst()
                    .orElseThrow();

            assertThat(cancelledRental.getState()).isEqualTo(RentalState.CANCELLED);
            restrainedToPending.forEach(r -> assertThat(r.getState()).isEqualTo(RentalState.PENDING));

            verify(userRepositoryMock).findById(tenantId);
            verify(rentalRepositoryMock).findById(rentalId);
            verify(rentalRepositoryMock).findRentalsByOverlapAndState(
                    propertyEntity.getId(),
                    RentalState.RESTRAINED,
                    rentalEntity.getStartDate(),
                    rentalEntity.getEndDate(),
                    rentalId
            );
            verify(presenter).prepareSuccessView(response);
        }
    }

    @Nested
    @DisplayName("Testing invalid classes")
    class TestingInvalidClasses {
        @Tag("TDD")
        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        @DisplayName("Should throw exception when rental state is different from confirmed")
        void shouldThrowExceptionWhenRentalStateIsDifferentFromConfirmed() {
            val request = factory.tenantUpdateRequestModel();

            rentalEntity = factory.generateRentalEntity(
                    factory.rentalId,
                    tenantEntity,
                    propertyEntity,
                    LocalDate.parse("2025-01-01"),
                    LocalDate.parse("2025-04-30"),
                    RentalState.PENDING
            );

            UUID tenantId = tenantEntity.getId();
            UUID rentalId = rentalEntity.getId();

            when(userRepositoryMock.findById(tenantId)).thenReturn(Optional.of(tenantEntity));
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rentalEntity));
            when(presenter.isDone()).thenReturn(false);

            sut.cancelRental(presenter, request);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            verify(presenter).prepareFailView(exceptionCaptor.capture());

            Exception captured = exceptionCaptor.getValue();
            assertThat(captured)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rental is not in a valid state to be cancelled");
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        @DisplayName("Should return early if presenter is already done after precondition check")
        void shouldReturnEarlyIfPresenterIsAlreadyDone() {
            val request = factory.tenantUpdateRequestModel();

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
            when(presenter.isDone()).thenReturn(true);

            sut.cancelRental(presenter, request);

            verify(rentalRepositoryMock, never()).save(any());
            verify(presenter, never()).prepareSuccessView(any());
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        @DisplayName("Should return early if presenter is already done after precondition check")
        void shouldReturnEarlyIfPresenterIsAlreadyDone2() {
            val request = factory.tenantUpdateRequestModel();

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));
            when(presenter.isDone()).thenReturn(false, true);

            sut.cancelRental(presenter, request);

            verify(rentalRepositoryMock, never()).save(any());
            verify(presenter, never()).prepareSuccessView(any());
        }
    }

    @Tag("Structural")
    @Tag("UnitTest")
    @Nested
    @DisplayName("Testing for structural integrity")
    class TestingStructure {
        @Test
        @DisplayName("Should set to expired correctly when removing restraint")
        void shouldSetToExpiredWhenRemovingRestraint() {
            // ---------- Arrange ----------
            val request = factory.tenantUpdateRequestModel();
            val response = factory.tenantUpdateResponseModel();

            val rental = new RentalEntity(
                    UUID.fromString("3f7409a7-3e55-472c-9d6c-6ecd6be6e571"),
                    factory.generateTenantEntity(),
                    propertyEntity,
                    LocalDate.parse("2024-12-25"),
                    LocalDate.parse("2024-12-31"),
                    new Price(BigDecimal.valueOf(3000.00)),
                    RentalState.PENDING
            );

            UUID tenantId = tenantEntity.getId();
            UUID rentalId = rentalEntity.getId();

            when(userRepositoryMock.findById(tenantId)).thenReturn(Optional.of(tenantEntity));
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rentalEntity));
            when(presenter.isDone()).thenReturn(false, false);
            when(rentalRepositoryMock.findRentalsByOverlapAndState(
                    propertyEntity.getId(),
                    RentalState.RESTRAINED,
                    rentalEntity.getStartDate(),
                    rentalEntity.getEndDate(),
                    rentalId
            )).thenReturn(List.of(rental));
            when(rentalRepositoryMock.save(any(RentalEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            // ---------- Act ----------
            sut.cancelRental(presenter, request);

            // ---------- Assert ----------
            ArgumentCaptor<RentalEntity> rentalCaptor = ArgumentCaptor.forClass(RentalEntity.class);
            verify(rentalRepositoryMock, times(2)).save(rentalCaptor.capture());

            List<RentalEntity> toExpire = rentalCaptor
                    .getAllValues()
                    .stream()
                    .filter(r -> !r.getId().equals(rentalEntity.getId()))
                    .toList();

            toExpire.forEach(r -> assertThat(r.getState()).isEqualTo(RentalState.EXPIRED));

            verify(userRepositoryMock).findById(tenantId);
            verify(rentalRepositoryMock).findById(rentalId);
            verify(rentalRepositoryMock).findRentalsByOverlapAndState(
                    propertyEntity.getId(),
                    RentalState.RESTRAINED,
                    rentalEntity.getStartDate(),
                    rentalEntity.getEndDate(),
                    rentalId
            );
            verify(presenter).prepareSuccessView(response);
        }

        @Test
        @DisplayName("Should return empty optional when user is not found in repository")
        void shouldReturnEmptyOptionalWhenUserIsNotFoundInRepository() {
            // ---------- Arrange ----------
            val request = factory.tenantUpdateRequestModel();

            UUID tenantId = tenantEntity.getId();

            when(userRepositoryMock.findById(tenantId)).thenReturn(Optional.empty());
            when(presenter.isDone()).thenReturn(true);

            // ---------- Act ----------
            sut.cancelRental(presenter, request);

            // ---------- Assert ----------
            verify(userRepositoryMock).findById(tenantId);

            ArgumentCaptor<EntityNotFoundException> captor = ArgumentCaptor.forClass(EntityNotFoundException.class);
            verify(presenter).prepareFailView(captor.capture());

            EntityNotFoundException exception = captor.getValue();
            assertThat(exception.getMessage()).isEqualTo("User does not exist");
        }
    }

    @Tag("Mutation")
    @Tag("UnitTest")
    @Nested
    @DisplayName("Testing against mutations")
    class TestingMutations {
        @Test
        @DisplayName("Should throw exception when rental is not found in repository")
        void shouldThrowExceptionWhenRentalIsNotFoundInRepository() {
            // ---------- Arrange ----------
            val request = factory.tenantUpdateRequestModel();

            UUID tenantId = tenantEntity.getId();

            when(userRepositoryMock.findById(tenantEntity.getId())).thenReturn(Optional.of(tenantEntity));
            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.empty());
            when(presenter.isDone()).thenReturn(false);

            // ---------- Act ----------
            sut.cancelRental(presenter, request);

            // ---------- Assert ----------
            verify(userRepositoryMock).findById(tenantId);

            ArgumentCaptor<EntityNotFoundException> captor = ArgumentCaptor.forClass(EntityNotFoundException.class);
            verify(presenter).prepareFailView(captor.capture());

            EntityNotFoundException exception = captor.getValue();
            assertThat(exception.getMessage()).isEqualTo("Rental not found");
        }

        @Test
        @DisplayName("Should throw exception inside presenter when rental is expired")
        void shouldThrowExceptionInsidePresenterWhenRentalIsExpired() {
            // ---------- Arrange ----------
            val request = factory.tenantUpdateRequestModel();

            UUID tenantId = tenantEntity.getId();
            RentalEntity rental = factory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.parse("2024-12-28"),
                    LocalDate.parse("2024-12-30"),
                    RentalState.PENDING
            );

            when(userRepositoryMock.findById(tenantId)).thenReturn(Optional.of(tenantEntity));
            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rental));
            when(presenter.isDone()).thenReturn(false, true);

            // ---------- Act ----------
            sut.cancelRental(presenter, request);

            // ---------- Assert ----------
            verify(userRepositoryMock).findById(tenantId);

            ArgumentCaptor<ImmutablePastEntityException> captor =
                    ArgumentCaptor.forClass(ImmutablePastEntityException.class);

            verify(presenter).prepareFailView(captor.capture());

            ImmutablePastEntityException exception = captor.getValue();
            assertThat(exception.getMessage()).isEqualTo("This operation must be current or future dates.");
        }
    }
}

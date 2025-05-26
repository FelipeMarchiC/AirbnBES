package br.ifsp.application.rental.update;

import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.update.tenant.TenantUpdateRentalPresenter;
import br.ifsp.application.rental.update.tenant.TenantUpdateRentalService;
import br.ifsp.application.rental.util.TestDataFactory;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import lombok.val;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
    @Mock private JpaPropertyRepository propertyRepository;
    @Mock private TenantUpdateRentalPresenter presenter;
    @InjectMocks private TenantUpdateRentalService sut;
    private TestDataFactory factory;

    private AutoCloseable closeable;

    private User tenant;
    private PropertyEntity propertyEntity;
    private RentalEntity rentalEntity;

    @BeforeEach
    void setupService() {
        closeable = MockitoAnnotations.openMocks(this);
        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.systemDefault());
        sut = new TenantUpdateRentalService(rentalRepositoryMock, userRepositoryMock, fixedClock);

        factory = new TestDataFactory();
        tenant = factory.generateTenant();
        User owner = factory.generateOwner();
        propertyEntity = factory.generateProperty(owner);
        rentalEntity = factory.generateRental(
                factory.rentalId,
                tenant,
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

            UUID tenantId = tenant.getId();
            UUID rentalId = rentalEntity.getId();

            when(userRepositoryMock.findById(tenantId)).thenReturn(Optional.of(tenant));
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

            val existingRental1 = factory.generateRental(
                    factory.generateTenant(UUID.randomUUID()),
                    propertyEntity,
                    LocalDate.parse("2025-01-02"),
                    LocalDate.parse("2025-01-15"),
                    RentalState.RESTRAINED
            );

            val existingRental2 = factory.generateRental(
                    factory.generateTenant(UUID.randomUUID()),
                    propertyEntity,
                    LocalDate.parse("2025-02-15"),
                    LocalDate.parse("2025-03-27"),
                    RentalState.RESTRAINED
            );

            UUID tenantId = tenant.getId();
            UUID rentalId = rentalEntity.getId();

            when(userRepositoryMock.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rentalEntity));
            when(presenter.isDone()).thenReturn(false, false);
            when(rentalRepositoryMock.findRentalsByOverlapAndState(
                    propertyEntity.getId(),
                    RentalState.RESTRAINED,
                    rentalEntity.getStartDate(),
                    rentalEntity.getEndDate(),
                    rentalId
            )).thenReturn(List.of(existingRental1, existingRental2));
            when(rentalRepositoryMock.save(any(RentalEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ---------- Act ----------
            sut.cancelRental(presenter, request);

            // ---------- Assert ----------
            assertThat(existingRental1.getState()).isEqualTo(RentalState.PENDING);
            assertThat(existingRental2.getState()).isEqualTo(RentalState.PENDING);

            ArgumentCaptor<RentalEntity> rentalCaptor = ArgumentCaptor.forClass(RentalEntity.class);
            verify(rentalRepositoryMock, times(3)).save(rentalCaptor.capture());

            List<RentalEntity> savedRentalEntities = rentalCaptor.getAllValues();

            RentalEntity cancelledRentalEntity = savedRentalEntities.stream()
                    .filter(r -> r.getId().equals(rentalEntity.getId()))
                    .findFirst()
                    .orElseThrow();

            assertThat(cancelledRentalEntity.getState()).isEqualTo(RentalState.CANCELLED);

            List<RentalEntity> restrainedToPending = savedRentalEntities.stream()
                    .filter(r -> !r.getId().equals(rentalEntity.getId()))
                    .toList();

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

            UUID tenantId = tenant.getId();
            UUID rentalId = rentalEntity.getId();

            rentalEntity.setState(RentalState.PENDING);

            when(userRepositoryMock.findById(tenantId)).thenReturn(Optional.of(tenant));
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

            when(userRepositoryMock.findById(tenant.getId())).thenReturn(Optional.of(tenant));
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

            when(userRepositoryMock.findById(tenant.getId())).thenReturn(Optional.of(tenant));
            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));
            when(presenter.isDone()).thenReturn(false, true);

            sut.cancelRental(presenter, request);

            verify(rentalRepositoryMock, never()).save(any());
            verify(presenter, never()).prepareSuccessView(any());
        }
    }
}

package br.ifsp.application.rental.update;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.update.owner.OwnerUpdateRentalPresenter;
import br.ifsp.application.rental.update.owner.OwnerUpdateRentalService;
import br.ifsp.application.rental.update.owner.IOwnerUpdateRentalService.RequestModel;
import br.ifsp.application.rental.util.TestDataFactory;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class OwnerUpdateRentalEntityServiceTest {

    @Mock
    private JpaRentalRepository rentalRepositoryMock;

    @Mock
    private OwnerUpdateRentalPresenter presenter;

    @InjectMocks
    private OwnerUpdateRentalService sut;

    private TestDataFactory testDataFactory;
    private User tenant;
    private User owner;
    private PropertyEntity propertyEntity;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        testDataFactory = new TestDataFactory();
        tenant = testDataFactory.generateTenant();
        owner = testDataFactory.generateOwner();
        propertyEntity = testDataFactory.generateProperty(owner);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @Tag("Functional")
    @Tag("Should throw security exception when non owner tries to confirm rental")
    void shouldThrowSecurityExceptionWhenNonOwnerTriesToConfirmRental() {
        RentalEntity rentalEntity = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.now(), LocalDate.now().plusDays(7), RentalState.PENDING);

        when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

        User nonOwner = testDataFactory.generateTenant();  // Diferente do proprietário

        RequestModel request = new RequestModel(nonOwner.getId(), rentalEntity.getId());

        sut.confirmRental(presenter, request);

        verify(presenter).prepareFailView(any(SecurityException.class));
    }


    @Nested
    @DisplayName("Rental Denial Tests")
    class DenyRentalEntityServiceTest {
        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        void shouldSetPendingRentalAsDeniedIfOwnerDenies() {
            RentalEntity rentalEntity = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.now(), LocalDate.now().plusDays(7), RentalState.PENDING);
            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

            RequestModel request = new RequestModel(owner.getId(), rentalEntity.getId());
            sut.denyRental(presenter, request);

            assertThat(rentalEntity.getState()).isEqualTo(RentalState.DENIED);
            verify(presenter).prepareSuccessView(any());
        }

        @Test
        @Tag("TDD")
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("Should throw security exception when non owner tries to deny rental")
        void shouldThrowSecurityExceptionWhenNonOwnerTriesToDenyRental() {
            RentalEntity rentalEntity = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.now(), LocalDate.now().plusDays(7), RentalState.PENDING);

            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

            User nonOwner = testDataFactory.generateTenant();

            RequestModel request = new RequestModel(nonOwner.getId(), rentalEntity.getId());

            sut.denyRental(presenter, request);

            verify(presenter).prepareFailView(any(SecurityException.class));
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"CONFIRMED", "EXPIRED", "DENIED"})
        void shouldNotPermitDenialIfRentalNotPendingOrRestrained(RentalState state) {
            RentalEntity rentalEntity = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.now(), LocalDate.now().plusDays(7), state);
            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

            RequestModel request = new RequestModel(owner.getId(), rentalEntity.getId());
            sut.denyRental(presenter, request);

            verify(presenter).prepareFailView(any(UnsupportedOperationException.class));
        }
    }

    @Nested
    class CancelConfirmedRentalEntityTests {

        @BeforeEach
        void setupClock() {
            Clock fixedClock = Clock.fixed(LocalDate.of(2025, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
            sut = new OwnerUpdateRentalService(rentalRepositoryMock, fixedClock);
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"PENDING", "DENIED", "EXPIRED", "RESTRAINED", "CANCELLED"})
        void shouldNotCancelIfRentalIsNotConfirmed(RentalState state) {
            RentalEntity rentalEntity = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 10), state);
            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

            sut.cancelRental(presenter, new RequestModel(owner.getId(), rentalEntity.getId()), null);

            verify(presenter).prepareFailView(any(IllegalArgumentException.class));
        }
        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        @DisplayName("Should not cancel a rental from a different Owner")
        void shouldNotCancelARentalFromADifferentOwners() {
            RentalEntity rentalEntity = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.now(), LocalDate.now().plusDays(7), RentalState.PENDING);

            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

            User nonOwner = testDataFactory.generateTenant();  // Diferente do proprietário

            RequestModel request = new RequestModel(nonOwner.getId(), rentalEntity.getId());

            sut.cancelRental(presenter, request,null);

            verify(presenter).prepareFailView(any(SecurityException.class));
        }

        @Tag("TDD")
        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        void shouldNotAllowCancelAfterStartDate() {
            RentalEntity rentalEntity = testDataFactory.generateRental(
                    tenant,
                    propertyEntity,
                    LocalDate.of(2024, 12, 30), // startDate (no passado)
                    LocalDate.of(2025, 1, 10),
                    RentalState.CONFIRMED
            );
            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

            sut.cancelRental(presenter, new RequestModel(owner.getId(), rentalEntity.getId()), null);

            ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
            verify(presenter).prepareFailView(captor.capture());

            Exception exception = captor.getValue();
            assertThat(exception).isInstanceOf(EntityNotFoundException.class);
            assertThat(exception.getMessage()).isEqualTo("User does not exist");
        }


        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        void shouldCancelRentalSuccessfully() {
            RentalEntity rentalEntity = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 10), RentalState.CONFIRMED);
            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.RESTRAINED), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            sut.cancelRental(presenter, new RequestModel(owner.getId(), rentalEntity.getId()), null);

            assertThat(rentalEntity.getState()).isEqualTo(RentalState.CANCELLED);
            verify(presenter).prepareSuccessView(any());
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        void shouldSetRestrainedConflictsToPending() {
            RentalEntity rentalEntity = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 10), RentalState.CONFIRMED);
            RentalEntity r1 = testDataFactory.generateRental(tenant, propertyEntity, rentalEntity.getStartDate(), rentalEntity.getEndDate(), RentalState.RESTRAINED);
            RentalEntity r2 = testDataFactory.generateRental(tenant, propertyEntity, rentalEntity.getStartDate(), rentalEntity.getEndDate(), RentalState.RESTRAINED);

            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.RESTRAINED), any(), any(), any()))
                    .thenReturn(List.of(r1, r2));

            sut.cancelRental(presenter, new RequestModel(owner.getId(), rentalEntity.getId()), null);

            assertThat(r1.getState()).isEqualTo(RentalState.PENDING);
            assertThat(r2.getState()).isEqualTo(RentalState.PENDING);
            verify(rentalRepositoryMock).saveAll(List.of(r1, r2));
        }

    }

    @Nested
    class ConfirmRentalEntityTests {

        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        void shouldConfirmPendingRentalWithoutConflict() {
            RentalEntity rentalEntity = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.of(1801, 2, 1), LocalDate.of(1801, 2, 10), RentalState.PENDING);
            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.CONFIRMED), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            sut.confirmRental(presenter, new RequestModel(owner.getId(), rentalEntity.getId()));

            assertThat(rentalEntity.getState()).isEqualTo(RentalState.CONFIRMED);
            verify(presenter).prepareSuccessView(any());
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"CONFIRMED", "EXPIRED", "DENIED", "RESTRAINED"})
        void shouldNotConfirmRentalIfNotPending(RentalState state) {
            RentalEntity rentalEntity = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.of(1801, 2, 1), LocalDate.of(1801, 2, 10), state);
            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

            sut.confirmRental(presenter, new RequestModel(owner.getId(), rentalEntity.getId()));

            verify(presenter).prepareFailView(any(UnsupportedOperationException.class));
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        @DisplayName("should throw IllegalState exception when conflicting rental exists")
        void shouldThrowIllegalStateExceptionWhenConflictingRentalExists() {
            RentalEntity rentalEntity = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.now(), LocalDate.now().plusDays(7), RentalState.PENDING);
            RentalEntity conflictingRentalEntity = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.now(), LocalDate.now().plusDays(7), RentalState.CONFIRMED);
            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.CONFIRMED), any(), any(), any()))
                    .thenReturn(List.of(conflictingRentalEntity));

            RequestModel request = new RequestModel(owner.getId(), rentalEntity.getId());
            sut.confirmRental(presenter, request);

            verify(presenter).prepareFailView(any(IllegalStateException.class));
        }



    }

    @Nested
    class UpdateRentalEntityTests {
        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        void shouldRestrainConflictingPendingRentals() {
            RentalEntity confirmedRentalEntity = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.of(1801, 2, 1), LocalDate.of(1801, 2, 10), RentalState.CONFIRMED);
            RentalEntity conflict = testDataFactory.generateRental(tenant, propertyEntity, LocalDate.of(1801, 2, 5), LocalDate.of(1801, 2, 8), RentalState.PENDING);

            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.PENDING), any(), any(), any()))
                    .thenReturn(List.of(conflict));

            sut.restrainPendingRentalsInConflict(confirmedRentalEntity);

            assertThat(conflict.getState()).isEqualTo(RentalState.RESTRAINED);
            verify(rentalRepositoryMock).save(conflict);
        }
    }

    @Tag("UnitTest")
    @Tag("Functional")
    @Test
    @Tag("Should throw entity not found exception when rental not found")
    void shouldThrowEntityNotFoundExceptionWhenRentalNotFound() {
        when(rentalRepositoryMock.findById(any())).thenReturn(Optional.empty());

        RequestModel request = new RequestModel(owner.getId(), UUID.randomUUID());
        sut.denyRental(presenter, request);

        verify(presenter).prepareFailView(any(EntityNotFoundException.class));
    }

}

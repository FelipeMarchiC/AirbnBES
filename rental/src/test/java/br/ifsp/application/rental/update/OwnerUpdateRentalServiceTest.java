package br.ifsp.application.rental.update;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.update.owner.OwnerUpdateRentalPresenter;
import br.ifsp.application.rental.update.owner.OwnerUpdateRentalService;
import br.ifsp.application.rental.update.owner.IOwnerUpdateRentalService.RequestModel;
import br.ifsp.application.rental.util.TestDataFactory;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class OwnerUpdateRentalServiceTest {

    @Mock
    private JpaRentalRepository rentalRepositoryMock;

    @Mock
    private OwnerUpdateRentalPresenter presenter;

    @InjectMocks
    private OwnerUpdateRentalService sut;

    private TestDataFactory testDataFactory;
    private User tenant;
    private User owner;
    private Property property;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        testDataFactory = new TestDataFactory();

        // Usando a TestDataFactory para gerar os dados de teste
        tenant = testDataFactory.generateTenant();
        owner = testDataFactory.generateOwner();
        property = testDataFactory.generateProperty(owner);
    }

    @Nested
    @DisplayName("Rental Denial Tests")
    class DenyRentalServiceTest {

        @Test
        void shouldSetPendingRentalAsDeniedIfOwnerDenies() {
            Rental rental = testDataFactory.generateRental(tenant, property, LocalDate.now(), LocalDate.now().plusDays(7), RentalState.PENDING);

            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));

            RequestModel request = new RequestModel(owner.getId(), rental.getId());
            sut.denyRental(presenter, request);

            assertThat(rental.getState()).isEqualTo(RentalState.DENIED);
            verify(presenter).prepareSuccessView(any());
        }

        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"CONFIRMED", "EXPIRED", "DENIED"})
        void shouldNotPermitDenialIfRentalNotPendingOrRestrained(RentalState state) {
            Rental rental = testDataFactory.generateRental(tenant, property, LocalDate.now(), LocalDate.now().plusDays(7), state);

            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));

            RequestModel request = new RequestModel(owner.getId(), rental.getId());
            sut.denyRental(presenter, request);

            verify(presenter).prepareFailView(any(UnsupportedOperationException.class));
        }
    }

    @Nested
    class CancelConfirmedRentalTests {

        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"PENDING", "DENIED", "EXPIRED", "RESTRAINED", "CANCELLED"})
        void shouldNotCancelIfRentalIsNotConfirmed(RentalState state) {
            Rental rental = testDataFactory.generateRental(tenant, property, LocalDate.now().plusDays(10), LocalDate.now().plusDays(17), state);

            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));

            sut.cancelRental(presenter, new RequestModel(owner.getId(), rental.getId()), LocalDate.now());

            verify(presenter).prepareFailView(any(IllegalArgumentException.class));
        }

        @Test
        void shouldNotAllowCancelAfterStartDate() {
            Rental rental = testDataFactory.generateRental(tenant, property, LocalDate.now(), LocalDate.now().plusDays(7), RentalState.CONFIRMED);

            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));

            sut.cancelRental(presenter, new RequestModel(owner.getId(), rental.getId()), LocalDate.now().plusDays(1));

            verify(presenter).prepareFailView(any(IllegalArgumentException.class));
        }

        @Test
        void shouldCancelRentalSuccessfully() {
            Rental rental = testDataFactory.generateRental(tenant, property, LocalDate.now(), LocalDate.now().plusDays(7), RentalState.CONFIRMED);

            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.RESTRAINED), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            sut.cancelRental(presenter, new RequestModel(owner.getId(), rental.getId()), LocalDate.now().minusDays(1));

            assertThat(rental.getState()).isEqualTo(RentalState.CANCELLED);
            verify(presenter).prepareSuccessView(any());
        }

        @Test
        void shouldSetRestrainedConflictsToPending() {
            Rental rental = testDataFactory.generateRental(tenant, property, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), RentalState.CONFIRMED);

            Rental r1 = testDataFactory.generateRental(tenant, property, rental.getStartDate(), rental.getEndDate(), RentalState.RESTRAINED);
            Rental r2 = testDataFactory.generateRental(tenant, property, rental.getStartDate(), rental.getEndDate(), RentalState.RESTRAINED);

            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.RESTRAINED), any(), any(), any()))
                    .thenReturn(List.of(r1, r2));

            sut.cancelRental(presenter, new RequestModel(owner.getId(), rental.getId()), LocalDate.now());

            assertThat(r1.getState()).isEqualTo(RentalState.PENDING);
            assertThat(r2.getState()).isEqualTo(RentalState.PENDING);
            verify(rentalRepositoryMock).saveAll(List.of(r1, r2));
        }
    }

    @Nested
    class ConfirmRentalTests {

        @Test
        void shouldConfirmPendingRentalWithoutConflict() {
            Rental rental = testDataFactory.generateRental(tenant, property, LocalDate.of(1801, 2, 1), LocalDate.of(1801, 2, 10), RentalState.PENDING);

            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.CONFIRMED), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            sut.confirmRental(presenter, new RequestModel(owner.getId(), rental.getId()));

            assertThat(rental.getState()).isEqualTo(RentalState.CONFIRMED);
            verify(presenter).prepareSuccessView(any());
        }

        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"CONFIRMED", "EXPIRED", "DENIED", "RESTRAINED"})
        void shouldNotConfirmRentalIfNotPending(RentalState state) {
            Rental rental = testDataFactory.generateRental(tenant, property, LocalDate.of(1801, 2, 1), LocalDate.of(1801, 2, 10), state);

            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));

            sut.confirmRental(presenter, new RequestModel(owner.getId(), rental.getId()));

            verify(presenter).prepareFailView(any(UnsupportedOperationException.class));
        }
    }

    @Nested
    class UpdateRentalTests {

        @Test
        void shouldRestrainConflictingPendingRentals() {
            Rental confirmedRental = testDataFactory.generateRental(tenant, property, LocalDate.of(1801, 2, 1), LocalDate.of(1801, 2, 10), RentalState.CONFIRMED);

            Rental conflict = testDataFactory.generateRental(tenant, property, LocalDate.of(1801, 2, 5), LocalDate.of(1801, 2, 8), RentalState.PENDING);

            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.PENDING), any(), any(), any()))
                    .thenReturn(List.of(conflict));

            sut = new OwnerUpdateRentalService(rentalRepositoryMock, Clock.systemUTC());
            sut.restrainPendingRentalsInConflict(confirmedRental);

            assertThat(conflict.getState()).isEqualTo(RentalState.RESTRAINED);
            verify(rentalRepositoryMock).save(conflict);
        }
    }
}

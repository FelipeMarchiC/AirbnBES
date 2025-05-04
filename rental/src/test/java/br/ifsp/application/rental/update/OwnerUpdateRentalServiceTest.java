package br.ifsp.application.rental.update;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.update.owner.OwnerUpdateRentalPresenter;
import br.ifsp.application.rental.update.owner.OwnerUpdateRentalService;
import br.ifsp.application.rental.update.owner.IOwnerUpdateRentalService.RequestModel;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OwnerUpdateRentalServiceTest {

    @Mock
    private JpaRentalRepository rentalRepositoryMock;

    @Mock
    private OwnerUpdateRentalPresenter presenter;

    @InjectMocks
    private OwnerUpdateRentalService sut;

    private User tenant;
    private Property property;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        tenant = new User(
                UUID.fromString("e924925c-2a7b-4cab-b938-0d6398ecc78a"),
                "Pedro",
                "Barro",
                "barro@outlook.com",
                "cGFzc3dvcmQ=",
                Role.USER,
                new ArrayList<>()
        );

        property = new Property(
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                "Wuthering Heights",
                "An isolated farmhouse on the Yorkshire moors.",
                new Price(new BigDecimal("250.00")),
                new Address("1", "Moor Lane", "Haworth", "West Yorkshire", "BD22"),
                tenant,
                new ArrayList<>()
        );
    }

    @Nested
    @DisplayName("Rental Denial Tests")
    class DenyRentalServiceTest {
        @Test
        void shouldSetAPendingRentalAsDeniedIfPropertyOwnerDeniesIt() {
            Rental rental = new Rental();
            rental.setId(UUID.randomUUID());
            rental.setUser(tenant);
            rental.setProperty(property);
            rental.setState(RentalState.PENDING);
            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));

            RequestModel request = new RequestModel(tenant.getId(), rental.getId());
            sut.denyRental(presenter, request);

            assertThat(rental.getState()).isEqualTo(RentalState.DENIED);
            verify(presenter).prepareSuccessView(any());
        }

        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"CONFIRMED", "EXPIRED", "DENIED"})
        void shouldNotPermitDenialToARentalWithInvalidState(RentalState state) {
            Rental rental = new Rental();
            rental.setId(UUID.randomUUID());
            rental.setUser(tenant);
            rental.setProperty(property);
            rental.setState(state);
            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));

            RequestModel request = new RequestModel(tenant.getId(), rental.getId());
            sut.denyRental(presenter, request);

            verify(presenter).prepareFailView(any(UnsupportedOperationException.class));
        }
    }

    @Nested
    class CancelConfirmedRentalTests {
        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"PENDING", "DENIED", "EXPIRED", "RESTRAINED", "CANCELLED"})
        void shouldNotPermitToCancelUnconfirmedRental(RentalState state) {
            Rental rental = Rental.builder()
                    .id(UUID.randomUUID())
                    .startDate(LocalDate.now().plusDays(10))
                    .state(state)
                    .build();
            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));

            sut.cancelRental(presenter, new RequestModel(UUID.randomUUID(), rental.getId()), LocalDate.now());

            verify(presenter).prepareFailView(any(IllegalArgumentException.class));
        }

        @Test
        void shouldNotAllowCancelDateAfterStartDate() {
            Rental rental = Rental.builder()
                    .id(UUID.randomUUID())
                    .startDate(LocalDate.now())
                    .state(RentalState.CONFIRMED)
                    .build();

            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));

            sut.cancelRental(presenter, new RequestModel(UUID.randomUUID(), rental.getId()), LocalDate.now().plusDays(1));

            verify(presenter).prepareFailView(any(IllegalArgumentException.class));
        }

        @Test
        void shouldChangeRentalStateToCanceled() {
            property.setId(UUID.randomUUID());
            Rental rental = Rental.builder()
                    .id(UUID.randomUUID())
                    .property(property)
                    .startDate(LocalDate.now())
                    .state(RentalState.CONFIRMED)
                    .user(tenant)
                    .build();

            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.RESTRAINED), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            sut.cancelRental(presenter, new RequestModel(tenant.getId(), rental.getId()), LocalDate.now().minusDays(1));

            assertThat(rental.getState()).isEqualTo(RentalState.CANCELLED);
            verify(presenter).prepareSuccessView(any());
        }

        @Test
        void shouldChangeRestrainedRentalsThatConflictToPending() {
            property.setId(UUID.randomUUID());
            Rental rental = Rental.builder()
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(10))
                    .id(UUID.randomUUID())
                    .property(property)
                    .state(RentalState.CONFIRMED)
                    .user(tenant)
                    .build();

            Rental r1 = Rental.builder().startDate(rental.getStartDate()).endDate(rental.getEndDate()).property(property).state(RentalState.RESTRAINED).id(UUID.randomUUID()).build();
            Rental r2 = Rental.builder().startDate(rental.getStartDate()).endDate(rental.getEndDate()).property(property).state(RentalState.RESTRAINED).id(UUID.randomUUID()).build();

            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.RESTRAINED), any(), any(), any()))
                    .thenReturn(List.of(r1, r2));

            sut.cancelRental(presenter, new RequestModel(tenant.getId(), rental.getId()), LocalDate.now());

            assertThat(r1.getState()).isEqualTo(RentalState.PENDING);
            assertThat(r2.getState()).isEqualTo(RentalState.PENDING);
            verify(rentalRepositoryMock).saveAll(List.of(r1, r2));
        }
    }

    @Nested
    class ConfirmRentalTests {
        @Test
        void shouldConfirmRentalWithoutConflict() {
            UUID rentalId = UUID.randomUUID();
            Rental rental = Rental.builder()
                    .id(rentalId)
                    .user(tenant)
                    .property(property)
                    .startDate(LocalDate.of(1801, 2, 1))
                    .endDate(LocalDate.of(1801, 2, 10))
                    .state(RentalState.PENDING)
                    .build();

            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rental));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.CONFIRMED), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            sut.confirmRental(presenter, new RequestModel(tenant.getId(), rentalId));

            assertThat(rental.getState()).isEqualTo(RentalState.CONFIRMED);
            verify(presenter).prepareSuccessView(any());
        }

        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"CONFIRMED", "EXPIRED", "DENIED", "RESTRAINED"})
        void shouldNotAllowConfirmingNonPendingRental(RentalState state) {
            UUID rentalId = UUID.randomUUID();
            Rental rental = Rental.builder()
                    .id(rentalId)
                    .user(tenant)
                    .property(property)
                    .startDate(LocalDate.of(1801, 2, 1))
                    .endDate(LocalDate.of(1801, 2, 10))
                    .state(state)
                    .build();

            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rental));

            sut.confirmRental(presenter, new RequestModel(tenant.getId(), rentalId));

            verify(presenter).prepareFailView(any(UnsupportedOperationException.class));
        }
    }

    @Nested
    class UpdateRentalTests {
        @Test
        void shouldRestrainConflictingPendingRentals() {
            UUID rentalId = UUID.randomUUID();
            Rental confirmedRental = Rental.builder()
                    .id(rentalId)
                    .user(tenant)
                    .property(property)
                    .startDate(LocalDate.of(1801, 2, 1))
                    .endDate(LocalDate.of(1801, 2, 10))
                    .state(RentalState.CONFIRMED)
                    .build();

            Rental conflicting = Rental.builder()
                    .id(UUID.randomUUID())
                    .user(tenant)
                    .property(property)
                    .startDate(LocalDate.of(1801, 2, 5))
                    .endDate(LocalDate.of(1801, 2, 8))
                    .state(RentalState.PENDING)
                    .build();

            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.PENDING), any(), any(), any()))
                    .thenReturn(List.of(conflicting));

            sut = new OwnerUpdateRentalService(rentalRepositoryMock);
            sut.restrainPendingRentalsInConflict(confirmedRental);

            assertThat(conflicting.getState()).isEqualTo(RentalState.RESTRAINED);
            verify(rentalRepositoryMock).save(conflicting);
        }
    }
}

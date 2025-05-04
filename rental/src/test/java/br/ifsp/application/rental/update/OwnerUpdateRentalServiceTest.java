package br.ifsp.application.rental.update;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.update.owner.OwnerUpdateRentalService;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


public class OwnerUpdateRentalServiceTest {

    @Mock
    private JpaRentalRepository rentalRepositoryMock;
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
                new Address(
                        "1",
                        "Moor Lane",
                        "Haworth",
                        "West Yorkshire",
                        "BD22"
                ),
                tenant,
                new ArrayList<>()
        );
    }

    @Nested
    @DisplayName("Rental Denial Tests")
    class DenyRentalServiceTest{
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("Should set a pending or blocked rental as denied if property owner denies it")
        @Test
        void shouldSetAPendingRentalAsDeniedIfPropertyOwnerDeniesIt(){
            Rental rental = new Rental();
            rental.setState(RentalState.PENDING);
            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));
            sut.deny(rental.getId());
            assertThat(rental.getState()).isEqualTo(RentalState.DENIED);
        }
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("Should not permit denial to a rental with status different than Pending or Restrained")
        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"CONFIRMED", "EXPIRED", "DENIED"})
        void shouldNotPermitDenialToARentalWithStateDifferentThanPendingOrRestrained(RentalState state){
            Rental rental = new Rental();

            rental.setState(state);
            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));
            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(()->sut.deny(rental.getId()));
        }


    }
    @DisplayName("Cancel Confirmed Rental")
    @Nested
    class CancelConfirmedRentalTests{
        @DisplayName("Should not permit to cancel an unconfirmed rental")
        @Tag("UnitTest")
        @Tag("TDD")
        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"PENDING","DENIED","EXPIRED","RESTRAINED","CANCELLED"})
        void shouldNotPermitToCancelAnUnconfirmedRental(RentalState state){
            Rental rental = Rental.builder().
                    id(UUID.randomUUID())
                    .startDate(LocalDate.now().plusDays(10))
                    .state(state).build();
            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(()->sut.cancel(rental.getId()));
        }

        @DisplayName("Should not allow a cancel date after the Start date of rental")
        @Test
        @Tag("TDD")
        @Tag("UnitTest")
        void shouldNotAllowACancelDateAfterTheStartDateOfRental(){
            LocalDate startDate = LocalDate.now();
            LocalDate cancelDate = LocalDate.now().plusDays(10);
            Rental rental= Rental.builder()
                    .id(UUID.randomUUID())
                    .state(RentalState.CONFIRMED)
                    .startDate(startDate)
                    .build();
            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(()->sut.cancel(rental.getId(),cancelDate));
        }

        @DisplayName("Should change rental state to canceled")
        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldChangeRentalStateToCanceled(){
            property= new Property();
            property.setId(UUID.randomUUID());
            Rental rental = Rental.builder()
                    .id(UUID.randomUUID())
                    .property(property)
                    .startDate(LocalDate.now())
                    .state(RentalState.CONFIRMED)
                    .build();
            when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));
            sut.cancel(rental.getId(), LocalDate.now().minusDays(1));
            assertThat(rental.getState()).isEqualTo(RentalState.CANCELLED);

        }
        
        @DisplayName("Should change Restrained rentals that conflicts to Pending")
        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        void shouldChangeRestrainedRentalsThatConflictsToPending(){
            property = new Property();
            property.setId(UUID.randomUUID());
            Rental rental = Rental.builder().
                    startDate(LocalDate.now().plusDays(1)).
                    endDate(LocalDate.now().plusDays(10)).
                    id(UUID.randomUUID()).
                    property(property).
                    state(RentalState.CONFIRMED).
                    build();

            Rental restrainedRental = Rental.builder().
                    startDate(LocalDate.now().plusDays(1)).
                    endDate(LocalDate.now().plusDays(10)).
                    id(UUID.randomUUID()).
                    property(property).
                    state(RentalState.RESTRAINED).
                    build();

            Rental restrainedRental1 = Rental.builder().
                    startDate(LocalDate.now().plusDays(1)).
                    endDate(LocalDate.now().plusDays(10)).
                    id(UUID.randomUUID()).
                    property(property).
                    state(RentalState.RESTRAINED).
                    build();
            List<Rental> rentals = List.of(restrainedRental, restrainedRental1);
            mockReturnSetups(rental);
            when(rentalRepositoryMock.findRentalsByOverlapAndState(property.getId(),RentalState.RESTRAINED,rental.getStartDate(),rental.getEndDate(),rental.getId())).thenReturn(rentals);
            sut.cancel(rental.getId());
            assertThat(rentals).allMatch(rental1 -> rental1.getState().equals(RentalState.PENDING));

        }
    }

    private void mockReturnSetups(Rental rental) {
        when(rentalRepositoryMock.findById(rental.getId())).thenReturn(Optional.of(rental));
    }


    @Nested
    @DisplayName("Confirm Rental Tests")
    class ConfirmRentalTests {

        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("Should confirm rental when no conflicting confirmed rentals exist")
        @Test
        void shouldConfirmPendingRentalWithoutConflict() {
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
            when(rentalRepositoryMock.findRentalsByOverlapAndState(
                    property.getId(),
                    RentalState.CONFIRMED,
                    rental.getStartDate(),
                    rental.getEndDate(),
                    rental.getId()
            )).thenReturn(List.of());

            sut.confirmRental(rentalId, property.getOwner().getId());

            assertThat(rental.getState()).isEqualTo(RentalState.CONFIRMED);
            verify(rentalRepositoryMock).save(rental);
        }
    }

    @Nested
    @DisplayName("Update Rental Tests")
    class UpdateRentalTests {

        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("Should restrain conflicting pending rentals")
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

            UUID conflictingRentalId = UUID.randomUUID();
            Rental conflictingRental = Rental.builder()
                    .id(conflictingRentalId)
                    .user(tenant)
                    .property(property)
                    .startDate(LocalDate.of(1801, 2, 5))
                    .endDate(LocalDate.of(1801, 2, 8))
                    .state(RentalState.PENDING)
                    .build();

            when(rentalRepositoryMock.findRentalsByOverlapAndState(
                    property.getId(),
                    RentalState.PENDING,
                    confirmedRental.getStartDate(),
                    confirmedRental.getEndDate(),
                    confirmedRental.getId()))
                    .thenReturn(List.of(conflictingRental));

            sut.restrainPendingRentalsInConflict(confirmedRental);

            assertThat(conflictingRental.getState()).isEqualTo(RentalState.RESTRAINED);
            verify(rentalRepositoryMock).save(conflictingRental);
        }
    }

    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("Should not allow confirming a rental that is not in PENDING state")
    @ParameterizedTest
    @EnumSource(value = RentalState.class, names = {"CONFIRMED", "EXPIRED", "DENIED", "RESTRAINED"})
    void shouldNotAllowConfirmingRentalNotInPendingState(RentalState state) {
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

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> sut.confirmRental(rentalId, property.getOwner().getId()))
                .withMessage("Rental is not in a PENDING state and cannot be confirmed.");
    }

    @Nested
    @DisplayName("Null Input Validation Tests")
    class NullInputValidationTests {
        @Tag("UnitTest")
        @DisplayName("Should throw exception when rental ID is null on confirm")
        @Test
        void shouldThrowExceptionWhenRentalIdIsNullOnConfirm() {
            assertThatThrownBy(() -> new OwnerUpdateRentalService(rentalRepositoryMock).confirmRental(null, UUID.randomUUID()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rental not found");
        }

        @Tag("UnitTest")
        @DisplayName("Should throw exception when rental is null on restrain conflict")
        @Test
        void shouldThrowExceptionWhenRentalIsNullOnRestrainConflict() {
            assertThatThrownBy(() -> new OwnerUpdateRentalService(rentalRepositoryMock).restrainPendingRentalsInConflict(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}

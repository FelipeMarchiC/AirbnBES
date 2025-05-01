package br.ifsp.application.rental.update;

import br.ifsp.application.rental.repository.JpaRentalRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


public class UpdateRentalServiceTest {

    @Mock
    private JpaRentalRepository rentalRepositoryMock;
    @InjectMocks
    private UpdateRentalService sut;

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

/*    @Nested
    @DisplayName("Rental Denial Tests")
    class DenyRentalServiceTest{
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("Should set a pending or blocked rental as denied if property owner denies it")
        @Test
        void shouldSetAPendingRentalAsDeniedIfPropertyOwnerDeniesIt(){
            Rental rental = new Rental();
            rental.setState(RentalState.PENDING);
            UpdateRentalService sut = new UpdateRentalService();
            sut.deny(rental);
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
            UpdateRentalService sut = new UpdateRentalService();
            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(()->sut.deny(rental));
        }
    }
ATUALIZAR POIS AGORA TEM RENTALREPOSITORY
 */

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

            sut.confirmRental(rentalId);

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

}

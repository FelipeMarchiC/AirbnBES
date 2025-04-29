package br.ifsp.application.rental.update;

import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

public class UpdateRentalServiceTest {
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
}

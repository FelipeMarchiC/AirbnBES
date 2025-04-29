package br.ifsp.application.rental.update;

import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class UpdateRentalServiceTest {
    @Nested
    @DisplayName("Rental Denial Tests")
    class DenyRetalService{
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("Should set a pending rental as denied if property owner denies it")
        @Test
        void ShouldSetAPendingRentalAsDeniedIfPropertyOwnerDeniesIt(){
            Rental rental = new Rental();
            rental.setState(RentalState.PENDING);
            UpdateRentalService sut = new UpdateRentalService();
            sut.deny(rental);
            assertThat(rental.getState()).isEqualTo(RentalState.DENIED);

        }

    }
}

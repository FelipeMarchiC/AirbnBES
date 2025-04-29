package br.ifsp.application.rental.delete;

import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

class DeleteRentalServiceTest {

    @Nested
    @DisplayName("Rental Deletion Tests")
    class RentalDeletionTests {
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("Should return error message when state is not PENDING or DENIED")
        @Test
        void shouldReturnErrorMessageWhenStateIsNotPendingOrDenied() {
            Rental rental = new Rental();
            rental.setState(RentalState.CONFIRMED);
            DeleteRentalService deleteRentalService = new DeleteRentalService();
            assertThatThrownBy(() -> deleteRentalService.delete(rental))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("State must be PENDING or DENIED");
        }
    }
}


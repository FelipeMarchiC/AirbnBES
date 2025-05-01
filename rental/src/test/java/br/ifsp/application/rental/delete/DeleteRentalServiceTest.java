package br.ifsp.application.rental.delete;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
            JpaRentalRepository mockRepository = mock(JpaRentalRepository.class);
            DeleteRentalService deleteRentalService = new DeleteRentalService(mockRepository);
            assertThatThrownBy(() -> deleteRentalService.delete(rental))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("State must be PENDING or DENIED");
        }
    }

    @Test
    @DisplayName("Should delete rental and return its ID when state is PENDING or RESTRAINED")
    void shouldDeleteRentalWhenStateIsPending() {
        Rental rental = new Rental();
        rental.setState(RentalState.PENDING);
//        rental.setRentalID(new UUID.fromString());

        JpaRentalRepository mockRepository = mock(JpaRentalRepository.class);
        DeleteRentalService deleteRentalService = new DeleteRentalService(mockRepository);

//        String deletedId = deleteRentalService.delete(rental);

//        verify(mockRepository).deleteById("rental-123");
//        assertThat(deletedId).isEqualTo("rental-123");
    }

}


package br.ifsp.application.rental.delete;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteRentalServiceTest {

    @Mock
    private JpaRentalRepository rentalRepositoryMock;

    @InjectMocks
    private DeleteRentalService sut;

    private Rental rental;

    @BeforeEach
    void setup() {
        rental = new Rental();
        rental.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    }

    @Nested
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("Rental Deletion Tests")
    class RentalDeletionTests {

        @Test
        @DisplayName("Should throw exception when state is not PENDING or DENIED")
        void shouldThrowWhenStateIsInvalid() {
            rental.setState(RentalState.CONFIRMED);
            assertThatThrownBy(() -> sut.delete(rental))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("State must be PENDING or DENIED");
            verify(rentalRepositoryMock, never()).deleteById(any());
        }

        @ParameterizedTest(name = "[{index}]: should delete rental with state {0}")
        @CsvSource({
                "PENDING",
                "DENIED"
        })
        @DisplayName("Should delete rental and return ID when state is valid")
        void shouldDeleteWhenStateIsValid(RentalState state) {
            rental.setState(state);
            UUID deletedId = sut.delete(rental);
            verify(rentalRepositoryMock).deleteById(rental.getId());
            assertThat(deletedId).isEqualTo(rental.getId());
        }
    }
}

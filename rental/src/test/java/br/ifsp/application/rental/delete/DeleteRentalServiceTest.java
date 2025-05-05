package br.ifsp.application.rental.delete;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteRentalServiceTest {

    @Mock
    private JpaRentalRepository rentalRepositoryMock;

    @Mock
    private DeleteRentalPresenter presenter;

    @InjectMocks
    private DeleteRentalService sut;

    private UUID rentalId;
    private UUID ownerId;
    private UUID tenantId;
    private Rental rental;

    @BeforeEach
    void setup() {
        rentalId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ownerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        tenantId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        User tenant = mock(User.class);
        lenient().when(tenant.getId()).thenReturn(tenantId);

        rental = new Rental();
        rental.setId(rentalId);
        rental.setUser(tenant);
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
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rental));

            var request = new IDeleteRentalService.RequestModel(ownerId, rentalId);
            sut.delete(presenter, request);

            verify(presenter).prepareFailView(any(IllegalArgumentException.class));
            verify(rentalRepositoryMock, never()).deleteById(any());
        }

        @ParameterizedTest(name = "[{index}]: should delete rental with state {0}")
        @EnumSource(value = RentalState.class, names = {"PENDING", "DENIED"})
        @DisplayName("Should delete rental and return ID when state is valid")
        void shouldDeleteWhenStateIsValid(RentalState state) {
            rental.setState(state);
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rental));

            var request = new IDeleteRentalService.RequestModel(ownerId, rentalId);
            sut.delete(presenter, request);

            verify(rentalRepositoryMock).deleteById(rentalId);

            ArgumentCaptor<IDeleteRentalService.ResponseModel> captor =
                    ArgumentCaptor.forClass(IDeleteRentalService.ResponseModel.class);
            verify(presenter).prepareSuccessView(captor.capture());

            var response = captor.getValue();
            assertThat(response.ownerId()).isEqualTo(ownerId);
            assertThat(response.tenantId()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("Should handle rental not found")
        void shouldHandleRentalNotFound() {
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.empty());

            var request = new IDeleteRentalService.RequestModel(ownerId, rentalId);
            sut.delete(presenter, request);

            verify(presenter).prepareFailView(any(IllegalArgumentException.class));
            verify(rentalRepositoryMock, never()).deleteById(any());
        }
    }
}

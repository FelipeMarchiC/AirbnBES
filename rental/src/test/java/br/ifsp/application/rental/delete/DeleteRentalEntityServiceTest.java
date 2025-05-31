package br.ifsp.application.rental.delete;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.repository.RentalMapper;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.application.user.repository.UserMapper;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.models.user.UserEntity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteRentalEntityServiceTest {

    @Mock
    private JpaRentalRepository rentalRepositoryMock;

    @Mock
    private JpaUserRepository userRepositoryMock;

    @Mock
    private DeleteRentalPresenter presenter;

    private DeleteRentalService sut;

    private UUID rentalId;
    private UUID ownerId;
    private UUID tenantId;
    private RentalEntity rentalEntity;
    private Clock clock;

    @BeforeEach
    void setup() {
        clock = Clock.fixed(Instant.parse("2025-06-01T00:00:00Z"), ZoneId.systemDefault());
        sut = new DeleteRentalService(rentalRepositoryMock, userRepositoryMock, clock);

        rentalId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ownerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        tenantId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        UserEntity tenant = mock(UserEntity.class);
        lenient().when(tenant.getId()).thenReturn(tenantId);

        rentalEntity = new RentalEntity();
        rentalEntity.setId(rentalId);
        rentalEntity.setUserEntity(tenant);
    }

    @Nested
    @Tag("UnitTest")
    @Tag("TDD")
    @Tag("Functional")
    @DisplayName("Successful rental deletion")
    class SuccessfulDeletion {

        @ParameterizedTest(name = "[{index}]: should delete rental with state {0}")
        @EnumSource(value = RentalState.class, names = {"PENDING", "DENIED"})
        @DisplayName("Should delete rental and notify presenter")
        void shouldDeleteWhenStateIsValid(RentalState state) {
            try (
                    MockedStatic<UserMapper> userMapperMocked = mockStatic(UserMapper.class);
                    MockedStatic<RentalMapper> rentalMapperMocked = mockStatic(RentalMapper.class)
            ) {
                User mockedUser = mock(User.class);
                userMapperMocked.when(() -> UserMapper.toDomain(any())).thenReturn(mockedUser);

                Rental mockedRental = mock(Rental.class);
                when(mockedRental.getState()).thenReturn(state);
                when(mockedRental.getStartDate()).thenReturn(LocalDate.now(clock).plusDays(1));
                when(mockedRental.getId()).thenReturn(rentalId);
                when(mockedRental.getUser()).thenReturn(mock(User.class));
                when(mockedRental.getUser().getId()).thenReturn(tenantId);

                rentalMapperMocked.when(() -> RentalMapper.toDomain(eq(rentalEntity), any())).thenReturn(mockedRental);

                when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rentalEntity));
                when(userRepositoryMock.findById(ownerId)).thenReturn(Optional.of(mock(UserEntity.class)));

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
        }
    }

    @Nested
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("Failure scenarios")
    class FailureCases {

        @Test
        @DisplayName("Should throw exception when state is not PENDING or DENIED")
        void shouldThrowWhenStateIsInvalid() {
            rentalEntity.setState(RentalState.CONFIRMED);
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rentalEntity));
            when(userRepositoryMock.findById(ownerId)).thenReturn(Optional.of(mock(UserEntity.class)));

            try (MockedStatic<UserMapper> userMapperMocked = mockStatic(UserMapper.class)) {
                userMapperMocked.when(() -> UserMapper.toDomain(any())).thenReturn(mock(User.class));

                Rental mockedRental = mock(Rental.class);
                when(mockedRental.getState()).thenReturn(RentalState.CONFIRMED);

                try (MockedStatic<RentalMapper> rentalMapperMocked = mockStatic(RentalMapper.class)) {
                    rentalMapperMocked.when(() -> RentalMapper.toDomain(eq(rentalEntity), any())).thenReturn(mockedRental);

                    var request = new IDeleteRentalService.RequestModel(ownerId, rentalId);
                    sut.delete(presenter, request);

                    verify(presenter).prepareFailView(any(IllegalArgumentException.class));
                    verify(rentalRepositoryMock, never()).deleteById(any());
                }
            }
        }

        @Test
        @DisplayName("Should handle rental not found")
        void shouldHandleRentalNotFound() {
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.empty());
            when(userRepositoryMock.findById(ownerId)).thenReturn(Optional.of(new UserEntity()));

            var request = new IDeleteRentalService.RequestModel(ownerId, rentalId);
            sut.delete(presenter, request);

            verify(presenter).prepareFailView(any(EntityNotFoundException.class));
            verify(rentalRepositoryMock, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should handle exception from repository when finding rental")
        void shouldHandleExceptionFromRepository() {
            when(rentalRepositoryMock.findById(rentalId)).thenThrow(new RuntimeException("DB error"));
            when(userRepositoryMock.findById(ownerId)).thenReturn(Optional.of(mock(UserEntity.class)));
            try (MockedStatic<UserMapper> userMapperMocked = mockStatic(UserMapper.class)) {
                userMapperMocked.when(() -> UserMapper.toDomain(any())).thenReturn(mock(User.class));

                var request = new IDeleteRentalService.RequestModel(ownerId, rentalId);
                sut.delete(presenter, request);

                verify(presenter).prepareFailView(any(RuntimeException.class));
            }
        }

        @Test
        @DisplayName("Should handle exception during deletion")
        void shouldHandleExceptionDuringDeletion() {
            rentalEntity.setState(RentalState.DENIED);
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rentalEntity));
            when(userRepositoryMock.findById(ownerId)).thenReturn(Optional.of(mock(UserEntity.class)));

            try (
                    MockedStatic<UserMapper> userMapperMocked = mockStatic(UserMapper.class);
                    MockedStatic<RentalMapper> rentalMapperMocked = mockStatic(RentalMapper.class)
            ) {
                userMapperMocked.when(() -> UserMapper.toDomain(any())).thenReturn(mock(User.class));

                Rental mockedRental = mock(Rental.class);
                when(mockedRental.getState()).thenReturn(RentalState.DENIED);
                when(mockedRental.getStartDate()).thenReturn(LocalDate.now(clock).plusDays(1));
                when(mockedRental.getId()).thenReturn(rentalId);

                rentalMapperMocked.when(() -> RentalMapper.toDomain(eq(rentalEntity), any())).thenReturn(mockedRental);
                doThrow(new RuntimeException("Delete failed")).when(rentalRepositoryMock).deleteById(rentalId);

                var request = new IDeleteRentalService.RequestModel(ownerId, rentalId);
                sut.delete(presenter, request);

                verify(presenter).prepareFailView(any(RuntimeException.class));
            }
        }

        @Test
        @DisplayName("Should handle unexpected exception in presenter")
        void shouldHandleUnexpectedPresenterException() {
            rentalEntity.setState(RentalState.PENDING);
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rentalEntity));
            when(userRepositoryMock.findById(ownerId)).thenReturn(Optional.of(mock(UserEntity.class)));

            try (
                    MockedStatic<UserMapper> userMapperMocked = mockStatic(UserMapper.class);
                    MockedStatic<RentalMapper> rentalMapperMocked = mockStatic(RentalMapper.class)
            ) {
                userMapperMocked.when(() -> UserMapper.toDomain(any())).thenReturn(mock(User.class));

                Rental mockedRental = mock(Rental.class);
                when(mockedRental.getState()).thenReturn(RentalState.PENDING);
                when(mockedRental.getStartDate()).thenReturn(LocalDate.now(clock).plusDays(1));
                when(mockedRental.getId()).thenReturn(rentalId);
                when(mockedRental.getUser()).thenReturn(mock(User.class));
                when(mockedRental.getUser().getId()).thenReturn(tenantId);

                rentalMapperMocked.when(() -> RentalMapper.toDomain(eq(rentalEntity), any())).thenReturn(mockedRental);
                doThrow(new RuntimeException("Presenter failure")).when(presenter).prepareSuccessView(any());

                var request = new IDeleteRentalService.RequestModel(ownerId, rentalId);

                assertThatCode(() -> sut.delete(presenter, request)).doesNotThrowAnyException();
            }
        }
    }
}
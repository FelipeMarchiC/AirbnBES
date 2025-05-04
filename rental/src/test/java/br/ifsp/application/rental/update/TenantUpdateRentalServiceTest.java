package br.ifsp.application.rental.update;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.update.tenant.TenantUpdateRentalPresenter;
import br.ifsp.application.rental.update.tenant.TenantUpdateRentalService;
import br.ifsp.application.rental.util.TestDataFactory;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import lombok.val;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
public class TenantUpdateRentalServiceTest {
    @Mock private JpaUserRepository userRepositoryMock;
    @Mock private JpaRentalRepository rentalRepositoryMock;
    @Mock private TenantUpdateRentalPresenter presenter;
    @InjectMocks private TenantUpdateRentalService sut;
    private TestDataFactory factory;

    private User owner;
    private User tenant;
    private Property property;
    private Rental rental;

    @BeforeEach
    void setupService() {
        factory = new TestDataFactory();
        tenant = factory.generateTenant();
        owner = factory.generateOwner();
        property = factory.generateProperty(owner);
        rental = factory.generateRental(
                factory.rentalId,
                tenant,
                property,
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-04-30"),
                RentalState.CONFIRMED
        );
    }

    @Nested
    @Tag("UnitTest")
    @DisplayName("Testing valid classes")
    class TestingValidClasses {
        @Tag("TDD")
        @Test()
        @DisplayName("Should successfully cancel rental")
        void shouldSuccessfullyCancelRental(
        ) {
            val request = factory.tenantUpdateRequestModel();
            val response = factory.tenantUpdateResponseModel();

            UUID tenantId = tenant.getId();
            UUID rentalId = rental.getId();

            when(userRepositoryMock.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rental));
            when(presenter.isDone()).thenReturn(false);
            when(rentalRepositoryMock.save(any(Rental.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            sut.cancelRental(presenter, request);

            ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
            verify(rentalRepositoryMock).save(rentalCaptor.capture());

            Rental savedRental = rentalCaptor.getValue();
            assertThat(savedRental.getState()).isEqualTo(RentalState.CANCELLED);

            verify(userRepositoryMock).findById(tenantId);
            verify(rentalRepositoryMock).findById(rentalId);
            verify(rentalRepositoryMock).save(any(Rental.class));
            verify(presenter).prepareSuccessView(response);
        }
    }

    @Nested
    @Tag("UnitTest")
    @DisplayName("Testing invalid classes")
    class TestingInvalidClasses {
        @Tag("TDD")
        @Test()
        @DisplayName("Should throw exception when rental state is different from confirmed")
        void shouldThrowExceptionWhenRentalStateIsDifferentFromConfirmed(
        ) {
            val request = factory.tenantUpdateRequestModel();

            UUID tenantId = tenant.getId();
            UUID rentalId = rental.getId();

            rental.setState(RentalState.PENDING);

            when(userRepositoryMock.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rental));
            when(presenter.isDone()).thenReturn(false);

            sut.cancelRental(presenter, request);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            verify(presenter).prepareFailView(exceptionCaptor.capture());

            Exception captured = exceptionCaptor.getValue();
            assertThat(captured)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rental is not in a valid state to be cancelled");

        }
    }
}

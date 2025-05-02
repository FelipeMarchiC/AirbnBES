package br.ifsp.application.rental.update;

import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import org.hibernate.mapping.Any;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TenantUpdateRentalServiceTest {
    @Mock private JpaUserRepository userRepositoryMock;
    @Mock private JpaRentalRepository rentalRepositoryMock;
    @InjectMocks private TenantUpdateRentalService sut;

    private AutoCloseable closeable;

    private User owner;
    private User tenant;
    private Property property;
    private Rental rental;

    @BeforeEach
    void setupService() {
        closeable = MockitoAnnotations.openMocks(this);

        sut = new TenantUpdateRentalService(rentalRepositoryMock);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setup() {
        tenant = User.builder()
                .id(UUID.fromString("cce0262c-5acc-4669-99bd-2ad504f08421"))
                .name("Federico")
                .lastname("Giallo")
                .email("federico_giallo@gmail.com")
                .password("YUtsODIxw4djaUVkcDg5MWF2ZElF")
                .role(Role.USER)
                .ownedProperties(new ArrayList<>())
                .build();

        property = Property.builder()
                .id(UUID.fromString("2c67b2e4-0ff2-44bb-851f-3ff0b5223b00"))
                .name("Basilisca Ambrosius")
                .description("An concert hall somewhere in France.")
                .dailyRate(new Price(new BigDecimal("250.00")))
                .address(Address.builder()
                        .number("92")
                        .street("Via Theodoros")
                        .city("Landen")
                        .state("Laterano")
                        .postalCode("LD22")
                        .build())
                .owner(owner)
                .rentals(new ArrayList<>())
                .build();

        rental = Rental.builder()
                .id(UUID.fromString("a13f29e7-7399-431d-b429-f28a4090dc4e"))
                .user(tenant)
                .property(property)
                .startDate(LocalDate.parse("2025-01-01"))
                .endDate(LocalDate.parse("2025-04-30"))
                .value(new Price(BigDecimal.valueOf(187934.87)))
                .state(RentalState.CONFIRMED)
                .build();

        property.addRental(rental);

        owner = User.builder()
                .id(UUID.fromString("5215e4da-50ff-42fb-9f55-b8e04a6ff82a"))
                .name("Arturia")
                .lastname("Giallo")
                .email("decrescendo@gmail.com")
                .password("dGhlYmxhY2tzaWxlbmNl")
                .role(Role.USER)
                .ownedProperties(List.of(property))
                .build();
    }

    @Nested
    @Tag("UnitTest")
    @DisplayName("Testing valid equivalent classes")
    class TestingValidEquivalentClasses {
        @Tag("TDD")
        @Test()
        @DisplayName("Should cancel rental")
        void shouldCancelRental(
        ) {
            UUID tenantId = tenant.getId();
            UUID rentalId = rental.getId();

            when(userRepositoryMock.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.of(rental));
            when(rentalRepositoryMock.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Rental rental = sut.cancelRental(tenantId, rentalId);

            verify(userRepositoryMock, times(1)).findById(tenantId);
            verify(rentalRepositoryMock, times(1)).save(rental);

            assertThat(rental.getState()).isEqualTo(RentalState.CANCELLED);
        }
    }
}

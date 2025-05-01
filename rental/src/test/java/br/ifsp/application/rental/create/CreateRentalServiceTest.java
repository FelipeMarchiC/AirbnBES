package br.ifsp.application.rental.create;

import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateRentalServiceTest {
    @Mock private JpaUserRepository userRepositoryMock;
    @Mock private JpaPropertyRepository propertyRepositoryMock;
    @Mock private JpaRentalRepository rentalRepositoryMock;
    @InjectMocks private CreateRentalService sut;

    private Clock fixedClock;
    private AutoCloseable closeable;

    private User owner;
    private User tenant;
    private Property property;

    @BeforeEach
    void setupService() {
        closeable = MockitoAnnotations.openMocks(this);

        fixedClock = Clock.fixed(
                LocalDate.of(1801, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant(),
                ZoneOffset.UTC
        );

        sut = new CreateRentalService(
                userRepositoryMock,
                propertyRepositoryMock,
                rentalRepositoryMock,
                fixedClock
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setup() {
        tenant = new User(
                UUID.fromString("e924925c-2a7b-4cab-b938-0d6398ecc78a"),
                "Hindley",
                "Earnshaw",
                "hindleyearnshaw@outlook.com",
                "bGV0c2dvZ2FtYmxpbmc=",
                Role.USER,
                new ArrayList<>()
        );

        owner = new User(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                "Catherine",
                "Earnshaw",
                "cathy_earnshaw@outlook.com",
                "ZGVsZXRlYWxsY2F0aHk=",
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
                owner
        );
    }

    @Nested
    @DisplayName("Testing valid equivalent classes")
    class TestingValidEquivalentClasses {
        @Tag("TDD")
        @Tag("UnitTest")
        @ParameterizedTest(name = "[{index}]: should create rental from {2} to {3}")
        @CsvSource({
                "e924925c-2a7b-4cab-b938-0d6398ecc78a, 123e4567-e89b-12d3-a456-426614174000, 1801-02-22, 1801-03-22"
        })
        @DisplayName("Should create when rental start date is before endDate")
        void shouldCreateRentalWhenStartDateIsBeforeEndDate(UUID userId, UUID propertyId, LocalDate startDate, LocalDate endDate) {
            when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(tenant));
            when(propertyRepositoryMock.findById(propertyId)).thenReturn(Optional.of(property));
            when(rentalRepositoryMock.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Rental rental = sut.registerRental(userId, propertyId, startDate, endDate);

            assertThat(rental).isNotNull();
        }

    }

    @Nested
    @DisplayName("Testing invalid equivalent classes")
    class TestingInvalidEquivalentClasses {
        @Tag("TDD")
        @Tag("UnitTest")
        @Test()
        @DisplayName("Should throw exception when startDate is in the past")
        void shouldThrowExceptionWhenStartDateIsInThePast() {
            var startDate = LocalDate.parse("1800-02-22");
            var endDate = LocalDate.parse("1802-03-22");

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> sut.registerRental(UUID.randomUUID(), UUID.randomUUID(), startDate, endDate))
                    .withMessageContaining("Rental cannot start in the past");
        }
    }
}

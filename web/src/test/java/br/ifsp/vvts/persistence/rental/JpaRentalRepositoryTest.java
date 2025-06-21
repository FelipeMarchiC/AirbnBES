package br.ifsp.vvts.persistence.rental;

import br.ifsp.application.property.repository.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.domain.shared.valueobjects.Price;
import br.ifsp.vvts.utils.EntityBuilder;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JpaRentalRepositoryTest {
    protected static final Faker faker = new Faker();
    @Autowired
    protected JpaUserRepository userRepository;
    @Autowired
    protected JpaPropertyRepository propertyRepository;
    @Autowired
    protected JpaRentalRepository sut;
    String adminPassword = faker.internet().password();
    UserEntity admin = EntityBuilder.createRandomAdmin(adminPassword);

    @BeforeAll
    void setup() {
        userRepository.deleteAll();
        userRepository.save(admin);

    }

    @AfterAll
    public void tearDown() {
        propertyRepository.deleteAll();
        sut.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    class FindRentalsByOverlapAndState {
        PropertyEntity property;
        LocalDate baseStart;
        LocalDate baseEnd;
        UUID referenceId;

        @BeforeEach
        void setup() {
            propertyRepository.deleteAll();
            sut.deleteAll();

            property = EntityBuilder.createProperty(
                    "11111111-1111-1111-1111-111111111111",
                    "Test Property", "Test Desc", admin,
                    "1000", "1", "Street", "City", "ST", "00000-000"
            );
            propertyRepository.save(property);

            baseStart = LocalDate.of(2025, 7, 1);
            baseEnd = LocalDate.of(2025, 7, 10);

            referenceId = UUID.randomUUID();

            sut.save(RentalEntity.builder()
                    .id(referenceId)
                    .userEntity(admin)
                    .propertyEntity(property)
                    .startDate(baseStart)
                    .endDate(baseEnd)
                    .value(new Price(BigDecimal.valueOf(1000).multiply(BigDecimal.valueOf(9))))
                    .state(RentalState.CONFIRMED)
                    .build()
            );

            sut.save(RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(property)
                    .startDate(baseStart.minusDays(10))
                    .endDate(baseStart.minusDays(1))
                    .state(RentalState.CONFIRMED)
                    .value(new Price(BigDecimal.ZERO))
                    .build()
            );

            sut.save(RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(property)
                    .startDate(baseEnd.plusDays(1))
                    .endDate(baseEnd.plusDays(5))
                    .state(RentalState.CONFIRMED)
                    .value(new Price(BigDecimal.ZERO))
                    .build()
            );

            sut.save(RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(property)
                    .startDate(baseStart.plusDays(2))
                    .endDate(baseEnd.plusDays(2))
                    .state(RentalState.CONFIRMED)
                    .value(new Price(BigDecimal.ZERO))
                    .build()
            );

            sut.save(RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(property)
                    .startDate(baseStart.minusDays(2))
                    .endDate(baseStart.plusDays(2))
                    .state(RentalState.CONFIRMED)
                    .value(new Price(BigDecimal.ZERO))
                    .build()
            );

            sut.save(RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(property)
                    .startDate(baseStart.minusDays(5))
                    .endDate(baseEnd.plusDays(5))
                    .state(RentalState.CONFIRMED)
                    .value(new Price(BigDecimal.ZERO))
                    .build()
            );

            sut.save(RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(property)
                    .startDate(baseStart.plusDays(1))
                    .endDate(baseEnd.minusDays(1))
                    .state(RentalState.PENDING)
                    .value(new Price(BigDecimal.ZERO))
                    .build()
            );
        }

        @Test
        @Tag("PersistenceTest")
        @Tag("IntegrationTest")
        @DisplayName("should get only confirmed overlapping rentals")
        void shouldGetOnlyConfirmedOverlappingRentals() {
            List<RentalEntity> conflicts = sut.findRentalsByOverlapAndState(
                    property.getId(), RentalState.CONFIRMED, baseStart, baseEnd, referenceId
            );

            Assertions.assertEquals(3, conflicts.size());
            conflicts.forEach(r -> {
                Assertions.assertEquals(RentalState.CONFIRMED, r.getState());
                Assertions.assertTrue(r.getStartDate().isBefore(baseEnd) && r.getEndDate().isAfter(baseStart));
            });
        }

        @Test
        @Tag("PersistenceTest")
        @Tag("IntegrationTest")
        @DisplayName("should exclude the rental with given id")
        void shouldExcludeGivenRentalId() {
            UUID skipId = UUID.randomUUID();
            sut.save(RentalEntity.builder()
                    .id(skipId)
                    .userEntity(admin)
                    .propertyEntity(property)
                    .startDate(baseStart.plusDays(1))
                    .endDate(baseEnd.minusDays(1))
                    .state(RentalState.CONFIRMED)
                    .value(new Price(BigDecimal.ZERO))
                    .build()
            );

            List<RentalEntity> conflicts = sut.findRentalsByOverlapAndState(
                    property.getId(), RentalState.CONFIRMED, baseStart, baseEnd, skipId
            );

            Assertions.assertFalse(conflicts.stream().anyMatch(r -> r.getId().equals(skipId)));
        }

        @Test
        @Tag("PersistenceTest")
        @Tag("IntegrationTest")
        @DisplayName("should include reference rental when passing non-existent rentalId")
        void shouldIncludeReferenceWhenDifferentRentalId() {
            UUID randomId = UUID.randomUUID();
            List<RentalEntity> conflicts = sut.findRentalsByOverlapAndState(
                    property.getId(), RentalState.CONFIRMED, baseStart, baseEnd, randomId
            );

            Assertions.assertEquals(4, conflicts.size());
            Assertions.assertTrue(conflicts.stream().anyMatch(r -> r.getId().equals(referenceId)));
        }
    }
}

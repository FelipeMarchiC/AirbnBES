package br.ifsp.vvts.persistence.property;

import br.ifsp.application.property.repository.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import br.ifsp.vvts.utils.EntityBuilder;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JpaPropertyRepositoryTest {
    @Autowired
    protected JpaUserRepository userRepository;
    @Autowired
    protected JpaPropertyRepository sut;
    @Autowired
    protected JpaRentalRepository rentalRepository;
    protected static final Faker faker = new Faker();
    String adminPassword = faker.internet().password();
    UserEntity admin = EntityBuilder.createRandomAdmin(adminPassword);

    @BeforeAll
    void setup() {
        userRepository.deleteAll();
        userRepository.save(admin);

    }

    @AfterAll
    public void tearDown() {
        sut.deleteAll();
        rentalRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    class SelectPropertiesByCity {
        @BeforeEach
        void setup() {
            sut.deleteAll();
            rentalRepository.deleteAll();
            List<PropertyEntity> properties = List.of(
                    EntityBuilder.createPropertyWithLocation(admin, Address.builder().city("São Paulo").street("Rua da Liberdade").number("101").state("SP").postalCode("01000-000").build()),
                    EntityBuilder.createPropertyWithLocation(admin, Address.builder().city("Rio de Janeiro").street("Avenida Atlântica").number("202").state("RJ").postalCode("22021-001").build()),
                    EntityBuilder.createPropertyWithLocation(admin, Address.builder().city("São Paulo").street("Rua Augusta").number("1500").state("SP").postalCode("01305-100").build()),
                    EntityBuilder.createPropertyWithLocation(admin, Address.builder().city("Curitiba").street("Rua XV de Novembro").number("303").state("PR").postalCode("80020-310").build()),
                    EntityBuilder.createPropertyWithLocation(admin, Address.builder().city("Belo Horizonte").street("Avenida Afonso Pena").number("707").state("MG").postalCode("30130-003").build()),
                    EntityBuilder.createPropertyWithLocation(admin, Address.builder().city("São Paulo").street("Avenida Paulista").number("900").state("SP").postalCode("01310-100").build()),
                    EntityBuilder.createPropertyWithLocation(admin, Address.builder().city("Recife").street("Rua do Bom Jesus").number("606").state("PE").postalCode("50030-170").build()),
                    EntityBuilder.createPropertyWithLocation(admin, Address.builder().city("Fortaleza").street("Avenida Beira Mar").number("707").state("CE").postalCode("60165-121").build()),
                    EntityBuilder.createPropertyWithLocation(admin, Address.builder().city("Curitiba").street("Avenida Batel").number("1111").state("PR").postalCode("80420-090").build()),
                    EntityBuilder.createPropertyWithLocation(admin, Address.builder().city("Salvador").street("Rua Chile").number("1212").state("BA").postalCode("40020-000").build())
            );
            sut.saveAll(properties);
        }


        @ParameterizedTest
        @CsvSource({"São Paulo,3", "Curitiba,2", "Belo Horizonte, 1", "São Carlos,0"})
        @Tag("PersistenceTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return the places with The Correct City")
        void shouldReturnThePlacesWithTheCorrectCity(String city, int expectedSize) {
            List<PropertyEntity> result = sut.findByLocation(city);
            assertThat(result).hasSize(expectedSize);
            assertThat(result.stream().allMatch(p -> p.getAddress().getCity().equals(city))).isTrue();
        }

    }

    @Nested
    class SelectPropertiesByPriceRange {
        @BeforeEach
        void setup() {
            sut.deleteAll();
            rentalRepository.deleteAll();

            userRepository.save(admin);
            List<PropertyEntity> properties = List.of(
                    EntityBuilder.createPropertyWithPrice(admin, 50.0),
                    EntityBuilder.createPropertyWithPrice(admin, 1200.00),
                    EntityBuilder.createPropertyWithPrice(admin, 850.50),
                    EntityBuilder.createPropertyWithPrice(admin, 3000.00),
                    EntityBuilder.createPropertyWithPrice(admin, 450.75),
                    EntityBuilder.createPropertyWithPrice(admin, 999.99),
                    EntityBuilder.createPropertyWithPrice(admin, 5000.00),
                    EntityBuilder.createPropertyWithPrice(admin, 1999.95),
                    EntityBuilder.createPropertyWithPrice(admin, 150.00),
                    EntityBuilder.createPropertyWithPrice(admin, 2750.30),
                    EntityBuilder.createPropertyWithPrice(admin, 10.00)
            );
            sut.saveAll(properties);
        }

        @ParameterizedTest
        @CsvSource({
                "0.00,100.00,2",
                "100.00,1000.00,4",
                "1000.00,2000.00,2",
                "2000.00,4000.00,2",
                "0.00,6000.00,11"
        })
        @Tag("PersistenceTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return properties with dailyRate between min and max")
        void shouldFindByDailyRateBetween(double min, double max, int expectedSize) {
            List<PropertyEntity> result = sut.findByDailyRateBetween(min, max);

            BigDecimal minBd = new BigDecimal(min);
            BigDecimal maxBd = new BigDecimal(max);

            assertThat(result).hasSize(expectedSize);
            assertThat(result).allMatch(p -> {
                BigDecimal amt = p.getDailyRate().getAmount();
                return amt.compareTo(minBd) >= 0 && amt.compareTo(maxBd) <= 0;
            });
        }

        @Test
        @Tag("PersistenceTest")
        @Tag("IntegrationTest")
        @DisplayName("Should include properties whose dailyRate is exactly the min or max")
        void shouldIncludeEndpoints() {
            BigDecimal valueB = new BigDecimal("3000.00");
            double valueD = 3000.00;

            List<PropertyEntity> result = sut.findByDailyRateBetween(valueD, valueD);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getDailyRate().getAmount()).isEqualByComparingTo(valueB);
        }

        @ParameterizedTest
        @CsvSource({
                "1000.00,100.00,0",
                "0.01,0.00,0",
        })
        @Tag("PersistenceTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return properties with dailyRate between min and max, including inverted ranges")
        void shouldFindByDailyRateBetween_IncludingInvertedRanges(double min, double max, int expectedSize) {
            List<PropertyEntity> result = sut.findByDailyRateBetween(min, max);

            BigDecimal minBd = new BigDecimal(min);
            BigDecimal maxBd = new BigDecimal(max);

            assertThat(result).hasSize(expectedSize);

            assertThat(result).allMatch(p -> {
                BigDecimal amt = p.getDailyRate().getAmount();
                return amt.compareTo(minBd) >= 0 && amt.compareTo(maxBd) <= 0;
            });
        }
    }

    @Nested
    class SelectAvailablePropertiesByPeriod {
        PropertyEntity p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16;

        @BeforeEach
        void setup() {
            sut.deleteAll();
            rentalRepository.deleteAll();

            p1 = createProperty("f0d6c22f-3e9a-44c4-b4b5-fd67e5edacfc", "Apartamento Central", "Apartamento de 2 quartos no centro da cidade", admin, "1500", "101", "Rua da Liberdade", "São Paulo", "SP", "01000-000");
            p2 = createProperty("8dfc6c64-1e7b-4ec1-8450-d20e160c7093", "Casa de Praia", "Casa de frente para o mar", admin, "2500", "202", "Rua da Liberdade", "Rio de Janeiro", "RJ", "22000-000");
            p3 = createProperty("6a1f9b3e-0d42-4a6d-ae7d-12b50a0b9c77", "Cobertura Luxuosa", "Cobertura com vista panorâmica", admin, "3500", "303", "Av. Paulista", "São Paulo", "SP", "01311-000");
            p4 = createProperty("b8de4a91-e9d9-4f6a-9481-ccf74cf71b01", "Sítio Tranquilo", "Sítio com lago e área verde", admin, "1200", "1", "Estrada do Campo", "Campinas", "SP", "13000-000");
            p5 = createProperty("c51f5172-bc2c-4bb4-9841-12b3933c94e0", "Flat Executivo", "Flat próximo ao centro financeiro", admin, "1800", "405", "Rua Augusta", "São Paulo", "SP", "01413-000");
            p6 = createProperty("25f913ed-0326-4633-b4ac-32b9477c3edb", "Chalé na Montanha", "Chalé com lareira e vista para as montanhas", admin, "1400", "99", "Alameda das Neves", "Campos do Jordão", "SP", "12460-000");
            p7 = createProperty("84e89a9a-2c9e-4ec1-8904-112b679e4715", "Estúdio Moderno", "Estúdio compacto e funcional", admin, "1000", "12", "Rua Bela Cintra", "São Paulo", "SP", "01415-000");
            p8 = createProperty("d30a5ae6-df95-42d0-8c5f-5e1a10f7f2ea", "Casa na Serra", "Casa aconchegante na serra", admin, "1600", "77", "Estrada da Serra", "Petrópolis", "RJ", "25680-000");
            p9 = createProperty("3fa11c74-79cc-499f-9f08-3e1f1c1d66e2", "Apartamento Beira-Mar", "Apartamento com varanda de frente para o mar", admin, "2200", "501", "Av. Atlântica", "Rio de Janeiro", "RJ", "22070-000");
            p10 = createProperty("4efb93c6-263d-47c1-9e38-e40a4d2d9627", "Loft Industrial", "Loft com decoração industrial e open space", admin, "2000", "888", "Rua Harmonia", "São Paulo", "SP", "05435-000");
            p11 = createProperty("a1c2b3d4-e5f6-7890-1234-567890abcdef", "Casa Histórica", "Casa charmosa no centro histórico", admin, "1900", "50", "Rua das Flores", "Ouro Preto", "MG", "35400-000");
            p12 = createProperty("b2c3d4e5-f6a7-8901-2345-67890abcdef0", "Fazenda com Gado", "Ampla fazenda para pecuária e lazer", admin, "4000", "S/N", "Rodovia do Gado", "Campo Grande", "MS", "79000-000");
            p13 = createProperty("c3d4e5f6-a7b8-9012-3456-7890abcdef12", "Bangalô na Lagoa", "Bangalô com acesso direto à lagoa", admin, "2800", "10", "Avenida Beira Mar", "Florianópolis", "SC", "88000-000");
            p14 = createProperty("d4e5f6a7-b8c9-0123-4567-890abcdef34", "Loja Comercial", "Espaço amplo para comércio", admin, "3000", "123", "Praça Central", "Salvador", "BA", "40000-000");
            p15 = createProperty("e5f6a7b8-c9d0-1234-5678-90abcdef56", "Apartamento de Luxo", "Apartamento com acabamento de alto padrão", admin, "5000", "1500", "Rua da Alta", "Belo Horizonte", "MG", "30000-000");
            p16 = createProperty("f6a7b8c9-d0e1-2345-6789-0abcdef789", "Chácara Arborizada", "Chácara com vasta área verde e piscina", admin, "1700", "20", "Estrada da Água", "Goiânia", "GO", "74000-000");

            List<PropertyEntity> properties = List.of(
                    p1, p2, p3, p4, p5, p6, p7, p8,
                    p9, p10, p11, p12, p13, p14, p15, p16
            );

            sut.saveAll(properties);


            RentalEntity r1 = RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(properties.get(0))
                    .startDate(LocalDate.of(2025, 8, 5))
                    .endDate(LocalDate.of(2025, 8, 10))
                    .value(new Price(properties.get(0).getDailyRate().getAmount().multiply(BigDecimal.valueOf(5))))
                    .state(RentalState.CONFIRMED)
                    .build();

            RentalEntity r2 = RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(properties.get(3))
                    .startDate(LocalDate.of(2025, 9, 15))
                    .endDate(LocalDate.of(2025, 9, 22))
                    .value(new Price(properties.get(3).getDailyRate().getAmount().multiply(BigDecimal.valueOf(7))))
                    .state(RentalState.CONFIRMED)
                    .build();

            RentalEntity r3 = RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(properties.get(5))
                    .startDate(LocalDate.of(2025, 10, 1))
                    .endDate(LocalDate.of(2025, 10, 4))
                    .value(new Price(properties.get(5).getDailyRate().getAmount().multiply(BigDecimal.valueOf(3))))
                    .state(RentalState.PENDING)
                    .build();
            RentalEntity r4 = RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(properties.get(2))
                    .startDate(LocalDate.of(2024, 12, 1))
                    .endDate(LocalDate.of(2024, 12, 5))
                    .value(new Price(properties.get(2).getDailyRate().getAmount().multiply(BigDecimal.valueOf(4))))
                    .state(RentalState.CONFIRMED)
                    .build();

            RentalEntity r5 = RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(properties.get(4))
                    .startDate(LocalDate.of(2025, 1, 10))
                    .endDate(LocalDate.of(2025, 1, 12))
                    .value(new Price(properties.get(4).getDailyRate().getAmount().multiply(BigDecimal.valueOf(2))))
                    .state(RentalState.DENIED)
                    .build();

            RentalEntity r6 = RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(properties.get(6))
                    .startDate(LocalDate.of(2025, 11, 20))
                    .endDate(LocalDate.of(2025, 11, 25))
                    .value(new Price(properties.get(6).getDailyRate().getAmount().multiply(BigDecimal.valueOf(5))))
                    .state(RentalState.PENDING)
                    .build();

            RentalEntity r7 = RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(properties.get(7))
                    .startDate(LocalDate.of(2025, 12, 10))
                    .endDate(LocalDate.of(2025, 12, 15))
                    .value(new Price(properties.get(7).getDailyRate().getAmount().multiply(BigDecimal.valueOf(5))))
                    .state(RentalState.CONFIRMED)
                    .build();

            RentalEntity r8 = RentalEntity.builder()
                    .id(UUID.randomUUID())
                    .userEntity(admin)
                    .propertyEntity(properties.get(8))
                    .startDate(LocalDate.of(2025, 12, 20))
                    .endDate(LocalDate.of(2025, 12, 27))
                    .value(new Price(properties.get(8).getDailyRate().getAmount().multiply(BigDecimal.valueOf(7))))
                    .state(RentalState.CONFIRMED)
                    .build();

            rentalRepository.saveAll(List.of(r1, r2, r3, r4, r5, r6, r7, r8));

        }

        @Test
        @Tag("PersistenceTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return available properties for a free period")
        void shouldReturnAvailablePropertiesForFreePeriod() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            LocalDate end = LocalDate.of(2025, 7, 10);

            List<PropertyEntity> available = sut.findAvailablePropertiesByPeriod(start, end);

            assertThat(available).hasSize(16);
        }

        @Test
        @Tag("PersistenceTest")
        @Tag("IntegrationTest")
        @DisplayName("Should not return properties with confirmed rentals during period")
        void shouldExcludePropertiesWithConfirmedRentalsDuringPeriod() {
            LocalDate start = LocalDate.of(2025, 8, 6);
            LocalDate end = LocalDate.of(2025, 8, 8);

            List<PropertyEntity> available = sut.findAvailablePropertiesByPeriod(start, end);

            assertThat(available).doesNotContain(p1);
        }

        @Test
        @Tag("PersistenceTest")
        @Tag("IntegrationTest")
        @DisplayName("Should not exclude properties with pending or denied rentals")
        void shouldNotExcludePendingOrDeniedRentals() {
            LocalDate start = LocalDate.of(2025, 10, 1);
            LocalDate end = LocalDate.of(2025, 10, 4);

            List<PropertyEntity> available = sut.findAvailablePropertiesByPeriod(start, end);

            assertThat(available).contains(p6, p7);

        }

        private PropertyEntity createProperty(
                String id, String name, String desc, UserEntity owner, String price,
                String number, String street, String city, String state, String cep
        ) {
            return PropertyEntity.builder()
                    .id(UUID.fromString(id))
                    .name(name)
                    .description(desc)
                    .owner(owner)
                    .dailyRate(new Price(new BigDecimal(price)))
                    .address(Address.builder()
                            .number(number)
                            .street(street)
                            .city(city)
                            .state(state)
                            .postalCode(cep)
                            .build())
                    .rentals(emptyList())
                    .build();
        }

    }
}

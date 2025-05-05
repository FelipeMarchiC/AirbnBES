package br.ifsp.vvts;

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
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

import static java.util.Collections.emptyList ;

@Component
public class DataLoader implements CommandLineRunner {
    private final JpaUserRepository userRepository;
    private final JpaPropertyRepository propertyRepository;
    private final JpaRentalRepository rentalRepository;
    private final PasswordEncoder encoder;

    public DataLoader(
            JpaUserRepository userRepository,
            JpaPropertyRepository propertyRepository,
            JpaRentalRepository rentalRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.rentalRepository = rentalRepository;
        this.encoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User user1 = User.builder()
                .id(UUID.fromString("9b96aa7e-4f1b-4c2f-b273-3b9f7c9b1a01"))
                .name("Amiya").lastname("Rhodes")
                .email("amiya@rhodesisland.com")
                .password(encoder.encode("bes"))
                .role(Role.ADMIN)
                .ownedProperties(emptyList())
                .build();

        User user2 = User.builder()
                .id(UUID.fromString("2e13c4b8-b5a2-4af4-b881-c0298bfe5132"))
                .name("Catherine")
                .lastname("Earnshaw")
                .email("cathy@wuthering-heights.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        User user3 = User.builder()
                .id(UUID.fromString("8dfc6c64-1e7b-4ec1-8450-d20e160c7092"))
                .name("Coragem").lastname("o Cão Covarde")
                .email("uriel@eustacio.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        User user4 = User.builder()
                .id(UUID.fromString("a7e6f768-b8cb-4a14-9205-84fd94961cb1"))
                .name("Roberto")
                .lastname("Abadia")
                .email("roberto-abadia@gmail.com")
                .password(encoder.encode("bes")).role(Role.ADMIN)
                .ownedProperties(emptyList())
                .build();

        User user5 = User.builder()
                .id(UUID.fromString("1a8a419c-4d84-4a7a-9c18-282df27819d3"))
                .name("Vin").lastname("Diesel")
                .email("familia@outlook.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        User user6 = User.builder()
                .id(UUID.fromString("3bd202db-2a38-4979-b13c-2ff0a6f31f76"))
                .name("Pedro")
                .lastname("Bosta")
                .email("barro@gmail.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        User user7 = User.builder()
                .id(UUID.fromString("f25b5c68-2ac9-4bc4-9f30-3d3b2a6832c4"))
                .name("Goleiro")
                .lastname("Bruno")
                .email("macarrao@penguinlogistics.com")
                .password(encoder.encode("bes"))
                .ownedProperties(emptyList())
                .role(Role.USER)
                .build();

        User user8 = User.builder()
                .id(UUID.fromString("c8b4708c-bdb2-42bb-90e7-e77bcb44eb11"))
                .name("Rock")
                .lastname("Roll")
                .email("rock@roll.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        User user9 = User.builder()
                .id(UUID.fromString("9440aeed-5809-4df3-950e-b839c3cf25a5"))
                .name("Lazaro")
                .lastname("Assassino")
                .email("lazaromanhunter@realoficial.com")
                .password(encoder.encode("bes"))
                .ownedProperties(emptyList())
                .role(Role.USER)
                .build();

        User user10 = User.builder()
                .id(UUID.fromString("bd869fa0-4b6f-4bc2-8d84-c164a49c58da"))
                .name("Nickel")
                .lastname("Back")
                .email("photograph@faraway.com")
                .password(encoder.encode("bes"))
                .ownedProperties(emptyList())
                .role(Role.USER)
                .build();

        userRepository.saveAll(List.of(user1, user2, user3, user4, user5, user6, user7, user8, user9, user10));


        Property p1 = Property.builder()
                .id(UUID.fromString("f0d6c22f-3e9a-44c4-b4b5-fd67e5edacfc"))
                .name("Apartamento Central")
                .description("Apartamento de 2 quartos no centro da cidade")
                .owner(user1)
                .dailyRate(new Price(new BigDecimal("1500")))
                .address(
                        Address.builder()
                                .number("101")
                                .street("Rua da Liberdade")
                                .city("São Paulo")
                                .state("SP")
                                .postalCode("01000-000")
                                .build()
                )
                .rentals(emptyList())
                .build();

        Property p2 = Property.builder()
                .id(UUID.fromString("8dfc6c64-1e7b-4ec1-8450-d20e160c7093"))
                .name("Casa de Praia").description("Casa de frente para o mar")
                .owner(user2)
                .dailyRate(new Price(new BigDecimal("2500")))
                .address(
                        Address.builder()
                                .number("202")
                                .street("Rua da Liberdade")
                                .city("Rio de Janeiro")
                                .state("RJ")
                                .postalCode("22000-000")
                                .build()
                )
                .rentals(emptyList())
                .build();

        Property p3 = Property.builder()
                .id(UUID.fromString("6a1f9b3e-0d42-4a6d-ae7d-12b50a0b9c77"))
                .name("Cobertura Luxuosa")
                .description("Cobertura com vista panorâmica")
                .owner(user3)
                .dailyRate(new Price(new BigDecimal("3500")))
                .address(
                        Address.builder()
                                .number("303")
                                .street("Av. Paulista")
                                .city("São Paulo")
                                .state("SP")
                                .postalCode("01311-000")
                                .build()
                )
                .rentals(emptyList())
                .build();

        Property p4 = Property.builder()
                .id(UUID.fromString("b8de4a91-e9d9-4f6a-9481-ccf74cf71b01"))
                .name("Sítio Tranquilo")
                .description("Sítio com lago e área verde")
                .owner(user4)
                .dailyRate(new Price(new BigDecimal("1200")))
                .address(
                        Address.builder()
                                .number("1")
                                .street("Estrada do Campo")
                                .city("Campinas")
                                .state("SP")
                                .postalCode("13000-000")
                                .build()
                )
                .rentals(emptyList())
                .build();

        Property p5 = Property.builder()
                .id(UUID.fromString("c51f5172-bc2c-4bb4-9841-12b3933c94e0"))
                .name("Flat Executivo")
                .description("Flat próximo ao centro financeiro")
                .owner(user1)
                .dailyRate(new Price(new BigDecimal("1800")))
                .address(
                        Address.builder()
                                .number("405")
                                .street("Rua Augusta")
                                .city("São Paulo")
                                .state("SP")
                                .postalCode("01413-000")
                                .build()
                )
                .rentals(emptyList())
                .build();

        Property p6 = Property.builder()
                .id(UUID.fromString("25f913ed-0326-4633-b4ac-32b9477c3edb"))
                .name("Chalé na Montanha")
                .description("Chalé com lareira e vista para as montanhas")
                .owner(user2)
                .dailyRate(new Price(new BigDecimal("1400")))
                .address(
                        Address.builder()
                                .number("99")
                                .street("Alameda das Neves")
                                .city("Campos do Jordão")
                                .state("SP")
                                .postalCode("12460-000")
                                .build()
                )
                .rentals(emptyList())
                .build();

        Property p7 = Property.builder()
                .id(UUID.fromString("84e89a9a-2c9e-4ec1-8904-112b679e4715"))
                .name("Estúdio Moderno")
                .description("Estúdio compacto e funcional")
                .owner(user3)
                .dailyRate(new Price(new BigDecimal("1000")))
                .address(
                        Address.builder()
                                .number("12")
                                .street("Rua Bela Cintra")
                                .city("São Paulo")
                                .state("SP")
                                .postalCode("01415-000")
                                .build()
                )
                .rentals(emptyList())
                .build();

        Property p8 = Property.builder()
                .id(UUID.fromString("d30a5ae6-df95-42d0-8c5f-5e1a10f7f2ea"))
                .name("Casa na Serra")
                .description("Casa aconchegante na serra")
                .owner(user4)
                .dailyRate(new Price(new BigDecimal("1600")))
                .address(
                        Address.builder()
                                .number("77")
                                .street("Estrada da Serra")
                                .city("Petrópolis")
                                .state("RJ")
                                .postalCode("25680-000")
                                .build()
                )
                .rentals(emptyList())
                .build();

        Property p9 = Property.builder()
                .id(UUID.fromString("3fa11c74-79cc-499f-9f08-3e1f1c1d66e2"))
                .name("Apartamento Beira-Mar")
                .description("Apartamento com varanda de frente para o mar")
                .owner(user1)
                .dailyRate(new Price(new BigDecimal("2200")))
                .address(
                        Address.builder()
                                .number("501")
                                .street("Av. Atlântica")
                                .city("Rio de Janeiro")
                                .state("RJ")
                                .postalCode("22070-000")
                                .build()
                )
                .rentals(emptyList())
                .build();

        Property p10 = Property.builder()
                .id(UUID.fromString("4efb93c6-263d-47c1-9e38-e40a4d2d9627"))
                .name("Loft Industrial")
                .description("Loft com decoração industrial e open space")
                .owner(user2)
                .dailyRate(new Price(new BigDecimal("2000")))
                .address(
                        Address.builder()
                                .number("888")
                                .street("Rua Harmonia")
                                .city("São Paulo")
                                .state("SP")
                                .postalCode("05435-000")
                                .build()
                )
                .rentals(emptyList())
                .build();

        propertyRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10));

        Rental r1 = Rental.builder()
                .id(UUID.randomUUID())
                .user(user5)
                .property(p1)
                .startDate(LocalDate.of(2025, 3, 10))
                .endDate(LocalDate.of(2025, 3, 15))
                .value(new Price(p1.getDailyRate().getAmount().multiply(BigDecimal.valueOf(5))))
                .state(RentalState.CONFIRMED)
                .build();

        Rental r2 = Rental.builder()
                .id(UUID.randomUUID())
                .user(user6)
                .property(p2)
                .startDate(LocalDate.of(2025, 4, 5))
                .endDate(LocalDate.of(2025, 4, 12))
                .value(new Price(p2.getDailyRate().getAmount().multiply(BigDecimal.valueOf(7))))
                .state(RentalState.DENIED)
                .build();

        Rental r3 = Rental.builder()
                .id(UUID.randomUUID())
                .user(user7)
                .property(p3)
                .startDate(LocalDate.of(2025, 5, 1))
                .endDate(LocalDate.of(2025, 5, 6))
                .state(RentalState.CONFIRMED)
                .value(new Price(p3.getDailyRate().getAmount().multiply(BigDecimal.valueOf(5))))
                .build();

        Rental r4 = Rental.builder()
                .id(UUID.randomUUID())
                .user(user8)
                .property(p4)
                .startDate(LocalDate.of(2025, 6, 20))
                .endDate(LocalDate.of(2025, 6, 30))
                .value(new Price(p4.getDailyRate().getAmount().multiply(BigDecimal.valueOf(10))))
                .state(RentalState.CONFIRMED)
                .build();

        Rental r5 = Rental.builder()
                .id(UUID.randomUUID())
                .user(user9)
                .property(p5)
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 10))
                .value(new Price(p5.getDailyRate().getAmount().multiply(BigDecimal.valueOf(9))))
                .state(RentalState.DENIED)
                .build();

        Rental r6 = Rental.builder()
                .id(UUID.randomUUID())
                .user(user10)
                .property(p6)
                .startDate(LocalDate.of(2025, 8, 15))
                .endDate(LocalDate.of(2025, 8, 20))
                .value(new Price(p6.getDailyRate().getAmount().multiply(BigDecimal.valueOf(5))))
                .state(RentalState.CONFIRMED)
                .build();

        Rental r7 = Rental.builder()
                .id(UUID.randomUUID())
                .user(user1)
                .property(p7)
                .startDate(LocalDate.of(2025, 9, 10))
                .endDate(LocalDate.of(2025, 9, 18))
                .value(new Price(p7.getDailyRate().getAmount().multiply(BigDecimal.valueOf(9))))
                .state(RentalState.CONFIRMED)
                .build();

        Rental r8 = Rental.builder()
                .id(UUID.randomUUID())
                .user(user2)
                .property(p8)
                .startDate(LocalDate.of(2025, 10, 2))
                .endDate(LocalDate.of(2025, 10, 7))
                .value(new Price(p8.getDailyRate().getAmount().multiply(BigDecimal.valueOf(5))))

                .state(RentalState.CONFIRMED)
                .build();

        Rental r9 = Rental.builder()
                .id(UUID.randomUUID())
                .user(user3)
                .property(p9)
                .startDate(LocalDate.of(2025, 11, 12))
                .endDate(LocalDate.of(2025, 11, 19))
                .value(new Price(p9.getDailyRate().getAmount().multiply(BigDecimal.valueOf(7))))
                .state(RentalState.PENDING)
                .build();

        Rental r10 = Rental
                .builder()
                .id(UUID.randomUUID())
                .user(user4)
                .property(p10)
                .startDate(LocalDate.of(2024, 12, 1))
                .endDate(LocalDate.of(2024, 12, 5))
                .value(new Price(p10.getDailyRate().getAmount().multiply(BigDecimal.valueOf(4))))
                .state(RentalState.PENDING).build();

        rentalRepository.saveAll(List.of(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10));
    }
}





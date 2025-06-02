package br.ifsp.vvts;

import br.ifsp.application.property.repository.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

import static java.util.Collections.emptyList;

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
        UserEntity user1 = UserEntity.builder()
                .id(UUID.fromString("a7e6f768-b8cb-4a14-9205-84fd94961cb1"))
                .name("Roberto")
                .lastname("Abadia")
                .email("roberto-abadia@gmail.com")
                .password(encoder.encode("bes"))
                .role(Role.ADMIN)
                .ownedProperties(emptyList())
                .build();

        UserEntity user2 = UserEntity.builder()
                .id(UUID.fromString("9b96aa7e-4f1b-4c2f-b273-3b9f7c9b1a01"))
                .name("Amiya").lastname("Rhodes")
                .email("amiya@rhodesisland.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        UserEntity user3 = UserEntity.builder()
                .id(UUID.fromString("2e13c4b8-b5a2-4af4-b881-c0298bfe5132"))
                .name("Catherine")
                .lastname("Earnshaw")
                .email("cathy@wuthering-heights.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        UserEntity user4 = UserEntity.builder()
                .id(UUID.fromString("8dfc6c64-1e7b-4ec1-8450-d20e160c7092"))
                .name("Coragem").lastname("o Cão Covarde")
                .email("uriel@eustacio.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        UserEntity user5 = UserEntity.builder()
                .id(UUID.fromString("1a8a419c-4d84-4a7a-9c18-282df27819d3"))
                .name("Vin").lastname("Diesel")
                .email("familia@outlook.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        UserEntity user6 = UserEntity.builder()
                .id(UUID.fromString("3bd202db-2a38-4979-b13c-2ff0a6f31f76"))
                .name("Pedro")
                .lastname("Bosta")
                .email("barro@gmail.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        UserEntity user7 = UserEntity.builder()
                .id(UUID.fromString("f25b5c68-2ac9-4bc4-9f30-3d3b2a6832c4"))
                .name("Goleiro")
                .lastname("Bruno")
                .email("macarrao@penguinlogistics.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        UserEntity user8 = UserEntity.builder()
                .id(UUID.fromString("c8b4708c-bdb2-42bb-90e7-e77bcb44eb11"))
                .name("Rock")
                .lastname("Roll")
                .email("rock@roll.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        UserEntity user9 = UserEntity.builder()
                .id(UUID.fromString("9440aeed-5809-4df3-950e-b839c3cf25a5"))
                .name("Lazaro")
                .lastname("Assassino")
                .email("lazaromanhunter@realoficial.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        UserEntity user10 = UserEntity.builder()
                .id(UUID.fromString("bd869fa0-4b6f-4bc2-8d84-c164a49c58da"))
                .name("Nickel")
                .lastname("Back")
                .email("photograph@faraway.com")
                .password(encoder.encode("bes"))
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();

        userRepository.saveAll(List.of(user1, user2, user3, user4, user5, user6, user7, user8, user9, user10));

        List<PropertyEntity> properties = List.of(
                createProperty("f0d6c22f-3e9a-44c4-b4b5-fd67e5edacfc", "Apartamento Central", "Apartamento de 2 quartos no centro da cidade", user1, "1500", "101", "Rua da Liberdade", "São Paulo", "SP", "01000-000"),
                createProperty("8dfc6c64-1e7b-4ec1-8450-d20e160c7093", "Casa de Praia", "Casa de frente para o mar", user1, "2500", "202", "Rua da Liberdade", "Rio de Janeiro", "RJ", "22000-000"),
                createProperty("6a1f9b3e-0d42-4a6d-ae7d-12b50a0b9c77", "Cobertura Luxuosa", "Cobertura com vista panorâmica", user1, "3500", "303", "Av. Paulista", "São Paulo", "SP", "01311-000"),
                createProperty("b8de4a91-e9d9-4f6a-9481-ccf74cf71b01", "Sítio Tranquilo", "Sítio com lago e área verde", user1, "1200", "1", "Estrada do Campo", "Campinas", "SP", "13000-000"),
                createProperty("c51f5172-bc2c-4bb4-9841-12b3933c94e0", "Flat Executivo", "Flat próximo ao centro financeiro", user1, "1800", "405", "Rua Augusta", "São Paulo", "SP", "01413-000"),
                createProperty("25f913ed-0326-4633-b4ac-32b9477c3edb", "Chalé na Montanha", "Chalé com lareira e vista para as montanhas", user1, "1400", "99", "Alameda das Neves", "Campos do Jordão", "SP", "12460-000"),
                createProperty("84e89a9a-2c9e-4ec1-8904-112b679e4715", "Estúdio Moderno", "Estúdio compacto e funcional", user1, "1000", "12", "Rua Bela Cintra", "São Paulo", "SP", "01415-000"),
                createProperty("d30a5ae6-df95-42d0-8c5f-5e1a10f7f2ea", "Casa na Serra", "Casa aconchegante na serra", user1, "1600", "77", "Estrada da Serra", "Petrópolis", "RJ", "25680-000"),
                createProperty("3fa11c74-79cc-499f-9f08-3e1f1c1d66e2", "Apartamento Beira-Mar", "Apartamento com varanda de frente para o mar", user1, "2200", "501", "Av. Atlântica", "Rio de Janeiro", "RJ", "22070-000"),
                createProperty("4efb93c6-263d-47c1-9e38-e40a4d2d9627", "Loft Industrial", "Loft com decoração industrial e open space", user1, "2000", "888", "Rua Harmonia", "São Paulo", "SP", "05435-000")
        );

        propertyRepository.saveAll(properties);
        RentalEntity r1 = RentalEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(user4) // Uriel
                .propertyEntity(properties.get(0)) // Apartamento Central
                .startDate(LocalDate.of(2025, 8, 5))
                .endDate(LocalDate.of(2025, 8, 10))
                .value(new Price(properties.get(0).getDailyRate().getAmount().multiply(BigDecimal.valueOf(5))))
                .state(RentalState.CONFIRMED)
                .build();

        RentalEntity r2 = RentalEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(user4) // Uriel
                .propertyEntity(properties.get(3)) // Sítio Tranquilo
                .startDate(LocalDate.of(2025, 9, 15))
                .endDate(LocalDate.of(2025, 9, 22))
                .value(new Price(properties.get(3).getDailyRate().getAmount().multiply(BigDecimal.valueOf(7))))
                .state(RentalState.CONFIRMED)
                .build();

        RentalEntity r3 = RentalEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(user4) // Uriel
                .propertyEntity(properties.get(5)) // Chalé na Montanha
                .startDate(LocalDate.of(2025, 10, 1))
                .endDate(LocalDate.of(2025, 10, 4))
                .value(new Price(properties.get(5).getDailyRate().getAmount().multiply(BigDecimal.valueOf(3))))
                .state(RentalState.PENDING)
                .build();
        RentalEntity r4 = RentalEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(user4)
                .propertyEntity(properties.get(2)) // Cobertura Luxuosa
                .startDate(LocalDate.of(2024, 12, 1))
                .endDate(LocalDate.of(2024, 12, 5))
                .value(new Price(properties.get(2).getDailyRate().getAmount().multiply(BigDecimal.valueOf(4))))
                .state(RentalState.CONFIRMED)
                .build();

        RentalEntity r5 = RentalEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(user4)
                .propertyEntity(properties.get(4)) // Flat Executivo
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 1, 12))
                .value(new Price(properties.get(4).getDailyRate().getAmount().multiply(BigDecimal.valueOf(2))))
                .state(RentalState.DENIED)
                .build();

// Aluguéis futuros
        RentalEntity r6 = RentalEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(user4)
                .propertyEntity(properties.get(6)) // Estúdio Moderno
                .startDate(LocalDate.of(2025, 11, 20))
                .endDate(LocalDate.of(2025, 11, 25))
                .value(new Price(properties.get(6).getDailyRate().getAmount().multiply(BigDecimal.valueOf(5))))
                .state(RentalState.PENDING)
                .build();

        RentalEntity r7 = RentalEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(user4)
                .propertyEntity(properties.get(7)) // Casa na Serra
                .startDate(LocalDate.of(2025, 12, 10))
                .endDate(LocalDate.of(2025, 12, 15))
                .value(new Price(properties.get(7).getDailyRate().getAmount().multiply(BigDecimal.valueOf(5))))
                .state(RentalState.CONFIRMED)
                .build();

        RentalEntity r8 = RentalEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(user4)
                .propertyEntity(properties.get(8)) // Apartamento Beira-Mar
                .startDate(LocalDate.of(2026, 1, 5))
                .endDate(LocalDate.of(2026, 1, 10))
                .value(new Price(properties.get(8).getDailyRate().getAmount().multiply(BigDecimal.valueOf(5))))
                .state(RentalState.EXPIRED)
                .build();

        rentalRepository.saveAll(List.of(r1, r2, r3,r4,r5,r6,r7,r8));

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

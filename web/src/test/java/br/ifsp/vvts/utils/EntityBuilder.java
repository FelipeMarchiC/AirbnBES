package br.ifsp.vvts.utils;

import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

import static java.util.Collections.emptyList;

@RequiredArgsConstructor
public class EntityBuilder {
    private static Faker faker = Faker.instance();

    public static UserEntity createRandomAdmin(String password) {
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .name(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .password(password)
                .role(Role.ADMIN)
                .ownedProperties(emptyList())
                .build();
        return user;
    }

    public static UserEntity createRandomUser(String password) {
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .name(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .password(password)
                .role(Role.USER)
                .ownedProperties(emptyList())
                .build();
        return user;
    }

    public static PropertyEntity createRandomProperty(UserEntity owner) {
        UUID uuid = UUID.randomUUID();
        BigDecimal randomPrice = BigDecimal.valueOf(faker.number().numberBetween(50, 500));
        PropertyEntity property = PropertyEntity.builder()
                .id(uuid)
                .name(faker.company().name() + " Apartment")
                .description(faker.lorem().sentence())
                .owner(owner)
                .dailyRate(new Price(randomPrice))
                .address(Address.builder()
                        .number(faker.address().buildingNumber())
                        .street(faker.address().streetName())
                        .city(faker.address().city())
                        .state(faker.address().stateAbbr())
                        .postalCode(faker.address().zipCode())
                        .build())
                .rentals(emptyList())
                .build();

        return property;
    }

}

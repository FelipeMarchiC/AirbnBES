package br.ifsp.vvts.utils;

import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.models.user.UserEntity;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static java.util.Collections.emptyList;

@RequiredArgsConstructor
public class EntityBuilder {
    private static Faker faker = Faker.instance();
    private static JpaUserRepository userRepository;
    private final PasswordEncoder encoder;

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
        userRepository.save(user);
        return user;
    }

    public static UserEntity createRandomUser(String password) {
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .name(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .password(password)
                .role(Role.ADMIN)
                .ownedProperties(emptyList())
                .build();
        userRepository.save(user);
        return user;
    }
}

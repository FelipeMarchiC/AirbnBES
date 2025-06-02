package br.ifsp.domain.models.user;

import br.ifsp.domain.models.property.PropertyEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserEntity Tests")
class UserEntityTest {

    private UUID userId;
    private String name;
    private String lastname;
    private String email;
    private String password;
    private Role role;
    private List<PropertyEntity> ownedProperties;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        name = "John";
        lastname = "Doe";
        email = "john.doe@example.com";
        password = "securePassword123";
        role = Role.USER;
        ownedProperties = Collections.emptyList();
    }

    @Nested
    @DisplayName("Structural Tests")
    @Tag("Structural")
    class StructuralTests {

        @Test
        @DisplayName("Should create UserEntity with Builder")
        @Tag("UnitTest")
        void shouldCreateUserEntityWithBuilder() {
            UserEntity user = UserEntity.builder()
                    .id(userId)
                    .name(name)
                    .lastname(lastname)
                    .email(email)
                    .password(password)
                    .role(role)
                    .ownedProperties(ownedProperties)
                    .build();

            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(userId);
            assertThat(user.getName()).isEqualTo(name);
            assertThat(user.getLastname()).isEqualTo(lastname);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getRole()).isEqualTo(role);
            assertThat(user.getOwnedProperties()).isEqualTo(ownedProperties);
        }

        @Test
        @DisplayName("Should create UserEntity with AllArgsConstructor")
        @Tag("UnitTest")
        void shouldCreateUserEntityWithAllArgsConstructor() {
            UserEntity user = new UserEntity(userId, name, lastname, email, password, role, ownedProperties);

            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(userId);
            assertThat(user.getName()).isEqualTo(name);
            assertThat(user.getLastname()).isEqualTo(lastname);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getRole()).isEqualTo(role);
            assertThat(user.getOwnedProperties()).isEqualTo(ownedProperties);
        }

        @Test
        @DisplayName("Should create UserEntity with NoArgsConstructor")
        @Tag("UnitTest")
        void shouldCreateUserEntityWithNoArgsConstructor() {
            UserEntity user = new UserEntity();
            assertThat(user).isNotNull();
            user.setId(userId);
            user.setName(name);
            user.setLastname(lastname);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(role);
            user.setOwnedProperties(ownedProperties);

            assertThat(user.getId()).isEqualTo(userId);
            assertThat(user.getName()).isEqualTo(name);
            assertThat(user.getLastname()).isEqualTo(lastname);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getRole()).isEqualTo(role);
            assertThat(user.getOwnedProperties()).isEqualTo(ownedProperties);
        }

        @Test
        @DisplayName("Should test equals method for identity")
        @Tag("UnitTest")
        void shouldTestEqualsMethodForIdentity() {
            UserEntity user1 = UserEntity.builder()
                    .id(userId)
                    .name(name)
                    .lastname(lastname)
                    .email(email)
                    .password(password)
                    .role(role)
                    .build();

            UserEntity user2 = UserEntity.builder()
                    .id(userId)
                    .name(name)
                    .lastname(lastname)
                    .email(email)
                    .password(password)
                    .role(role)
                    .build();

            UserEntity user3 = UserEntity.builder()
                    .id(UUID.randomUUID())
                    .name("Jane")
                    .lastname("Smith")
                    .email("jane.smith@example.com")
                    .password("anotherPassword")
                    .role(Role.ADMIN)
                    .build();

            assertThat(user1).isEqualTo(user1);
            assertThat(user1).isEqualTo(user2);
            assertThat(user1).isNotEqualTo(user3);
            assertThat(user1).isNotEqualTo(null);
            assertThat(user1).isNotEqualTo(new Object());
        }

        @Test
        @DisplayName("Should test hashCode method consistency")
        @Tag("UnitTest")
        void shouldTestHashCodeMethodConsistency() {
            UserEntity user1 = UserEntity.builder()
                    .id(userId)
                    .name(name)
                    .lastname(lastname)
                    .email(email)
                    .password(password)
                    .role(role)
                    .build();

            UserEntity user2 = UserEntity.builder()
                    .id(userId)
                    .name(name)
                    .lastname(lastname)
                    .email(email)
                    .password(password)
                    .role(role)
                    .build();

            assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        }

        @Test
        @DisplayName("Should test toString method not null or empty")
        @Tag("UnitTest")
        void shouldTestToStringMethodNotNullOrEmpty() {
            UserEntity user = UserEntity.builder()
                    .id(userId)
                    .name(name)
                    .lastname(lastname)
                    .email(email)
                    .password(password)
                    .role(role)
                    .build();
            assertThat(user.toString()).isNotBlank();
            assertThat(user.toString()).contains(name, email, userId.toString());
        }
    }

    @Nested
    @DisplayName("UserDetails Interface Tests")
    @Tag("UnitTest")
    class UserDetailsInterfaceTests {

        private UserEntity user;

        @BeforeEach
        void setupUserDetails() {
            user = UserEntity.builder()
                    .id(userId)
                    .name(name)
                    .lastname(lastname)
                    .email(email)
                    .password(password)
                    .role(role)
                    .ownedProperties(ownedProperties)
                    .build();
        }

        @Test
        @DisplayName("Should return correct authorities based on role")
        void shouldReturnCorrectAuthorities() {
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
            assertThat(authorities).hasSize(1);
            assertThat(authorities.stream()
                    .anyMatch(authority -> authority.getAuthority().equals(role.name())))
                    .isTrue();
        }

        @Test
        @DisplayName("Should return correct password")
        void shouldReturnCorrectPassword() {
            assertThat(user.getPassword()).isEqualTo(password);
        }

        @Test
        @DisplayName("Should return email as username")
        void shouldReturnEmailAsUsername() {
            assertThat(user.getUsername()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should return true for isAccountNonExpired by default")
        void shouldReturnTrueForIsAccountNonExpired() {
            assertThat(user.isAccountNonExpired()).isTrue();
        }

        @Test
        @DisplayName("Should return true for isAccountNonLocked by default")
        void shouldReturnTrueForIsAccountNonLocked() {
            assertThat(user.isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName("Should return true for isCredentialsNonExpired by default")
        void shouldReturnTrueForIsCredentialsNonExpired() {
            assertThat(user.isCredentialsNonExpired()).isTrue();
        }

        @Test
        @DisplayName("Should return true for isEnabled by default")
        void shouldReturnTrueForIsEnabled() {
            assertThat(user.isEnabled()).isTrue();
        }
    }
}
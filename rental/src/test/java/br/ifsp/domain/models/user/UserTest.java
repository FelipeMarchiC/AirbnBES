package br.ifsp.domain.models.user;

import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("Structural")
@Tag("UnitTest")
@DisplayName("User Domain Model Tests")
class UserTest {

    private UUID userId;
    private String name;
    private String lastname;
    private String email;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        name = "John";
        lastname = "Doe";
        email = "john.doe@example.com";
        user = User.builder()
                .id(userId)
                .name(name)
                .lastname(lastname)
                .email(email)
                .ownedProperties(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("User Constructor Tests")
    class ConstructorTests {
        @Test
        @DisplayName("Should create a User with all valid arguments")
        void shouldCreateUserWithAllValidArguments() {
            List<Property> properties = new ArrayList<>();
            Property property = Property.builder()
                    .id(UUID.randomUUID())
                    .name("Beach House")
                    .description("Nice house near the beach")
                    .dailyRate(new Price(new BigDecimal("150.00")))
                    .address(new Address("123", "Street A", "City X", "State Y", "12345-678"))
                    .owner(user)
                    .rentals(new ArrayList<>())
                    .build();
            properties.add(property);

            User userWithProperties = User.builder()
                    .id(userId)
                    .name(name)
                    .lastname(lastname)
                    .email(email)
                    .ownedProperties(properties)
                    .build();

            assertNotNull(userWithProperties);
            assertEquals(userId, userWithProperties.getId());
            assertEquals(name, userWithProperties.getName());
            assertEquals(lastname, userWithProperties.getLastname());
            assertEquals(email, userWithProperties.getEmail());
            assertFalse(userWithProperties.getOwnedProperties().isEmpty());
            assertEquals(1, userWithProperties.getOwnedProperties().size());
            assertEquals(property, userWithProperties.getOwnedProperties().get(0));
        }

    }


}
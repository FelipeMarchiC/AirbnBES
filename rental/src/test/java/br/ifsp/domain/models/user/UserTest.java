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

        @Test
        @DisplayName("Should create a User with null owned properties list")
        void shouldCreateUserWithNullOwnedPropertiesList() {
            User userWithNullProperties = User.builder()
                    .id(userId)
                    .name(name)
                    .lastname(lastname)
                    .email(email)
                    .ownedProperties(null)
                    .build();

            assertNotNull(userWithNullProperties);
            assertTrue(userWithNullProperties.getOwnedProperties().isEmpty());
        }

        @Test
        @DisplayName("Should create a User with an empty owned properties list")
        void shouldCreateUserWithEmptyOwnedPropertiesList() {
            User userWithEmptyProperties = User.builder()
                    .id(userId)
                    .name(name)
                    .lastname(lastname)
                    .email(email)
                    .ownedProperties(new ArrayList<>())
                    .build();

            assertNotNull(userWithEmptyProperties);
            assertTrue(userWithEmptyProperties.getOwnedProperties().isEmpty());
        }
    }

    @Nested
    @DisplayName("User Getters Tests")
    class GettersTests {
        @Test
        @DisplayName("Should return the correct user ID")
        void shouldReturnCorrectId() {
            assertEquals(userId, user.getId());
        }

        @Test
        @DisplayName("Should return the correct user name")
        void shouldReturnCorrectName() {
            assertEquals(name, user.getName());
        }

        @Test
        @DisplayName("Should return the correct user lastname")
        void shouldReturnCorrectLastname() {
            assertEquals(lastname, user.getLastname());
        }

        @Test
        @DisplayName("Should return the correct user email")
        void shouldReturnCorrectEmail() {
            assertEquals(email, user.getEmail());
        }

        @Test
        @DisplayName("Should return an unmodifiable list of owned properties")
        void shouldReturnUnmodifiableOwnedPropertiesList() {
            List<Property> ownedProperties = user.getOwnedProperties();
            assertNotNull(ownedProperties);
            assertTrue(ownedProperties.isEmpty());
            assertThrows(UnsupportedOperationException.class, () -> ownedProperties.add(null));
        }
    }

    @Nested
    @DisplayName("User Property Management Tests")
    class PropertyManagementTests {
        private UUID propertyId;
        private String propertyName;
        private String description;
        private Price dailyRate;
        private Address address;

        @BeforeEach
        void setUpProperties() {
            propertyId = UUID.randomUUID();
            propertyName = "Cozy Apartment";
            description = "A lovely apartment in the city center.";
            dailyRate = new Price(new BigDecimal("100.00"));
            address = new Address("456", "Main St", "Metropolis", "NY", "98765-432");
        }

        @Test
        @DisplayName("Should create a new property and add it to owned properties")
        void shouldCreatePropertyAndAddToOwnedProperties() {
            Property newProperty = user.createProperty(propertyId, propertyName, description, dailyRate, address);

            assertNotNull(newProperty);
            assertEquals(propertyId, newProperty.getId());
            assertEquals(propertyName, newProperty.getName());
            assertEquals(description, newProperty.getDescription());
            assertEquals(dailyRate, newProperty.getDailyRate());
            assertEquals(address, newProperty.getAddress());
            assertEquals(user, newProperty.getOwner());
            assertTrue(newProperty.getRentals().isEmpty());

            List<Property> ownedProperties = user.getOwnedProperties();
            assertFalse(ownedProperties.isEmpty());
            assertEquals(1, ownedProperties.size());
            assertTrue(ownedProperties.contains(newProperty));
        }


    }
}
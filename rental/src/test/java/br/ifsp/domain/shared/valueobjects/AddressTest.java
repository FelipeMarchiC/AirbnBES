package br.ifsp.domain.shared.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@Tag("Structural")
@Tag("UnitTest")
@DisplayName("Address Value Object Tests")
class AddressTest {

    private static final String DEFAULT_NUMBER = "123";
    private static final String DEFAULT_STREET = "Main St";
    private static final String DEFAULT_CITY = "Springfield";
    private static final String DEFAULT_STATE = "IL";
    private static final String DEFAULT_POSTAL_CODE = "62704";

    private Address createDefaultAddress() {
        return new Address(DEFAULT_NUMBER, DEFAULT_STREET, DEFAULT_CITY, DEFAULT_STATE, DEFAULT_POSTAL_CODE);
    }

    @Nested
    @DisplayName("Constructors and Getters")
    class ConstructorsAndGettersTests {

        @Test
        @DisplayName("Should create an Address with all arguments")
        void shouldCreateAddressWithAllArguments() {
            Address address = new Address(DEFAULT_NUMBER, DEFAULT_STREET, DEFAULT_CITY, DEFAULT_STATE, DEFAULT_POSTAL_CODE);

            assertThat(address).isNotNull();
            assertThat(address.getNumber()).isEqualTo(DEFAULT_NUMBER);
            assertThat(address.getStreet()).isEqualTo(DEFAULT_STREET);
            assertThat(address.getCity()).isEqualTo(DEFAULT_CITY);
            assertThat(address.getState()).isEqualTo(DEFAULT_STATE);
            assertThat(address.getPostalCode()).isEqualTo(DEFAULT_POSTAL_CODE);
        }
        @Test
        @DisplayName("Should create an Address using builder pattern")
        void shouldCreateAddressUsingBuilderPattern() {
            Address address = Address.builder()
                    .number(DEFAULT_NUMBER)
                    .street(DEFAULT_STREET)
                    .city(DEFAULT_CITY)
                    .state(DEFAULT_STATE)
                    .postalCode(DEFAULT_POSTAL_CODE)
                    .build();

            assertThat(address).isNotNull();
            assertThat(address.getNumber()).isEqualTo(DEFAULT_NUMBER);
            assertThat(address.getStreet()).isEqualTo(DEFAULT_STREET);
            assertThat(address.getCity()).isEqualTo(DEFAULT_CITY);
            assertThat(address.getState()).isEqualTo(DEFAULT_STATE);
            assertThat(address.getPostalCode()).isEqualTo(DEFAULT_POSTAL_CODE);
        }

        @Test
        @DisplayName("Should instantiate Address using no-args constructor (for JPA/Lombok)")
        void shouldInstantiateAddressUsingNoArgsConstructor() {
            try {
                Address address = Address.class.getDeclaredConstructor().newInstance();
                assertThat(address).isNotNull();
                // Asserts para campos nulos, pois @NonNull não é validado no construtor padrão
                assertThat(address.getNumber()).isNull();
                assertThat(address.getStreet()).isNull();
                assertThat(address.getCity()).isNull();
                assertThat(address.getState()).isNull();
                assertThat(address.getPostalCode()).isNull();
            } catch (Exception e) {
                fail("Failed to instantiate Address using no-args constructor: " + e.getMessage());
            }
        }
    }
}
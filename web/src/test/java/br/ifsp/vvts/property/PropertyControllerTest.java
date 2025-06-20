package br.ifsp.vvts.property;

import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.vvts.utils.BaseApiIntegrationTest;
import br.ifsp.vvts.utils.EntityBuilder;
import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class PropertyControllerTest extends BaseApiIntegrationTest {
    private static final Faker faker = new Faker();
    private UserEntity user;
    private UserEntity admin;
    private String userPassword;
    private String adminPassword;

    @BeforeEach
    void setUp() {
        userPassword = faker.internet().password();
        adminPassword = faker.internet().password();
        user = registerUser(userPassword);
        admin = registerAdminUser(adminPassword);
    }

    @Nested
    class ListAllProperties {

        private Response sendRequest(String token) {
            return given()
                    .contentType(ContentType.JSON)
                    .port(port)
                    .header("Authorization", "Bearer " + token)
                    .when().get("/api/v1/property")
                    .then()
                    .log().all()
                    .extract().response();
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should list all properties to user")
        void listAllPropertiesForRegularUser() {
            String token = authenticate(user.getEmail(), userPassword);
            PropertyEntity property = EntityBuilder.createRandomProperty(user);
            propertyRepository.save(property);

            Response response = sendRequest(token);

            assertThat(response.getBody().jsonPath().getList("id")).contains(property.getId().toString());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should list all properties to admin")
        void listAllPropertiesForAdmin() {
            String token = authenticate(admin.getEmail(), adminPassword);

            PropertyEntity property = EntityBuilder.createRandomProperty(admin);
            propertyRepository.save(property);

            Response response = sendRequest(token);

            assertThat(response.getBody().jsonPath().getList("id")).contains(property.getId().toString());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return empty list when no properties are registered")
        void listAllPropertiesWhenNoPropertiesAreRegistered() {
            String token = authenticate(admin.getEmail(), adminPassword);

            Response response = sendRequest(token);

            assertThat(response.getBody().jsonPath().getList("id")).isEmpty();
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return forbidden (401) when user is not authorized")
        void listAllPropertiesWhenUserIsNotAuthorized() {
            String token = faker.rickAndMorty().character();

            Response response = sendRequest(token);

            assertThat(response.getStatusCode()).isEqualTo(401);
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return OK (200) if the list is empty")
        void shouldReturnOK200IfTheListIsEmpty() {
            String token = authenticate(user.getEmail(), userPassword);

            Response response = sendRequest(token);

            assertThat(response.getStatusCode()).isEqualTo(200);
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return OK (200) if the list is not empty")
        void shouldReturnOK200IfTheListIsNotEmpty() {
            String token = authenticate(user.getEmail(), userPassword);

            PropertyEntity property = EntityBuilder.createRandomProperty(user);
            propertyRepository.save(property);

            Response response = sendRequest(token);

            assertThat(response.getStatusCode()).isEqualTo(200);
        }

    }

    @Nested
    class GetPropertyById {

        private Response sendRequest(String propertyId, String token) {
            return given()
                    .contentType(ContentType.JSON)
                    .port(port)
                    .header("Authorization", "Bearer " + token)
                    .when().get("/api/v1/property/" + propertyId)
                    .then()
                    .log().all()
                    .extract().response();
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return property by id to user")
        void getPropertyByIdForRegularUser() {
            String token = authenticate(user.getEmail(), userPassword);
            PropertyEntity property = EntityBuilder.createRandomProperty(user);
            propertyRepository.save(property);
            propertyRepository.save(EntityBuilder.createRandomProperty(user));
            propertyRepository.save(EntityBuilder.createRandomProperty(user));
            propertyRepository.save(EntityBuilder.createRandomProperty(user));

            Response response = sendRequest(property.getId().toString(), token);

            assertThat(response.getBody().jsonPath().getString("id")).isEqualTo(property.getId().toString());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return property by id if the user it's not the owner too")
        void getPropertyByIdForRegularUserButNotTheOwner() {
            String token = authenticate(admin.getEmail(), adminPassword);

            PropertyEntity property = EntityBuilder.createRandomProperty(user);
            propertyRepository.save(property);
            propertyRepository.save(EntityBuilder.createRandomProperty(user));
            propertyRepository.save(EntityBuilder.createRandomProperty(user));
            propertyRepository.save(EntityBuilder.createRandomProperty(user));

            Response response = sendRequest(property.getId().toString(), token);

            assertThat(response.getBody().jsonPath().getString("id")).isEqualTo(property.getId().toString());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return 400 if send a invalid propertyId")
        void getPropertyByIdWithWrongPropertyId() {
            String token = authenticate(user.getEmail(), userPassword);
            PropertyEntity property = EntityBuilder.createRandomProperty(user);
            propertyRepository.save(property);
            propertyRepository.save(EntityBuilder.createRandomProperty(user));
            propertyRepository.save(EntityBuilder.createRandomProperty(user));
            propertyRepository.save(EntityBuilder.createRandomProperty(user));

            Response response = sendRequest(faker.rickAndMorty().character(), token);

            assertThat(response.getStatusCode()).isEqualTo(400);
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return 404 if send a propertyId that does not exists")
        void getPropertyByIdWithNonExistentPropertyId() {
            String token = authenticate(user.getEmail(), userPassword);
            PropertyEntity property = EntityBuilder.createRandomProperty(user);
            propertyRepository.save(property);
            propertyRepository.save(EntityBuilder.createRandomProperty(user));
            propertyRepository.save(EntityBuilder.createRandomProperty(user));
            propertyRepository.save(EntityBuilder.createRandomProperty(user));

            Response response = sendRequest(UUID.randomUUID().toString(), token);

            assertThat(response.getStatusCode()).isEqualTo(404);
        }
    }

    @Nested
    class FilterByLocation {

        private Response sendRequest(String location, String token) {
            return given()
                    .contentType(ContentType.JSON)
                    .port(port)
                    .header("Authorization", "Bearer " + token)
                    .queryParam("location", location)
                    .when().get("/api/v1/property/location")
                    .then()
                    .log().all()
                    .extract().response();
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return properties by exact location")
        void shouldReturnPropertiesByExactLocation() {
            String token = authenticate(user.getEmail(), userPassword);
            Address l1 = Address.builder()
                    .number(faker.address().buildingNumber())
                    .street(faker.address().streetName())
                    .city(faker.address().city())
                    .state(faker.address().stateAbbr())
                    .postalCode(faker.address().zipCode())
                    .build();
            PropertyEntity p1 = EntityBuilder.createPropertyWithLocation(user, l1);
            propertyRepository.save(p1);

            Response response = sendRequest(l1.toString(), token);

            assertThat(response.jsonPath().getList("id")).contains(p1.getId().toString());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return properties by the state")
        void shouldReturnPropertiesByTheState() {
            String token = authenticate(user.getEmail(), userPassword);
            Address l1 = Address.builder()
                    .number(faker.address().buildingNumber())
                    .street(faker.address().streetName())
                    .city(faker.address().city())
                    .state(faker.address().stateAbbr())
                    .postalCode(faker.address().zipCode())
                    .build();

            PropertyEntity p1 = EntityBuilder.createPropertyWithLocation(user, l1);
            propertyRepository.save(p1);

            Response response = sendRequest(l1.getState(), token);

            assertThat(response.jsonPath().getList("id")).contains(p1.getId().toString());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return properties by the number")
        void shouldReturnPropertiesByTheNumber() {
            String token = authenticate(user.getEmail(), userPassword);
            Address l1 = Address.builder()
                    .number(faker.address().buildingNumber())
                    .street(faker.address().streetName())
                    .city(faker.address().city())
                    .state(faker.address().stateAbbr())
                    .postalCode(faker.address().zipCode())
                    .build();

            PropertyEntity p1 = EntityBuilder.createPropertyWithLocation(user, l1);
            propertyRepository.save(p1);

            Response response = sendRequest(l1.getNumber(), token);

            assertThat(response.jsonPath().getList("id")).contains(p1.getId().toString());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return properties by the street")
        void shouldReturnPropertiesByTheStreet() {
            String token = authenticate(user.getEmail(), userPassword);
            Address l1 = Address.builder()
                    .number(faker.address().buildingNumber())
                    .street(faker.address().streetName())
                    .city(faker.address().city())
                    .state(faker.address().stateAbbr())
                    .postalCode(faker.address().zipCode())
                    .build();

            PropertyEntity p1 = EntityBuilder.createPropertyWithLocation(user, l1);
            propertyRepository.save(p1);

            Response response = sendRequest(l1.getStreet(), token);

            assertThat(response.jsonPath().getList("id")).contains(p1.getId().toString());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return properties by the city")
        void shouldReturnPropertiesByTheCity() {
            String token = authenticate(user.getEmail(), userPassword);
            Address l1 = Address.builder()
                    .number(faker.address().buildingNumber())
                    .street(faker.address().streetName())
                    .city(faker.address().city())
                    .state(faker.address().stateAbbr())
                    .postalCode(faker.address().zipCode())
                    .build();

            PropertyEntity p1 = EntityBuilder.createPropertyWithLocation(user, l1);
            propertyRepository.save(p1);

            Response response = sendRequest(l1.getCity(), token);

            assertThat(response.jsonPath().getList("id")).contains(p1.getId().toString());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return properties by the postal code")
        void shouldReturnPropertiesByThePostalCode() {
            String token = authenticate(user.getEmail(), userPassword);
            Address l1 = Address.builder()
                    .number(faker.address().buildingNumber())
                    .street(faker.address().streetName())
                    .city(faker.address().city())
                    .state(faker.address().stateAbbr())
                    .postalCode(faker.address().zipCode())
                    .build();

            PropertyEntity p1 = EntityBuilder.createPropertyWithLocation(user, l1);
            propertyRepository.save(p1);

            Response response = sendRequest(l1.getPostalCode(), token);

            assertThat(response.jsonPath().getList("id")).contains(p1.getId().toString());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return 400 if location is null")
        void shouldReturn400IfLocationIsNull() {
            String token = authenticate(user.getEmail(), userPassword);

            Response response = sendRequest(null, token);

            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(response.jsonPath().getList("id")).isEmpty();
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return 400 when location is blank")
        void shouldReturn400IfLocationIsBlank() {
            String token = authenticate(user.getEmail(), userPassword);

            Response response = sendRequest("   ", token);

            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(response.jsonPath().getList("id")).isEmpty();
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return empty when location is too long")
        void shouldReturnEmptyWhenLocationIsTooLong() {
            String token = authenticate(user.getEmail(), userPassword);

            String longLocation = "a".repeat(300);

            Response response = sendRequest(longLocation, token);

            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(response.jsonPath().getList("id")).isEmpty();
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return empty when location is random garbage string")
        void shouldReturnEmptyWhenLocationIsRandomString() {
            String token = authenticate(user.getEmail(), userPassword);

            Response response = sendRequest(faker.rickAndMorty().character(), token);

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.jsonPath().getList("id")).isEmpty();
        }


    }

    @Nested
    class FilterByPriceRange {

        private Response sendRequest(BigDecimal min, BigDecimal max, String token) {
            return given()
                    .contentType(ContentType.JSON)
                    .port(port)
                    .header("Authorization", "Bearer " + token)
                    .queryParam("min", min)
                    .queryParam("max", max)
                    .when().get("/api/v1/property/price-range")
                    .then()
                    .log().all()
                    .extract().response();
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return only properties within [min, max]")
        void shouldReturnPropertiesWithinRange() {
            String token = authenticate(user.getEmail(), userPassword);
            var p50 = EntityBuilder.createPropertyWithPrice(user, 50.0);
            var p100 = EntityBuilder.createPropertyWithPrice(user, 100.0);
            var p150 = EntityBuilder.createPropertyWithPrice(user, 150.0);
            propertyRepository.saveAll(List.of(p50, p100, p150));

            Response response = sendRequest(BigDecimal.valueOf(60.0), BigDecimal.valueOf(120.0), token);

            assertThat(response.jsonPath().getList("id")).containsExactly(p100.getId().toString());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("should throws if has a value too small or too big")
        void shouldHandleBoundaryPositiveValues() {
            String token = authenticate(user.getEmail(), userPassword);
            var pSmall = EntityBuilder.createPropertyWithPrice(user, 0.000000001);
            propertyRepository.save(pSmall);

            BigDecimal min = BigDecimal.ONE.scaleByPowerOfTen(-Integer.MAX_VALUE);
            BigDecimal max = BigDecimal.ONE.scaleByPowerOfTen(Integer.MAX_VALUE);
            Response response = sendRequest(min, max, token);

            assertThat(response.statusCode()).isEqualTo(400);
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("should return 400 if min is negative")
        void shouldReturn400IfMinIsNegative() {
            String token = authenticate(user.getEmail(), userPassword);
            var pSmall = EntityBuilder.createPropertyWithPrice(user, 1000);
            propertyRepository.save(pSmall);

            BigDecimal min = BigDecimal.valueOf(-1);
            BigDecimal max = BigDecimal.valueOf(1000);
            Response response = sendRequest(min, max, token);

            assertThat(response.statusCode()).isEqualTo(400);
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("should return 400 if max is negative")
        void shouldReturn400IfMaxIsNegative() {
            String token = authenticate(user.getEmail(), userPassword);
            var pSmall = EntityBuilder.createPropertyWithPrice(user, 1000);
            propertyRepository.save(pSmall);

            BigDecimal min = BigDecimal.valueOf(1000);
            BigDecimal max = BigDecimal.valueOf(-1);
            Response response = sendRequest(min, max, token);

            assertThat(response.statusCode()).isEqualTo(400);
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Boundary: zero and positive range")
        void shouldHandleZeroMin() {
            String token = authenticate(user.getEmail(), userPassword);
            var pZero = EntityBuilder.createPropertyWithPrice(user, 0.0);
            var pPositive = EntityBuilder.createPropertyWithPrice(user, 10.0);
            propertyRepository.saveAll(List.of(pZero, pPositive));

            Response response = sendRequest(BigDecimal.ZERO, BigDecimal.valueOf(5.0), token);

            assertThat(response.jsonPath().getList("id")).containsExactly(pZero.getId().toString());
        }

        @ParameterizedTest
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return 400 with the min is greater than max")
        @CsvSource({
                "150,100",
                "200.50,200.49",
                "0.01,0.00",
                "2147483648,2147483647",
        })
        void shouldReturnBadRequestWhenMinIsGreaterThanMax(String minStr, String maxStr) {
            String token = authenticate(user.getEmail(), userPassword);
            BigDecimal min = new BigDecimal(minStr);
            BigDecimal max = new BigDecimal(maxStr);
            Response response = sendRequest(min, max, token);
            assertThat(response.getStatusCode()).isEqualTo(400);
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Unauthorized when missing token")
        void shouldReturnUnauthorizedWithoutToken() {
            Response response = sendRequest(BigDecimal.ZERO, BigDecimal.valueOf(5.0), null);

            assertThat(response.getStatusCode()).isEqualTo(401);
        }
    }
}

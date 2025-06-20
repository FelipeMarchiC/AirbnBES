package br.ifsp.vvts.property;

import br.ifsp.application.user.repository.JpaUserRepository;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class PropertyControllerTest extends BaseApiIntegrationTest {
    //    @Autowired private final JpaPropertyRepository propertyRepository;
    private static final Faker faker = new Faker();
    private UserEntity user;
    private UserEntity admin;
    private String userPassword;
    private String adminPassword;
    @Autowired
    private JpaUserRepository jpaUserRepository;

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
    }
}

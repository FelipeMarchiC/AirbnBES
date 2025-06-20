package br.ifsp.vvts.property;

import br.ifsp.application.property.repository.JpaPropertyRepository;
import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import br.ifsp.vvts.utils.BaseApiIntegrationTest;
import br.ifsp.vvts.utils.EntityBuilder;
import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.apache.maven.surefire.shared.utils.cli.Arg;
import org.apache.maven.surefire.shared.utils.cli.Commandline;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.authentication;
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

        private Response sendListAllPropertiesRequest(String token) {
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

            Response response = sendListAllPropertiesRequest(token);

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

            Response response = sendListAllPropertiesRequest(token);

            assertThat(response.getBody().jsonPath().getList("id")).contains(property.getId().toString());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return empty list when no properties are registered")
        void listAllPropertiesWhenNoPropertiesAreRegistered() {
            String token = authenticate(admin.getEmail(), adminPassword);

            Response response = sendListAllPropertiesRequest(token);

            assertThat(response.getBody().jsonPath().getList("id")).isEmpty();
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return forbidden (401) when user is not authorized")
        void listAllPropertiesWhenUserIsNotAuthorized() {
            String token = faker.rickAndMorty().character();

            Response response = sendListAllPropertiesRequest(token);

            assertThat(response.getStatusCode()).isEqualTo(401);
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @DisplayName("Should return OK (200) if the list is empty")
        void shouldReturnOK200IfTheListIsEmpty() {
            String token = authenticate(user.getEmail(), userPassword);

            Response response = sendListAllPropertiesRequest(token);

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

            Response response = sendListAllPropertiesRequest(token);

            assertThat(response.getStatusCode()).isEqualTo(200);
        }

    }

    @Nested
    class GetPropertyById {

        private Response sendGetPropertyByIdRequest(String propertyId, String token) {
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

            Response response = sendGetPropertyByIdRequest(property.getId().toString(), token);

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

            Response response = sendGetPropertyByIdRequest(property.getId().toString(), token);

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

            Response response = sendGetPropertyByIdRequest(faker.rickAndMorty().character(), token);

            assertThat(response.getStatusCode()).isEqualTo(400);
        }
    }
}

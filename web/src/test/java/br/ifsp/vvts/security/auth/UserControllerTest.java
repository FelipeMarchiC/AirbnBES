package br.ifsp.vvts.security.auth;

import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.vvts.rental.requests.PostRequest;
import br.ifsp.vvts.security.config.JwtService;
import br.ifsp.vvts.utils.BaseApiIntegrationTest;
import com.github.javafaker.Faker;
import io.restassured.response.Response;
import jdk.jfr.Description;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest extends BaseApiIntegrationTest {
    @Nested
    class RegisterUser {
        private Response registerUser(String name, String lastname, String email, String password) {
            RegisterUserRequest postRequest = new RegisterUserRequest(name, lastname, email, password);
            var request = given().contentType("application/json").port(port).body(postRequest);
            return request.when().post("/api/v1/register");
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should create a user successfully and return 201")
        void shouldRegisterUserSuccessfully() {
            Response response = registerUser("Jon", "Snow", "jon.snow@example.com", "Password123!");
            assertEquals(201, response.getStatusCode());
            assertNotNull(response.getBody().jsonPath().get("id"));
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should fail with 409 when email is already in use")
        void shouldFailWhenEmailAlreadyExists() {
            String email = "jon.snow@example.com";
            registerUser("Jon", "Snow", email, "Password123!");

            Response response = registerUser("Jon", "Snow", email, "Password123!");
            assertEquals(409, response.getStatusCode());
        }

        @ParameterizedTest
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @MethodSource("provideInvalidUserFields")
        @Description("Should fail and return 400 when fields are invalid")
        void shouldFailAndReturn400(String name, String lastname, String email, String password) {
            Response response = registerUser(name, lastname, email, password);
            assertEquals(400, response.getStatusCode(), "Expected status code 400 for invalid input");
        }

        public static Stream<Arguments> provideInvalidUserFields() {
            String validLastName = "Jon";
            String validFirstName = "Snow";
            String validEmail = "jon.snow@example.com";
            String validPassword = "Password123!";
            return Stream.of(
                    Arguments.of("", validLastName, validEmail, validPassword),
                    Arguments.of(null, validLastName, validEmail, validPassword),

                    Arguments.of(validFirstName, "", validEmail, validPassword),
                    Arguments.of(validFirstName, null, validEmail, validPassword),

                    Arguments.of(validFirstName, validLastName, "", validPassword),
                    Arguments.of(validFirstName, validLastName, null, validPassword),
                    Arguments.of(validFirstName, validLastName, "invalidEmail", validPassword),

                    Arguments.of(validFirstName, validLastName, validEmail, ""),
                    Arguments.of(validFirstName, validLastName, validEmail, null)
            );
        }
    }

    @Nested
    class Authenticate {
        private final JwtService jwtService = new JwtService();

        private Response authenticateUser(String email, String password) {
            AuthRequest authRequestBody = new AuthRequest(email, password);
            var request = given().contentType("application/json").port(port).body(authRequestBody);
            return request.when().post("/api/v1/authenticate");
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should authenticate a user and return a token")
        void shouldAuthenticateUserAndReturnAToken() {
            UserEntity user = registerUser("validPassword123!");
            Response response = authenticateUser(user.getEmail(), "validPassword123!");

            assertEquals(200, response.getStatusCode());
            assertNotNull(response.getBody().jsonPath().get("token"));
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        void shouldFailAuthenticationWithWrongPassword() {
            UserEntity user = registerUser("validPassword123!");
            Response response = authenticateUser(user.getEmail(), "wrongPassword");
            assertEquals(401, response.getStatusCode());
        }
    }
}
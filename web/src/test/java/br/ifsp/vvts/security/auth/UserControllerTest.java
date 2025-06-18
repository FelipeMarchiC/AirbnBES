package br.ifsp.vvts.security.auth;

import br.ifsp.vvts.rental.requests.PostRequest;
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
    }
}
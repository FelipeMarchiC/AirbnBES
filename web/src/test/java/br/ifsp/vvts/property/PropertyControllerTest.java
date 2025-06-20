package br.ifsp.vvts.property;

import br.ifsp.application.property.repository.JpaPropertyRepository;
import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import br.ifsp.vvts.utils.BaseApiIntegrationTest;
import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

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

    @BeforeAll
    void setUp() {
        userPassword = faker.internet().password();
        adminPassword = faker.internet().password();
        user = registerUser(userPassword);
        admin = registerAdminUser(adminPassword);
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Should list all properties to user")
    void listAllPropertiesForRegularUser() {
        PropertyEntity property = PropertyEntity.builder()
                .id(UUID.randomUUID())
                .name("Casa na praia em riviera")
                .owner(user)
                .dailyRate(new Price(BigDecimal.valueOf(590)))
                .description("Bela casa")
                .address(Address.builder()
                        .state("São Paulo")
                        .city("Riviera")
                        .postalCode("Ai ta querendo dms")
                        .number("69")
                        .street("Rua Osvaldo")
                        .build()
                ).build();
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Should list all properties to admin")
    void listAllPropertiesForAdmin() {
        String token = authenticate(admin.getEmail(), adminPassword);

        PropertyEntity property = PropertyEntity.builder()
                .id(UUID.randomUUID())
                .name("Casa na praia em riviera")
                .owner(admin)
                .dailyRate(new Price(BigDecimal.valueOf(590)))
                .description("Bela casa")
                .address(Address.builder()
                        .state("São Paulo")
                        .city("Riviera")
                        .postalCode("Ai ta querendo dms")
                        .number("69")
                        .street("Rua Osvaldo")
                        .build()
                ).build();

        var response = given()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Authorization", "Bearer " + token)
                .when().get("/api/v1/property")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response();

        System.out.println(response);
    }
}

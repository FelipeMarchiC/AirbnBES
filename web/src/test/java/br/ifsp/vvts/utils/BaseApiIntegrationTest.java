package br.ifsp.vvts.utils;

import br.ifsp.application.property.repository.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.vvts.security.auth.AuthRequest;
import br.ifsp.vvts.security.auth.AuthResponse;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.baseURI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseApiIntegrationTest {
    @LocalServerPort
    protected int port = 8080;
    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private JpaPropertyRepository propertyRepository;

    @Autowired JpaRentalRepository rentalRepository;

    @BeforeEach
    public void generalSetup() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        propertyRepository.deleteAll();
        rentalRepository.deleteAll();
    }

    @BeforeAll
    void setUp() {
        rentalRepository.deleteAll();
        propertyRepository.deleteAll();
        rentalRepository.deleteAll();
    }

    protected UserEntity registerUser(String password) {
        final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserEntity user = EntityBuilder.createRandomUser(encoder.encode(password));
        userRepository.save(user);
        return user;
    }

    protected UserEntity registerAdminUser(String password) {
        final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserEntity user = EntityBuilder.createRandomAdmin(encoder.encode(password));
        userRepository.save(user);
        return user;
    }

    protected String authenticate(String username, String password) {
        RestTemplate restTemplate = new RestTemplate();
        AuthRequest authRequest = new AuthRequest(username, password);
        final String url = baseURI + "/api/v1/authenticate";
        final AuthResponse response = restTemplate.postForObject(url, authRequest, AuthResponse.class);
        Assertions.assertNotNull(response);
        return response.token();
    }

    protected PropertyEntity createRandomProperty(UserEntity user) {
        PropertyEntity property = EntityBuilder.createRandomProperty(user);
        propertyRepository.save(property);
        return property;
    }

    protected RentalEntity createRentalEntity(UserEntity user, PropertyEntity property) {
        RentalEntity rentalEntity = EntityBuilder.createRentalEntity(user, property);
        rentalRepository.save(rentalEntity);
        return rentalEntity;
    }
}

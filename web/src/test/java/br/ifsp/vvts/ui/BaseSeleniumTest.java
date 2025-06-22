package br.ifsp.vvts.ui;

import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.UserEntity;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseSeleniumTest {
    @Autowired
    protected JpaUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    protected WebDriver driver;

    protected String baseUrl = "http://localhost:5173";

    protected String usersPassword;

    protected UserEntity admin;

    protected UserEntity user;

    protected final Faker faker = Faker.instance();


    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        setInitialPage();
        usersPassword = faker.internet().password();

        this.admin = createUser(Role.ADMIN, usersPassword);
        this.user = createUser(Role.USER, usersPassword);

        userRepository.save(admin);
        userRepository.save(user);
    }

    private UserEntity createUser(Role role, String password) {
        return UserEntity.builder()
                .id(UUID.randomUUID())
                .name("Gustavo")
                .lastname("Contiero")
                .email(faker.internet().emailAddress())
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();
    }

    @AfterEach
    public void tearDown() {
        driver.close();
    }

    protected void setInitialPage() {
    }
}

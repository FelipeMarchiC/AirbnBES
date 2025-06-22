package br.ifsp.vvts.ui;

import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.UserEntity;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class BaseSeleniumTest {
    @Autowired
    protected JpaUserRepository userRepository;

    protected WebDriver driver;

    protected String baseUrl = "http://localhost:5173";

    protected UserEntity admin;

    protected UserEntity user;

    protected final Faker faker = Faker.instance();

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        setInitialPage();

        this.admin = createUser(Role.ADMIN);
        this.user = createUser(Role.USER);

        userRepository.save(admin);
        userRepository.save(user);
    }

    private UserEntity createUser(Role role) {
        return UserEntity.builder()
                .id(UUID.randomUUID())
                .name(faker.rickAndMorty().character())
                .lastname(faker.rickAndMorty().character())
                .email(faker.internet().emailAddress())
                .password("validPassword123!")
                .role(role)
                .build();
    }

    @AfterEach
    public void tearDown() {
        driver.close();
    }

    protected void setInitialPage(){}
}

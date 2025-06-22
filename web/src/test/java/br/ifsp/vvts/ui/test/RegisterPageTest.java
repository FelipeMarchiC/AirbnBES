package br.ifsp.vvts.ui.test;

import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.vvts.ui.BaseSeleniumTest;
import br.ifsp.vvts.ui.page.RegisterPageObject;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegisterPageTest extends BaseSeleniumTest {
    private RegisterPageObject registerPageObject;
    private final Faker faker = Faker.instance();

    @Autowired
    JpaUserRepository userRepository;

    @Override
    protected void setInitialPage() {
        driver.get(baseUrl + "/cadastro");
    }

    @BeforeEach
    void setUpPage() {
        registerPageObject = new RegisterPageObject(driver);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Should register user with valid input data")
    void shouldRegisterUserWithValidInputData() {
        String password = "validPassword123!";
        String email = faker.internet().emailAddress();

        registerPageObject.registerUser(
                faker.name().fullName(),
                email,
                password,
                password
        );

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.urlContains("/login"));

        assertThat(driver.getCurrentUrl()).contains("/login");
        assertTrue(userRepository.findByEmail(email).isPresent());

        List<String> toasts = registerPageObject.getToastMessages();
        assertThat(toasts).anyMatch(msg -> msg.contains("Cadastro realizado com sucesso"));
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Should not register user with already registered email")
    void shouldNotRegisterUserWithExistingEmail() {
        String email = faker.internet().emailAddress();
        String password = "validPassword123!";

        registerPageObject.registerUser(
                faker.name().fullName(),
                email,
                password,
                password
        );

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.urlContains("/login"));

        driver.get(baseUrl + "/cadastro");

        registerPageObject.registerUser(
                faker.name().fullName(),
                email,
                password,
                password
        );

        List<String> toasts = registerPageObject.getToastMessages();
        assertThat(toasts).anyMatch(msg -> msg.toLowerCase()
                .contains("este e-mail já está cadastrado. por favor, faça login ou use outro e-mail."));
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Should navigate to login page when click in login link")
    void shouldNavigateToLoginPage() {
        registerPageObject.clickLoginLink();

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.urlContains("/login"));

        assertThat(driver.getCurrentUrl()).contains("/login");
    }
    @Test
    @Tag("UiTest")
    @DisplayName("Should not register user if name is too long")
    void shouldNotRegisterUserIfNameIsTooLong() {
        String longName = "A".repeat(500 + 1);
        String email = faker.internet().emailAddress();
        String password = "validPassword123!";

        registerPageObject.registerUser(
                longName,
                email,
                password,
                password
        );

        new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.urlContains("/cadastro"));
        assertThat(driver.getCurrentUrl()).contains("/cadastro");

        assertFalse(userRepository.findByEmail(email).isPresent());

        List<String> toasts = registerPageObject.getToastMessages();
        assertThat(toasts).anyMatch(msg -> msg.toLowerCase().contains("nome muito longo") || msg.toLowerCase().contains("limite de caracteres excedido"));
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Should not register user if email is too long")
    void shouldNotRegisterUserIfEmailIsTooLong() {
        String oversizedEmail = "longEmail".repeat(500) + "@" + "example.com";
        String password = "validPassword123!";
        String name = faker.name().fullName();

        registerPageObject.registerUser(
                name,
                oversizedEmail,
                password,
                password
        );

        new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.urlContains("/cadastro"));
        assertThat(driver.getCurrentUrl()).contains("/cadastro");

        assertFalse(userRepository.findByEmail(oversizedEmail).isPresent());

        List<String> toasts = registerPageObject.getToastMessages();
        assertThat(toasts).anyMatch(msg -> msg.toLowerCase().contains("e-mail muito longo") || msg.toLowerCase().contains("limite de caracteres excedido"));
    }
}

package br.ifsp.vvts.ui.test;

import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.vvts.ui.BaseSeleniumTest;
import br.ifsp.vvts.ui.page.LoginPageObject;
import br.ifsp.vvts.utils.EntityBuilder;
import com.github.javafaker.Faker;
import jdk.jfr.Description;
import org.junit.jupiter.api.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginPageTest extends BaseSeleniumTest {
    private LoginPageObject loginPageObject;
    private final Faker faker = Faker.instance();

    @Autowired
    JpaUserRepository userRepository;

    @Override
    protected void setInitialPage() {
        driver.get(baseUrl + "/login");
    }

    @BeforeEach
    void setUpPage() {
        loginPageObject = new LoginPageObject(driver);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    @Tag("UiTest")
    void shouldLoginUserWithValidCredentials() {
        loginPageObject.loginUser(user.getEmail(), usersPassword);

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/"));

        assertThat(driver.getCurrentUrl()).contains("/");
        List<String> toasts = loginPageObject.getToastMessages();
        assertThat(toasts).anyMatch(msg -> msg.toLowerCase().contains("login realizado com sucesso!"));
    }

    @Test
    @Tag("UiTest")
    @Description("Should not login user with unregistered email")
    void shouldNotLoginUserWithUnregisteredEmail() {
        String unregisteredEmail = faker.internet().emailAddress();
        String password = "anyPassword123!";

        loginPageObject.loginUser(unregisteredEmail, password);

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/login"));

        assertThat(driver.getCurrentUrl()).contains("/login");
        List<String> toasts = loginPageObject.getToastMessages();
        assertThat(toasts).anyMatch(msg -> msg.toLowerCase().contains("credenciais inválidas. tente novamente."));
    }

    @Test
    @Tag("UiTest")
    @Description("Should navigate to register page")
    void shouldNavigateToRegisterPage() {
        loginPageObject.clickRegisterLink();

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/cadastro"));

        assertThat(driver.getCurrentUrl()).contains("/cadastro");
    }
}

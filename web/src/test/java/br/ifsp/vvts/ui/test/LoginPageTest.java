package br.ifsp.vvts.ui.test;

import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.vvts.ui.BaseSeleniumTest;
import br.ifsp.vvts.ui.page.LoginPageObject;
import br.ifsp.vvts.utils.EntityBuilder;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.pitest.reloc.antlr.common.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

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
        String password = "validPassword123!";
        UserEntity user = EntityBuilder.createRandomUser(password);
        String email = user.getEmail();

        userRepository.save(user);

        loginPageObject.loginUser(email, password);

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/"));

        assertThat(driver.getCurrentUrl()).contains("/");
    }
}

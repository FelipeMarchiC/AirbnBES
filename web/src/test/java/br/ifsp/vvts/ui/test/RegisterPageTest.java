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

}

package br.ifsp.vvts.ui.test;

import br.ifsp.application.user.repository.JpaUserRepository;
import br.ifsp.vvts.ui.BaseSeleniumTest;
import br.ifsp.vvts.ui.page.LoginPageObject;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}

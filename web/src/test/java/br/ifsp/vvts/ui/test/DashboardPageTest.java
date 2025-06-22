package br.ifsp.vvts.ui.test;

import br.ifsp.vvts.ui.BaseSeleniumTest;
import br.ifsp.vvts.ui.page.LoginPageObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DashboardPageTest extends BaseSeleniumTest {

    @Override
    protected void setInitialPage() {
        driver.get(baseUrl + "/login");
    }

    @BeforeEach
    void setUpPage() {
        var loginPageObject = new LoginPageObject(driver);
        loginPageObject.loginUser(admin.getEmail(), admin.getPassword());
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

}

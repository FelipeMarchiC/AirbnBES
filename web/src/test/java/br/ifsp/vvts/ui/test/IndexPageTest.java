package br.ifsp.vvts.ui.test;

import br.ifsp.vvts.ui.BaseSeleniumTest;
import br.ifsp.vvts.ui.page.IndexPageObject;
import br.ifsp.vvts.ui.page.LoginPageObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

public class IndexPageTest extends BaseSeleniumTest {
    @Override
    protected void setInitialPage() {
        driver.get(baseUrl + "/login");
    }

    @BeforeEach
    void setUpPage() {
        LoginPageObject loginPageObject = new LoginPageObject(driver);
        loginPageObject.loginUser(user.getEmail(), user.getPassword());
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/"));
    }


}

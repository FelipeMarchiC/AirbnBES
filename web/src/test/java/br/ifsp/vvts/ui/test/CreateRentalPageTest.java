package br.ifsp.vvts.ui.test;

import br.ifsp.vvts.ui.BaseSeleniumTest;
import br.ifsp.vvts.ui.page.CreateRentalPageObject;
import br.ifsp.vvts.ui.page.IndexPageObject;
import br.ifsp.vvts.ui.page.LoginPageObject;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;


public class CreateRentalPageTest extends BaseSeleniumTest {
    CreateRentalPageObject createRentalPageObject;

    @Override
    protected void setInitialPage() {
        driver.get(baseUrl + "/login");
    }

    @BeforeEach
    void setUpPage() {
        createRentalPageObject = new CreateRentalPageObject(driver);
        LoginPageObject loginPageObject = new LoginPageObject(driver);
        IndexPageObject indexPageObject = new IndexPageObject(driver);

        loginPageObject.loginUser(user.getEmail(), usersPassword);

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(indexPageObject.getPropertyCardAnchor()));

        indexPageObject.getFirstAnchorInFirstCard().click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/propriedades"));
    }

    @Test
    @Tag("UiTest")
    @DisplayName("the location of the property should not be null")
    void shouldNotBeNullTheLocationOfTheProperty() {
        Assertions.assertFalse(createRentalPageObject.getLocation().isEmpty());
    }


}

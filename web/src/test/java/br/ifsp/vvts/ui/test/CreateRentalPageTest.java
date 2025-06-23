package br.ifsp.vvts.ui.test;

import br.ifsp.vvts.ui.BaseSeleniumTest;
import br.ifsp.vvts.ui.page.CreateRentalPageObject;
import br.ifsp.vvts.ui.page.IndexPageObject;
import br.ifsp.vvts.ui.page.LoginPageObject;
import br.ifsp.vvts.ui.page.MyRentalsPageObject;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


public class CreateRentalPageTest extends BaseSeleniumTest {
    CreateRentalPageObject createRentalPageObject;
    LoginPageObject loginPageObject;
    IndexPageObject indexPageObject;
    MyRentalsPageObject myRentalsPageObject;

    @Override
    protected void setInitialPage() {
        driver.get(baseUrl + "/login");
    }

    @BeforeEach
    void setUpPage() {
        createRentalPageObject = new CreateRentalPageObject(driver);
        loginPageObject = new LoginPageObject(driver);
        indexPageObject = new IndexPageObject(driver);
        myRentalsPageObject = new MyRentalsPageObject(driver);

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

    @Test
    @Tag("UiTest")
    @DisplayName("Should match the draft rental price with the real rental price")
    void shouldMatchTheDraftRentalPriceWithTheRealRentalPrice() {
        String totalPrice = createRentalPageObject
                .fillStartDate(LocalDate.now().plusDays(1))
                .fillEndDate(LocalDate.now().plusDays(3))
                .getTotalPrice();

        String rawValue = totalPrice.replace("Total: R$ ", "").trim();
        rawValue = rawValue.replace(".", "");
        BigDecimal value = new BigDecimal(rawValue.replace(",", "."));

        createRentalPageObject.clickSubmitButton();

        String realRentalPriceString = myRentalsPageObject.getTextFromRental();
        realRentalPriceString = realRentalPriceString.replace("R$ ", "").trim();
        realRentalPriceString = realRentalPriceString.replace("Valor total: ", "").trim();
        realRentalPriceString = realRentalPriceString.replace(".", "");
        BigDecimal realRentalPrice = new BigDecimal(realRentalPriceString.replace(",", "."));

        assertThat(realRentalPrice).isEqualTo(value);
    }
}

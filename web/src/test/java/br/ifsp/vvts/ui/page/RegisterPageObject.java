package br.ifsp.vvts.ui.page;

import br.ifsp.vvts.ui.BasePageObject;
import com.github.javafaker.Faker;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class RegisterPageObject extends BasePageObject {
    private static final Faker faker = Faker.instance();
    private final WebDriverWait wait;

    private final By fullNameInput = By.id("name");
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By confirmPasswordInput = By.id("confirmPassword");
    private final By registerButton = By.xpath("//*[@id=\"root\"]/div[1]/main/div/div/form/div[2]/button");

    private final By toaster = By.xpath("//*[@id='_rht_toaster']/*");

    public RegisterPageObject(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public RegisterPageObject fillFullName(String fullName) {
        driver.findElement(fullNameInput).sendKeys(fullName);
        return this;
    }

    public RegisterPageObject fillEmail(String email) {
        driver.findElement(emailInput).sendKeys(email);
        return this;
    }

    public RegisterPageObject fillPassword(String password) {
        driver.findElement(passwordInput).sendKeys(password);
        return this;
    }

    public RegisterPageObject fillConfirmPassword(String confirmPassword) {
        driver.findElement(confirmPasswordInput).sendKeys(confirmPassword);
        return this;
    }

    public RegisterPageObject clickRegisterButton() {
        driver.findElement(registerButton).click();
        return this;
    }

    public RegisterPageObject registerUser(String fullName, String email, String password, String confirmPassword) {
        return fillFullName(fullName)
                .fillEmail(email)
                .fillPassword(password)
                .fillConfirmPassword(confirmPassword)
                .clickRegisterButton();
    }

    public List<String> getToastMessages() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(toaster));
        List<WebElement> elements = driver.findElements(toaster);
        return elements.stream()
                .map(WebElement::getText)
                .filter(text -> !text.isBlank())
                .toList();
    }
}
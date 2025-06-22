package br.ifsp.vvts.ui.page;

import br.ifsp.vvts.ui.BasePageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.pitest.util.Log;

public class LoginPageObject extends BasePageObject {
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.cssSelector("form button[type='submit']");
    private final By registerLink = By.linkText("Cadastre-se");
    public LoginPageObject(WebDriver driver) {
        super(driver);
    }

    public LoginPageObject loginUser(String email, String password) {
        return fillEmail(email)
                .fillPassword(password)
                .clickLoginButton();
    }

    private LoginPageObject fillEmail(String email) {
        driver.findElement(emailInput).sendKeys(email);
        return this;
    }

    private LoginPageObject fillPassword(String password) {
        driver.findElement(passwordInput).sendKeys(password);
        return this;
    }

    private LoginPageObject clickLoginButton() {
        driver.findElement(loginButton).click();
        return this;
    }

    public LoginPageObject clickRegisterLink() {
        driver.findElement(registerLink).click();
        return this;
    }
}

package br.ifsp.vvts.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pitest.fasterxml.jackson.databind.ser.Serializers;

import java.time.Duration;
import java.util.List;

public class BasePageObject {
    protected final WebDriver driver;

    protected final By toaster = By.xpath("//*[@id='_rht_toaster']/*");

    protected final WebDriverWait wait;

    public List<String> getToastMessages() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(toaster));
        List<WebElement> elements = driver.findElements(toaster);
        return elements.stream()
                .map(WebElement::getText)
                .filter(text -> !text.isBlank())
                .toList();
    }
    public BasePageObject(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
}

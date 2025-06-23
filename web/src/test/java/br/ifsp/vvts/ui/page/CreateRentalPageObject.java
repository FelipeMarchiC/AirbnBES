package br.ifsp.vvts.ui.page;

import br.ifsp.vvts.ui.BasePageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CreateRentalPageObject extends BasePageObject {
    private final By locationSpan = By.cssSelector("div.mb-6 div.flex > span");
    private final By startDateInput = By.id("startDate");
    private final By endDateInput = By.id("endDate");
    private final By submitButton = By.cssSelector("form button[type='submit']");

    public CreateRentalPageObject(WebDriver driver) {
        super(driver);
    }

    public String getLocation() {
        wait.until(ExpectedConditions.presenceOfElementLocated(locationSpan));
        return driver.findElement(locationSpan).getText();
    }

    public CreateRentalPageObject fillStartDate(LocalDate startDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        String formattedDate = startDate.format(formatter);
        driver.findElement(startDateInput).sendKeys(formattedDate);
        return this;
    }

    public CreateRentalPageObject fillEndDate(LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        String formattedDate = endDate.format(formatter);
        driver.findElement(endDateInput).sendKeys(formattedDate);
        return this;
    }

    public CreateRentalPageObject clickCreateButton() {
        driver.findElement(submitButton).click();
        return this;
    }

}

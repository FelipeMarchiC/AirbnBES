package br.ifsp.vvts.ui.page;

import br.ifsp.vvts.ui.BasePageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CreateRentalPageObject extends BasePageObject {
    private final By locationSpan = By.cssSelector("div.mb-6 div.flex > span");
    private final By startDateInput = By.id("startDate");
    private final By endDateInput = By.id("endDate");
    private final By submitButton = By.xpath("/html/body/div/div[1]/main/div/div[2]/div[4]/form/button");
    private final By totalPriceText = By.xpath("/html/body/div/div[1]/main/div/div[2]/div[4]/form/div[3]");

    public CreateRentalPageObject(WebDriver driver) {
        super(driver);
    }

    public String getLocation() {
        wait.until(ExpectedConditions.presenceOfElementLocated(locationSpan));
        return driver.findElement(locationSpan).getText();
    }

    public String getTotalPrice() {
        wait.until(ExpectedConditions.presenceOfElementLocated(totalPriceText));
        return driver.findElement(totalPriceText).getText();
    }

    public CreateRentalPageObject fillStartDate(LocalDate startDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = startDate.format(formatter);
        formattedDate = formattedDate.replace("-", "");
        wait.until(ExpectedConditions.visibilityOfElementLocated(startDateInput));
        driver.findElement(startDateInput).sendKeys(formattedDate);
        return this;
    }

    public CreateRentalPageObject fillEndDate(LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = endDate.format(formatter);
        formattedDate = formattedDate.replace("-", "");
        wait.until(ExpectedConditions.visibilityOfElementLocated(endDateInput));
        driver.findElement(endDateInput).sendKeys(formattedDate);
        return this;
    }

    public CreateRentalPageObject clickSubmitButton() {
        driver.findElement(submitButton).click();
        return this;
    }


}

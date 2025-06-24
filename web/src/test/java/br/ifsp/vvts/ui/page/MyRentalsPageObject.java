package br.ifsp.vvts.ui.page;

import br.ifsp.vvts.ui.BasePageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class MyRentalsPageObject extends BasePageObject {
    private final By rentalPrice = By.xpath("/html/body/div/div[1]/main/div/div[2]/div/div[1]/div/div/div/div[1]/p[3]");

    public MyRentalsPageObject(WebDriver driver) {
        super(driver);
    }

    public String getTextFromRental() {
        wait.until(ExpectedConditions.presenceOfElementLocated(rentalPrice));
        return driver.findElement(rentalPrice).getText();
    }
}

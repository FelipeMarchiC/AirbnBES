package br.ifsp.vvts.ui.page;

import br.ifsp.vvts.ui.BasePageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class DashboardPageObject extends BasePageObject {
    private final By manageRentalsButton = By.xpath("//*[@id=\"root\"]/div[1]/aside/nav/a[2]");
    private final By dashboardButton = By.xpath("//*[@id=\"root\"]/div[1]/aside/nav/a[1]");
    private final By linkToMainPage = By.xpath("//*[@id=\"root\"]/div[1]/aside/nav/a[3]");
    private final By statusFilter = By.id("statusFilter");
    private final By rentalContainer = By.className("//*[@id=\"root\"]/div[1]/div/main/div[2]/div/div[2]");

    public DashboardPageObject(WebDriver driver) {
        super(driver);
    }

    public void clickDashboardButton() {
        wait.until(ExpectedConditions.elementToBeClickable(dashboardButton)).click();
    }

    public void clickManageRentalsButton() {
        wait.until(ExpectedConditions.elementToBeClickable(manageRentalsButton)).click();
    }

    public void clickLinkToMainPage() {
        wait.until(ExpectedConditions.elementToBeClickable(linkToMainPage)).click();
    }

    public void selectStatusOption(String visibleText) {
        var element = wait.until(ExpectedConditions.visibilityOfElementLocated(statusFilter));
        Select select = new Select(element);
        select.selectByVisibleText(visibleText);
    }
}
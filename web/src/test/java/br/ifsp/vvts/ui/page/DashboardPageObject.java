package br.ifsp.vvts.ui.page;

import br.ifsp.vvts.ui.BasePageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPageObject extends BasePageObject {
    private final By manageRentalsButton = By.xpath("//*[@id=\"root\"]/div[1]/aside/nav/a[2]");
    public DashboardPageObject(WebDriver driver) {
        super(driver);
    }
}

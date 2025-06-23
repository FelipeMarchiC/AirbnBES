package br.ifsp.vvts.ui.page;

import br.ifsp.vvts.ui.BasePageObject;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Getter
public class IndexPageObject extends BasePageObject {
    private final By propertiesGrid = By.cssSelector("div.grid.grid-cols-1");
    private final By propertyCardAnchor = By.cssSelector(".card a");

    public IndexPageObject(WebDriver driver) {
        super(driver);
    }

    public WebElement getFirstPropertyCard() {
        WebElement grid = driver.findElement(propertiesGrid);
        return grid.findElement(By.cssSelector("div.card"));
    }

    public WebElement getFirstAnchorInFirstCard() {
        WebElement firstCard = getFirstPropertyCard();
        return firstCard.findElement(By.tagName("a"));
    }

}


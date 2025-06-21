package br.ifsp.ui;

import org.openqa.selenium.WebDriver;
import pitest.fasterxml.jackson.databind.ser.Serializers;

public class BasePageObject {
    protected final WebDriver driver;

    public BasePageObject(WebDriver driver) {
        this.driver = driver;
    }
}

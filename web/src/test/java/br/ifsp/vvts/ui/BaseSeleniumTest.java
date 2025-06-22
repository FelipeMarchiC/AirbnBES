package br.ifsp.vvts.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class BaseSeleniumTest {
    protected WebDriver driver;

    protected String baseUrl = "http://localhost:5173";

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        setInitialPage();
    }

    @AfterEach
    public void tearDown() {
        driver.close();
    }

    protected void setInitialPage(){}
}

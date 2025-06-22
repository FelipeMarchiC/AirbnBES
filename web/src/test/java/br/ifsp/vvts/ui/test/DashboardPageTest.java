package br.ifsp.vvts.ui.test;

import br.ifsp.vvts.ui.BaseSeleniumTest;
import br.ifsp.vvts.ui.page.DashboardPageObject;
import br.ifsp.vvts.ui.page.LoginPageObject;
import org.junit.jupiter.api.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DashboardPageTest extends BaseSeleniumTest {
    private DashboardPageObject dashboardPageObject;

    @Override
    protected void setInitialPage() {
        driver.get(baseUrl + "/login");
    }

    @BeforeEach
    void setUpPage() {
        var loginPageObject = new LoginPageObject(driver);
        loginPageObject.loginUser(admin.getEmail(), usersPassword);

        this.dashboardPageObject = new DashboardPageObject(driver);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Nested
    class ManageRentals {
        @Test
        @Tag("UiTest")
        @DisplayName("Should render manage rentals page")
        void shouldRenderManageRentalsPage() {
            dashboardPageObject.clickManageRentalsButton();
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.urlContains("/admin/alugueis"));
            assertThat(driver.getCurrentUrl()).contains("/admin/alugueis");
        }
    }
}

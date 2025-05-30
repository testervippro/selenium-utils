// your package
import lombok.AllArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;


public class WebDriverWaitUtil {

    private  WebDriver driver;
    private  Duration defaultWaitTime ;

    public WebDriverWaitUtil(WebDriver driver,long timeOut ) {
        this.driver = driver;
        this.defaultWaitTime = Duration.ofSeconds(timeOut);
    }

    public void waitForElementPresent(By locator) {
        webDriverWait()
                .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public void waitForElementToBeDisplayed(WebElement element) {
        webDriverWait()
                .until(ExpectedConditions.visibilityOf(element));
    }

    public void waitForElementClickable(WebElement element) {
        webDriverWait()
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    private WebDriverWait webDriverWait() {
        return new WebDriverWait(driver, defaultWaitTime);
    }
}



import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.net.MalformedURLException;
import java.net.URL;

public class SeleniumGridTest {
  private WebDriver driver;
  private static final String GRID_URL = "http://localhost:4444/wd/hub";

  @BeforeClass
  public void setUp() throws MalformedURLException {
    // Set Chrome options
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--start-maximized");

    // Initialize RemoteWebDriver
    driver = new RemoteWebDriver(new URL(GRID_URL), options);
  }

  @Test
  public void testGoogleTitle() {
    // Open Google
    driver.get("https://www.google.com");

    // Verify the title
    String title = driver.getTitle();
    System.out.println("Page Title: " + title);
    Assert.assertTrue(title.contains("Google"), "Title does not contain 'Google'");
  }

  @AfterClass
  public void tearDown() {
    if (driver != null) {
      driver.quit();
    }
  }
}

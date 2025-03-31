
```markdown
# Install from Maven

```xml
<dependency>
    <groupId>io.github.testervippro</groupId>
    <artifactId>selenium-utils</artifactId>
    <version>1.3</version>
</dependency>
```

# Library Features

This library comes pre-installed with some dependencies. You can use it by default or override them by re-writing in your project.

---

# Example: Using the Library to Record Video

## Using MONTE for Video Recording

Copy all class `RecorderManager.java` to your project.

```java
@BeforeClass
public void start() throws Exception {
    RecorderManager.startVideoRecording(RecorderManager.RECORDTYPE.MONTE, "Video01");
}

@AfterClass
public void stop() throws Exception {
    // If set to true, video converts from AVI to MP4 and deletes the AVI file
    RecorderManager.stopVideoRecording(RecorderManager.RECORDTYPE.MONTE, true);
}
```

---

## Using FFMPEG for Video Recording

```java
@BeforeClass
public void start() throws Exception {
    RecorderManager.startVideoRecording(RecorderManager.RECORDTYPE.FFMPEG, "Video01");
}

@AfterClass
public void stop() throws Exception {
    // When recording with FFMPEG, setting true or false has no impact as it always records in MP4 format
    RecorderManager.stopVideoRecording(RecorderManager.RECORDTYPE.FFMPEG, true);
}
```

---

# Attaching Video to Allure Report

This implementation is based on [this reference](https://github.com/biczomate/allure-testng7.5-attachment-example).

```java
@AfterClass
public void stop() throws Exception {
    RecorderManager.stopVideoRecording(RecorderManager.RECORDTYPE.MONTE, true);
    File videoPath = new File("videos", RecorderManager.nameVideo);
    log.info(videoPath);

    if (videoPath.exists() && videoPath.isFile()) { // Check if file exists and is a file
        Allure.addAttachment("Video", "video/mp4",
            Files.asByteSource(videoPath).openStream(), "mp4");
    } else {
        log.info("Video file does not exist: " + videoPath.getAbsolutePath());
    }
}
```

---

# Running Tests with Allure

```sh
mvn clean test
mvn allure:serve
```

Example:

![image](https://github.com/user-attachments/assets/0f23b25a-e98e-42d6-93c2-77f7b52ec11e)

---

# Install Allure Maven Plugin

Ensure your project includes the Allure Maven plugin:

```xml
<plugin>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-maven</artifactId>
    <version>2.12.0</version>
</plugin>
```

---

# Selenium Grid Standalone Record Video with Podman (No Docker)

```java
public class SeleniumGridTest {
  private WebDriver driver;
  private static final String GRID_URL = "http://localhost:4444/wd/hub";

  @BeforeClass
  public void setUp() throws Exception {
    // Place before call driver
    PodmanManager.start();
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
  public void tearDown() throws Exception {

    if (driver != null) {
      driver.quit();
    }
    // Place after
    PodmanManager.stop();

  }
}
```

Run other commands with [reference](https://github.com/SeleniumHQ/docker-selenium?tab=readme-ov-file#video-recording)

---

# General Properties

```xml
<properties>
    <allure.version>2.24.0</allure.version>
    <maven-compiler-plugin.version>3.13.0</maven-compiler-plugin.version>
    <allure-maven.version>2.11.2</allure-maven.version>
    <allure-environment-writer.version>1.0.0</allure-environment-writer.version>
    <aspectj.version>1.9.20.1</aspectj.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <java-compiler.version>17</java-compiler.version>
    <selenium.version>4.28.0</selenium.version>
    <testng.version>7.10.2</testng.version>
    <assertj.version>3.26.3</assertj.version>
    <datafaker.version>2.4.2</datafaker.version>
    <log4j.version>2.23.1</log4j.version>
    <owner.version>1.0.12</owner.version>
    <opencsv.version>5.7.1</opencsv.version>
    <jackson-databind.version>2.15.0</jackson-databind.version>
    <jackson-core.version>2.15.0</jackson-core.version>
    <poi.version>3.13</poi.version>
    <poi-ooxml.version>3.13</poi-ooxml.version>
    <commons-io.version>2.18.0</commons-io.version>
    <record-video.version>2.1</record-video.version>
    <jsonpath.version>2.8.0</jsonpath.version>
    <restassured.version>5.5.0</restassured.version>
    <webdrivermanager.version>5.9.2</webdrivermanager.version>
    <selenium-edge-driver.version>4.28.1</selenium-edge-driver.version>
    <commons-compress.version>1.27.1</commons-compress.version>
    <lombok.version>1.18.36</lombok.version>
    <junit.version>5.11.0-M2</junit.version>
</properties>
```

---

# After Installing `pom.xml`, Your Project Becomes Shorter

### Final `pom.xml` Structure

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.thoaikx</groupId>
    <artifactId>selenium-java-automation</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <maven.test.skip>true</maven.test.skip>
        <maven-surefire-plugin.version>3.5.2</maven-surefire-plugin.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <suite>local</suite>
    </properties>

    <dependencies>
        <dependency>
            <groupId>io.github.testervippro</groupId>
            <artifactId>selenium-utils</artifactId>
            <version>0.9</version>
        </dependency>
    </dependencies>
</project>
```

# Install from Maven
```xml
<dependency>
    <groupId>io.github.testervippro</groupId>
    <artifactId>selenium-utils</artifactId>
    <version>0.9</version>
</dependency>
```

# Library Features
This library comes pre-installed with some dependencies. You can use it by default or override them by re-writing in your project.

# Example: Using the Library to Record Video

## Using MONTE for Video Recording
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

# Attaching Video to Allure Report

This implementation is based on [this reference](https://github.com/biczomate/allure-testng7.5-attachment-example).

```java
import io.qameta.allure.Attachment;
import org.openqa.selenium.WebDriver;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AllureTestLifecycleListener implements TestLifecycleListener {

    public AllureTestLifecycleListener() {
        // Default constructor
    }

    @Attachment(value = "Test Video", type = "video/mp4")
    public byte[] saveVideo(WebDriver driver) {
        // Ensure nameVideo is not null
        if (RecorderManager.nameVideo == null || RecorderManager.nameVideo.isEmpty()) {
            System.err.println("Video name is not set.");
            return new byte[0];
        }

        File videoFile = new File("videos", RecorderManager.nameVideo);

        if (!videoFile.exists()) {
            System.err.println("Video file not found: " + videoFile.getAbsolutePath());
            return new byte[0];
        }
        try (FileInputStream fis = new FileInputStream(videoFile)) {
            byte[] videoBytes = new byte[(int) videoFile.length()];
            fis.read(videoBytes);
            return videoBytes;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public void beforeTestStop(TestResult result) {
        WebDriver driver = DriverManager.getDriver(); // Retrieve the driver instance
        saveVideo(driver);
    }
}
```

## General Properties
```xml
<properties>
    <maven.test.skip>true</maven.test.skip>
    <skipTests>true</skipTests>
    <allure.maven>2.12.0</allure.maven>
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


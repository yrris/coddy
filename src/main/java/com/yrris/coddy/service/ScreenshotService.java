package com.yrris.coddy.service;

import com.yrris.coddy.constant.AppConstant;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ScreenshotService {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotService.class);

    /**
     * Take a screenshot of the given URL using headless Chrome
     * and save it as PNG to the screenshot directory.
     *
     * @return the filename (e.g. "app_123.png")
     */
    public String takeScreenshot(String url, long appId) {
        Path screenshotDir = Paths.get(AppConstant.SCREENSHOT_ROOT_DIR);
        try {
            Files.createDirectories(screenshotDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create screenshot directory", e);
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1600,900");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        ChromeDriver driver = new ChromeDriver(options);
        try {
            driver.get(url);
            // Wait for page to render (JS execution, async loading)
            Thread.sleep(3000);

            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            String filename = "app_" + appId + ".png";
            Path outputPath = screenshotDir.resolve(filename);
            Files.write(outputPath, screenshotBytes);

            log.info("Screenshot saved: {}", outputPath);
            return filename;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Screenshot interrupted for appId=" + appId, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save screenshot for appId=" + appId, e);
        } finally {
            driver.quit();
        }
    }
}

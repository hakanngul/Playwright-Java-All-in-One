package com.starlettech.utils;

import com.microsoft.playwright.Page;
import com.starlettech.config.TestConfig;
import com.starlettech.core.PlaywrightManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for taking screenshots
 */
public class ScreenshotUtils {
    private static final Logger logger = LogManager.getLogger(ScreenshotUtils.class);
    private final TestConfig testConfig;
    private final String screenshotDir;

    public ScreenshotUtils() {
        this.testConfig = TestConfig.getInstance();
        this.screenshotDir = testConfig.getScreenshotPath();
        createScreenshotDirectory();
    }

    /**
     * Create screenshot directory if it doesn't exist
     */
    private void createScreenshotDirectory() {
        try {
            Path path = Paths.get(screenshotDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Created screenshot directory: {}", screenshotDir);
            }
        } catch (IOException e) {
            logger.error("Failed to create screenshot directory: {}", e.getMessage());
        }
    }

    /**
     * Take screenshot with timestamp
     */
    public String takeScreenshot() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        return takeScreenshot("screenshot_" + timestamp);
    }

    /**
     * Take screenshot with custom name
     */
    public String takeScreenshot(String name) {
        Page page = PlaywrightManager.getPage();
        if (page == null) {
            logger.warn("Page is null, cannot take screenshot");
            return null;
        }

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String fileName = name + "_" + timestamp + ".png";
            Path screenshotPath = Paths.get(screenshotDir, fileName);

            page.screenshot(new Page.ScreenshotOptions()
                .setPath(screenshotPath)
                .setFullPage(true));

            logger.info("Screenshot saved: {}", screenshotPath.toAbsolutePath());
            return screenshotPath.toString();
        } catch (Exception e) {
            logger.error("Failed to take screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Take screenshot of specific element
     */
    public String takeElementScreenshot(String selector, String name) {
        Page page = PlaywrightManager.getPage();
        if (page == null) {
            logger.warn("Page is null, cannot take element screenshot");
            return null;
        }

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String fileName = name + "_element_" + timestamp + ".png";
            Path screenshotPath = Paths.get(screenshotDir, fileName);

            page.locator(selector).screenshot(new com.microsoft.playwright.Locator.ScreenshotOptions()
                .setPath(screenshotPath));

            logger.info("Element screenshot saved: {}", screenshotPath.toAbsolutePath());
            return screenshotPath.toString();
        } catch (Exception e) {
            logger.error("Failed to take element screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Take screenshot with custom options
     */
    public String takeScreenshot(String name, boolean fullPage, int quality) {
        Page page = PlaywrightManager.getPage();
        if (page == null) {
            logger.warn("Page is null, cannot take screenshot");
            return null;
        }

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String fileName = name + "_" + timestamp + ".png";
            Path screenshotPath = Paths.get(screenshotDir, fileName);

            Page.ScreenshotOptions options = new Page.ScreenshotOptions()
                .setPath(screenshotPath)
                .setFullPage(fullPage);

            if (quality > 0 && quality <= 100) {
                options.setQuality(quality);
            }

            page.screenshot(options);

            logger.info("Screenshot saved with custom options: {}", screenshotPath.toAbsolutePath());
            return screenshotPath.toString();
        } catch (Exception e) {
            logger.error("Failed to take screenshot with custom options: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get screenshot directory path
     */
    public String getScreenshotDirectory() {
        return screenshotDir;
    }
}

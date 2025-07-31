package com.starlettech.utils;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.starlettech.config.TestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BooleanSupplier;

/**
 * Utility class for wait operations
 */
public class WaitUtils {
    private static final Logger logger = LogManager.getLogger(WaitUtils.class);
    private final Page page;
    private final TestConfig testConfig;

    public WaitUtils(Page page) {
        this.page = page;
        this.testConfig = TestConfig.getInstance();
    }

    /**
     * Wait for element to be visible
     */
    public void waitForVisible(String selector) {
        waitForVisible(selector, testConfig.getExplicitWait() * 1000);
    }

    /**
     * Wait for element to be visible with timeout
     */
    public void waitForVisible(String selector, int timeoutMs) {
        logger.debug("Waiting for element to be visible: {}", selector);
        page.waitForSelector(selector, new Page.WaitForSelectorOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(timeoutMs));
    }

    /**
     * Wait for element to be hidden
     */
    public void waitForHidden(String selector) {
        waitForHidden(selector, testConfig.getExplicitWait() * 1000);
    }

    /**
     * Wait for element to be hidden with timeout
     */
    public void waitForHidden(String selector, int timeoutMs) {
        logger.debug("Waiting for element to be hidden: {}", selector);
        page.waitForSelector(selector, new Page.WaitForSelectorOptions()
            .setState(WaitForSelectorState.HIDDEN)
            .setTimeout(timeoutMs));
    }

    /**
     * Wait for element to be attached to DOM
     */
    public void waitForAttached(String selector) {
        waitForAttached(selector, testConfig.getExplicitWait() * 1000);
    }

    /**
     * Wait for element to be attached with timeout
     */
    public void waitForAttached(String selector, int timeoutMs) {
        logger.debug("Waiting for element to be attached: {}", selector);
        page.waitForSelector(selector, new Page.WaitForSelectorOptions()
            .setState(WaitForSelectorState.ATTACHED)
            .setTimeout(timeoutMs));
    }

    /**
     * Wait for element to be detached from DOM
     */
    public void waitForDetached(String selector) {
        waitForDetached(selector, testConfig.getExplicitWait() * 1000);
    }

    /**
     * Wait for element to be detached with timeout
     */
    public void waitForDetached(String selector, int timeoutMs) {
        logger.debug("Waiting for element to be detached: {}", selector);
        page.waitForSelector(selector, new Page.WaitForSelectorOptions()
            .setState(WaitForSelectorState.DETACHED)
            .setTimeout(timeoutMs));
    }

    /**
     * Wait for page to load
     */
    public void waitForPageLoad() {
        logger.debug("Waiting for page to load");
        page.waitForLoadState();
    }

    /**
     * Wait for network to be idle
     */
    public void waitForNetworkIdle() {
        logger.debug("Waiting for network to be idle");
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    /**
     * Wait for DOM content to be loaded
     */
    public void waitForDOMContentLoaded() {
        logger.debug("Waiting for DOM content to be loaded");
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }

    /**
     * Wait for URL to contain text
     */
    public void waitForUrlContains(String text) {
        waitForUrlContains(text, testConfig.getExplicitWait() * 1000);
    }

    /**
     * Wait for URL to contain text with timeout
     */
    public void waitForUrlContains(String text, int timeoutMs) {
        logger.debug("Waiting for URL to contain: {}", text);
        page.waitForURL("**/*" + text + "*", new Page.WaitForURLOptions().setTimeout(timeoutMs));
    }

    /**
     * Wait for title to contain text
     */
    public void waitForTitleContains(String text) {
        waitForTitleContains(text, testConfig.getExplicitWait() * 1000);
    }

    /**
     * Wait for title to contain text with timeout
     */
    public void waitForTitleContains(String text, int timeoutMs) {
        logger.debug("Waiting for title to contain: {}", text);
        waitForCondition(() -> page.title().contains(text), timeoutMs);
    }

    /**
     * Wait for custom condition
     */
    public void waitForCondition(BooleanSupplier condition, int timeoutMs) {
        logger.debug("Waiting for custom condition");
        long startTime = System.currentTimeMillis();
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw new RuntimeException("Timeout waiting for condition after " + timeoutMs + "ms");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Wait interrupted", e);
            }
        }
    }

    /**
     * Hard wait (sleep)
     */
    public void hardWait(int milliseconds) {
        logger.debug("Hard wait for {} milliseconds", milliseconds);
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Wait interrupted", e);
        }
    }
}

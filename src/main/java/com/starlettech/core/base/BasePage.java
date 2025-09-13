package com.starlettech.core.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.starlettech.config.TestConfig;
import com.starlettech.core.managers.PlaywrightManager;
import com.starlettech.exceptions.BrowserException;
import com.starlettech.exceptions.PageException;
import com.starlettech.utils.WaitUtils;

/**
 * Base Page class containing common page operations
 */
public abstract class BasePage {
    protected final Logger logger = LogManager.getLogger(this.getClass());
    protected Page page;
    protected TestConfig testConfig;
    protected WaitUtils waitUtils;

    public BasePage() {
        this.page = PlaywrightManager.getPage();
        this.testConfig = TestConfig.getInstance();
        this.waitUtils = new WaitUtils(page);

        if (this.page == null) {
            throw BrowserException.pageNotInitialized();
        }
    }

    /**
     * Navigate to URL
     */
    public void navigateTo(String url) {
        logger.info("Navigating to: {}", url);
        try {
            page.navigate(url);
            waitForPageLoad();
        } catch (Exception e) {
            throw PageException.navigationFailed(url);
        }
    }

    /**
     * Get current URL
     */
    public String getCurrentUrl() {
        return page.url();
    }

    /**
     * Get page title
     */
    public String getTitle() {
        return page.title();
    }

    /**
     * Wait for page to load
     */
    public void waitForPageLoad() {
        page.waitForLoadState();
        logger.debug("Page loaded successfully");
    }

    /**
     * Click element
     */
    public void click(String selector) {
        logger.debug("Clicking element: {}", selector);
        try {
            waitForElement(selector);
            page.click(selector);
        } catch (Exception e) {
            throw PageException.clickFailed(selector, e);
        }
    }

    /**
     * Click element with options
     */
    public void click(String selector, Page.ClickOptions options) {
        logger.debug("Clicking element with options: {}", selector);
        waitForElement(selector);
        page.click(selector, options);
    }

    /**
     * Type text into element
     */
    public void type(String selector, String text) {
        logger.debug("Typing '{}' into element: {}", text, selector);
        try {
            waitForElement(selector);
            page.fill(selector, text);
        } catch (Exception e) {
            throw PageException.typeFailed(selector, e);
        }
    }

    /**
     * Clear and type text
     */
    public void clearAndType(String selector, String text) {
        logger.debug("Clearing and typing '{}' into element: {}", text, selector);
        waitForElement(selector);
        page.fill(selector, "");
        page.fill(selector, text);
    }

    /**
     * Get text from element
     */
    public String getText(String selector) {
        logger.debug("Getting text from element: {}", selector);
        waitForElement(selector);
        return page.textContent(selector);
    }

    /**
     * Get attribute value
     */
    public String getAttribute(String selector, String attribute) {
        logger.debug("Getting attribute '{}' from element: {}", attribute, selector);
        waitForElement(selector);
        return page.getAttribute(selector, attribute);
    }

    /**
     * Check if element is visible
     */
    public boolean isVisible(String selector) {
        try {
            return page.isVisible(selector);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if element is enabled
     */
    public boolean isEnabled(String selector) {
        try {
            return page.isEnabled(selector);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Wait for element to be visible
     */
    public void waitForElement(String selector) {
        waitForElement(selector, testConfig.getExplicitWait() * 1000);
    }

    /**
     * Wait for element with timeout
     */
    public void waitForElement(String selector, int timeoutMs) {
        logger.debug("Waiting for element: {}", selector);
        page.waitForSelector(selector, new Page.WaitForSelectorOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(timeoutMs));
    }

    /**
     * Wait for element to disappear
     */
    public void waitForElementToDisappear(String selector) {
        logger.debug("Waiting for element to disappear: {}", selector);
        page.waitForSelector(selector, new Page.WaitForSelectorOptions()
            .setState(WaitForSelectorState.HIDDEN)
            .setTimeout(testConfig.getExplicitWait() * 1000));
    }

    /**
     * Scroll to element
     */
    public void scrollToElement(String selector) {
        logger.debug("Scrolling to element: {}", selector);
        page.locator(selector).scrollIntoViewIfNeeded();
    }

    /**
     * Get locator
     */
    public Locator getLocator(String selector) {
        return page.locator(selector);
    }

    /**
     * Refresh page
     */
    public void refresh() {
        logger.info("Refreshing page");
        page.reload();
        waitForPageLoad();
    }

    /**
     * Go back
     */
    public void goBack() {
        logger.info("Going back");
        page.goBack();
        waitForPageLoad();
    }

    /**
     * Go forward
     */
    public void goForward() {
        logger.info("Going forward");
        page.goForward();
        waitForPageLoad();
    }
}

package com.starlettech.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import com.starlettech.core.PlaywrightManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for element operations
 */
public class ElementUtils {
    private static final Logger logger = LogManager.getLogger(ElementUtils.class);

    /**
     * Get current page instance
     */
    private static Page getPage() {
        Page page = PlaywrightManager.getPage();
        if (page == null) {
            throw new RuntimeException("Page is not initialized. Make sure to call PlaywrightManager.createPage() first.");
        }
        return page;
    }

    /**
     * Check if element exists
     */
    public static boolean isElementPresent(String selector) {
        try {
            Page page = getPage();
            return page.locator(selector).count() > 0;
        } catch (Exception e) {
            logger.debug("Element not present: {}", selector);
            return false;
        }
    }

    /**
     * Check if element is visible
     */
    public static boolean isElementVisible(String selector) {
        try {
            Page page = getPage();
            return page.isVisible(selector);
        } catch (Exception e) {
            logger.debug("Element not visible: {}", selector);
            return false;
        }
    }

    /**
     * Check if element is enabled
     */
    public static boolean isElementEnabled(String selector) {
        try {
            Page page = getPage();
            return page.isEnabled(selector);
        } catch (Exception e) {
            logger.debug("Element not enabled: {}", selector);
            return false;
        }
    }

    /**
     * Check if element is checked (for checkboxes/radio buttons)
     */
    public static boolean isElementChecked(String selector) {
        try {
            Page page = getPage();
            return page.isChecked(selector);
        } catch (Exception e) {
            logger.debug("Element not checked: {}", selector);
            return false;
        }
    }

    /**
     * Get element text content
     */
    public static String getElementText(String selector) {
        try {
            Page page = getPage();
            return page.textContent(selector);
        } catch (Exception e) {
            logger.error("Failed to get text from element {}: {}", selector, e.getMessage());
            return "";
        }
    }

    /**
     * Get element inner text
     */
    public static String getElementInnerText(String selector) {
        try {
            Page page = getPage();
            return page.innerText(selector);
        } catch (Exception e) {
            logger.error("Failed to get inner text from element {}: {}", selector, e.getMessage());
            return "";
        }
    }

    /**
     * Get element attribute value
     */
    public static String getElementAttribute(String selector, String attribute) {
        try {
            Page page = getPage();
            return page.getAttribute(selector, attribute);
        } catch (Exception e) {
            logger.error("Failed to get attribute {} from element {}: {}", attribute, selector, e.getMessage());
            return "";
        }
    }

    /**
     * Get element CSS property value
     */
    public static String getElementCssValue(String selector, String property) {
        try {
            Page page = getPage();
            return page.locator(selector).evaluate("el => getComputedStyle(el).getPropertyValue('" + property + "')").toString();
        } catch (Exception e) {
            logger.error("Failed to get CSS property {} from element {}: {}", property, selector, e.getMessage());
            return "";
        }
    }

    /**
     * Get all elements matching selector
     */
    public static List<Locator> getAllElements(String selector) {
        try {
            Page page = getPage();
            Locator locator = page.locator(selector);
            int count = locator.count();
            return java.util.stream.IntStream.range(0, count)
                    .mapToObj(locator::nth)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get all elements for selector {}: {}", selector, e.getMessage());
            return List.of();
        }
    }

    /**
     * Get element count
     */
    public static int getElementCount(String selector) {
        try {
            Page page = getPage();
            return page.locator(selector).count();
        } catch (Exception e) {
            logger.error("Failed to get element count for selector {}: {}", selector, e.getMessage());
            return 0;
        }
    }

    /**
     * Select option from dropdown by value
     */
    public static void selectByValue(String selector, String value) {
        try {
            Page page = getPage();
            page.selectOption(selector, new SelectOption().setValue(value));
            logger.info("Selected option by value '{}' from dropdown: {}", value, selector);
        } catch (Exception e) {
            logger.error("Failed to select option by value '{}' from dropdown {}: {}", value, selector, e.getMessage());
        }
    }

    /**
     * Select option from dropdown by text
     */
    public static void selectByText(String selector, String text) {
        try {
            Page page = getPage();
            page.selectOption(selector, new SelectOption().setLabel(text));
            logger.info("Selected option by text '{}' from dropdown: {}", text, selector);
        } catch (Exception e) {
            logger.error("Failed to select option by text '{}' from dropdown {}: {}", text, selector, e.getMessage());
        }
    }

    /**
     * Select option from dropdown by index
     */
    public static void selectByIndex(String selector, int index) {
        try {
            Page page = getPage();
            page.selectOption(selector, new SelectOption().setIndex(index));
            logger.info("Selected option by index '{}' from dropdown: {}", index, selector);
        } catch (Exception e) {
            logger.error("Failed to select option by index '{}' from dropdown {}: {}", index, selector, e.getMessage());
        }
    }

    /**
     * Get selected option text from dropdown
     */
    public static String getSelectedOptionText(String selector) {
        try {
            Page page = getPage();
            return page.locator(selector + " option:checked").textContent();
        } catch (Exception e) {
            logger.error("Failed to get selected option text from dropdown {}: {}", selector, e.getMessage());
            return "";
        }
    }

    /**
     * Get all option texts from dropdown
     */
    public static List<String> getAllOptionTexts(String selector) {
        try {
            Page page = getPage();
            return page.locator(selector + " option").allTextContents();
        } catch (Exception e) {
            logger.error("Failed to get all option texts from dropdown {}: {}", selector, e.getMessage());
            return List.of();
        }
    }

    /**
     * Scroll element into view
     */
    public static void scrollIntoView(String selector) {
        try {
            Page page = getPage();
            page.locator(selector).scrollIntoViewIfNeeded();
            logger.debug("Scrolled element into view: {}", selector);
        } catch (Exception e) {
            logger.error("Failed to scroll element into view {}: {}", selector, e.getMessage());
        }
    }

    /**
     * Hover over element
     */
    public static void hoverElement(String selector) {
        try {
            Page page = getPage();
            page.hover(selector);
            logger.debug("Hovered over element: {}", selector);
        } catch (Exception e) {
            logger.error("Failed to hover over element {}: {}", selector, e.getMessage());
        }
    }

    /**
     * Double click element
     */
    public static void doubleClickElement(String selector) {
        try {
            Page page = getPage();
            page.dblclick(selector);
            logger.debug("Double clicked element: {}", selector);
        } catch (Exception e) {
            logger.error("Failed to double click element {}: {}", selector, e.getMessage());
        }
    }

    /**
     * Right click element
     */
    public static void rightClickElement(String selector) {
        try {
            Page page = getPage();
            page.click(selector, new Page.ClickOptions().setButton(com.microsoft.playwright.options.MouseButton.RIGHT));
            logger.debug("Right clicked element: {}", selector);
        } catch (Exception e) {
            logger.error("Failed to right click element {}: {}", selector, e.getMessage());
        }
    }

    /**
     * Focus on element
     */
    public static void focusElement(String selector) {
        try {
            Page page = getPage();
            page.focus(selector);
            logger.debug("Focused on element: {}", selector);
        } catch (Exception e) {
            logger.error("Failed to focus on element {}: {}", selector, e.getMessage());
        }
    }

    /**
     * Clear element content
     */
    public static void clearElement(String selector) {
        try {
            Page page = getPage();
            page.fill(selector, "");
            logger.debug("Cleared element: {}", selector);
        } catch (Exception e) {
            logger.error("Failed to clear element {}: {}", selector, e.getMessage());
        }
    }

    /**
     * Upload file to input element
     */
    public static void uploadFile(String selector, String filePath) {
        try {
            Page page = getPage();
            page.setInputFiles(selector, java.nio.file.Paths.get(filePath));
            logger.info("Uploaded file '{}' to element: {}", filePath, selector);
        } catch (Exception e) {
            logger.error("Failed to upload file '{}' to element {}: {}", filePath, selector, e.getMessage());
        }
    }

    /**
     * Upload multiple files to input element
     */
    public static void uploadFiles(String selector, String[] filePaths) {
        try {
            Page page = getPage();
            java.nio.file.Path[] paths = java.util.Arrays.stream(filePaths)
                    .map(java.nio.file.Paths::get)
                    .toArray(java.nio.file.Path[]::new);
            page.setInputFiles(selector, paths);
            logger.info("Uploaded {} files to element: {}", filePaths.length, selector);
        } catch (Exception e) {
            logger.error("Failed to upload files to element {}: {}", selector, e.getMessage());
        }
    }

    /**
     * Get element bounding box
     */
    public static com.microsoft.playwright.BoundingBox getElementBoundingBox(String selector) {
        try {
            Page page = getPage();
            return page.locator(selector).boundingBox();
        } catch (Exception e) {
            logger.error("Failed to get bounding box for element {}: {}", selector, e.getMessage());
            return null;
        }
    }

    /**
     * Check if element is in viewport
     */
    public static boolean isElementInViewport(String selector) {
        try {
            Page page = getPage();
            return (Boolean) page.locator(selector).evaluate(
                "el => { const rect = el.getBoundingClientRect(); " +
                "return rect.top >= 0 && rect.left >= 0 && " +
                "rect.bottom <= window.innerHeight && rect.right <= window.innerWidth; }"
            );
        } catch (Exception e) {
            logger.error("Failed to check if element is in viewport {}: {}", selector, e.getMessage());
            return false;
        }
    }

    /**
     * Wait for element to be stable (not moving)
     */
    public static void waitForElementToBeStable(String selector) {
        try {
            Page page = getPage();
            page.locator(selector).waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
            // Additional wait for element to be stable
            Thread.sleep(100);
            logger.debug("Element is stable: {}", selector);
        } catch (Exception e) {
            logger.error("Failed to wait for element to be stable {}: {}", selector, e.getMessage());
        }
    }
}

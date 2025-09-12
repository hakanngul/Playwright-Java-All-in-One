package com.starlettech.exceptions;

/**
 * Exception thrown for page-related errors (UI operations)
 */
public class PageException extends FrameworkException {
    
    private final String selector;
    private final String pageName;
    
    public PageException(String message) {
        super(message, ErrorType.PAGE, "FW_PAGE_001");
        this.selector = null;
        this.pageName = null;
    }
    
    public PageException(String message, String selector) {
        super(message, ErrorType.PAGE, "FW_PAGE_002");
        this.selector = selector;
        this.pageName = null;
    }
    
    public PageException(String message, String selector, String pageName) {
        super(message, ErrorType.PAGE, "FW_PAGE_003");
        this.selector = selector;
        this.pageName = pageName;
    }
    
    public PageException(String message, Throwable cause) {
        super(message, cause, ErrorType.PAGE, "FW_PAGE_001");
        this.selector = null;
        this.pageName = null;
    }
    
    public PageException(String message, Throwable cause, String selector) {
        super(message, cause, ErrorType.PAGE, "FW_PAGE_002");
        this.selector = selector;
        this.pageName = null;
    }
    
    public String getSelector() {
        return selector;
    }
    
    public String getPageName() {
        return pageName;
    }
    
    // Specific page error methods
    public static PageException elementNotFound(String selector) {
        return new PageException("Element not found: " + selector, selector);
    }
    
    public static PageException elementNotVisible(String selector) {
        return new PageException("Element is not visible: " + selector, selector);
    }
    
    public static PageException elementNotEnabled(String selector) {
        return new PageException("Element is not enabled: " + selector, selector);
    }
    
    public static PageException navigationFailed(String url) {
        return new PageException("Failed to navigate to: " + url, "FW_PAGE_004");
    }
    
    public static PageException pageLoadTimeout(String url) {
        return new PageException("Page load timeout for: " + url, "FW_PAGE_005");
    }
    
    public static PageException clickFailed(String selector, Throwable cause) {
        return new PageException("Failed to click element: " + selector, cause, selector);
    }
    
    public static PageException typeFailed(String selector, Throwable cause) {
        return new PageException("Failed to type in element: " + selector, cause, selector);
    }
    
    public static PageException waitTimeout(String selector, int timeoutMs) {
        return new PageException(
            String.format("Wait timeout (%dms) for element: %s", timeoutMs, selector), 
            selector);
    }
    
    @Override
    public String toString() {
        String baseString = super.toString();
        if (selector != null) {
            baseString += String.format(" [Selector: %s]", selector);
        }
        if (pageName != null) {
            baseString += String.format(" [Page: %s]", pageName);
        }
        return baseString;
    }
}
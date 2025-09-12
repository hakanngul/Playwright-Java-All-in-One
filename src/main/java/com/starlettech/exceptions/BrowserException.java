package com.starlettech.exceptions;

/**
 * Exception thrown for browser-related errors
 */
public class BrowserException extends FrameworkException {
    
    public BrowserException(String message) {
        super(message, ErrorType.BROWSER, "FW_BROWSER_001");
    }
    
    public BrowserException(String message, Throwable cause) {
        super(message, cause, ErrorType.BROWSER, "FW_BROWSER_001");
    }
    
    public BrowserException(String message, String errorCode) {
        super(message, ErrorType.BROWSER, errorCode);
    }
    
    public BrowserException(String message, Throwable cause, String errorCode) {
        super(message, cause, ErrorType.BROWSER, errorCode);
    }
    
    // Specific browser error methods
    public static BrowserException browserNotLaunched() {
        return new BrowserException("Browser is not launched. Call launchBrowser() first.", "FW_BROWSER_001");
    }
    
    public static BrowserException pageNotInitialized() {
        return new BrowserException("Page is not initialized. Make sure to call PlaywrightManager.createPage() first.", "FW_BROWSER_002");
    }
    
    public static BrowserException contextNotCreated() {
        return new BrowserException("Browser context is not created. Call createContext() first.", "FW_BROWSER_003");
    }
    
    public static BrowserException unsupportedBrowserType(String browserType) {
        return new BrowserException("Unsupported browser type: " + browserType, "FW_BROWSER_004");
    }
    
    public static BrowserException browserLaunchFailed(String browserType, Throwable cause) {
        return new BrowserException("Failed to launch browser: " + browserType, cause, "FW_BROWSER_005");
    }
}
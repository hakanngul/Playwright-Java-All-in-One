package com.starlettech.enums;

/**
 * Enum for supported browser types
 */
public enum BrowserType {
    CHROMIUM("chromium"),
    FIREFOX("firefox"),
    WEBKIT("webkit"),
    CHROME("chrome"),
    EDGE("msedge");

    private final String browserName;

    BrowserType(String browserName) {
        this.browserName = browserName;
    }

    public String getBrowserName() {
        return browserName;
    }

    public static BrowserType fromString(String browser) {
        for (BrowserType type : BrowserType.values()) {
            if (type.browserName.equalsIgnoreCase(browser) || type.name().equalsIgnoreCase(browser)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown browser type: " + browser);
    }
}

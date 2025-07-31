package starlettech.pages;

import com.starlettech.core.BasePage;

/**
 * Home Page Object
 */
public class HomePage extends BasePage {

    // Locators
    private static final String WELCOME_MESSAGE = "#welcome-message";
    private static final String USER_PROFILE = "#user-profile";
    private static final String LOGOUT_BUTTON = "#logout-btn";
    private static final String NAVIGATION_MENU = "#nav-menu";
    private static final String DASHBOARD_LINK = "#dashboard-link";
    private static final String PROFILE_LINK = "#profile-link";
    private static final String SETTINGS_LINK = "#settings-link";
    private static final String NOTIFICATIONS = "#notifications";
    private static final String SEARCH_BOX = "#search-box";
    private static final String MAIN_CONTENT = "#main-content";

    /**
     * Navigate to home page
     */
    public void navigateToHomePage() {
        navigateTo(testConfig.getBaseUrl() + "/home");
        waitForElement(MAIN_CONTENT);
        logger.info("Navigated to home page");
    }

    /**
     * Get welcome message text
     */
    public String getWelcomeMessage() {
        logger.info("Getting welcome message");
        waitForElement(WELCOME_MESSAGE);
        return getText(WELCOME_MESSAGE);
    }

    /**
     * Click logout button
     */
    public void clickLogout() {
        logger.info("Clicking logout button");
        click(LOGOUT_BUTTON);
    }

    /**
     * Click dashboard link
     */
    public void clickDashboard() {
        logger.info("Clicking dashboard link");
        click(DASHBOARD_LINK);
    }

    /**
     * Click profile link
     */
    public void clickProfile() {
        logger.info("Clicking profile link");
        click(PROFILE_LINK);
    }

    /**
     * Click settings link
     */
    public void clickSettings() {
        logger.info("Clicking settings link");
        click(SETTINGS_LINK);
    }

    /**
     * Search for content
     */
    public void search(String searchTerm) {
        logger.info("Searching for: {}", searchTerm);
        type(SEARCH_BOX, searchTerm);
        page.keyboard().press("Enter");
    }

    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return isVisible(USER_PROFILE) && isVisible(LOGOUT_BUTTON);
    }

    /**
     * Check if welcome message is displayed
     */
    public boolean isWelcomeMessageDisplayed() {
        return isVisible(WELCOME_MESSAGE);
    }

    /**
     * Check if navigation menu is displayed
     */
    public boolean isNavigationMenuDisplayed() {
        return isVisible(NAVIGATION_MENU);
    }

    /**
     * Check if logout button is displayed
     */
    public boolean isLogoutButtonDisplayed() {
        return isVisible(LOGOUT_BUTTON);
    }

    /**
     * Get user profile text
     */
    public String getUserProfileText() {
        logger.info("Getting user profile text");
        waitForElement(USER_PROFILE);
        return getText(USER_PROFILE);
    }

    /**
     * Check if notifications are displayed
     */
    public boolean areNotificationsDisplayed() {
        return isVisible(NOTIFICATIONS);
    }

    /**
     * Get notifications count
     */
    public String getNotificationsCount() {
        if (areNotificationsDisplayed()) {
            return getAttribute(NOTIFICATIONS, "data-count");
        }
        return "0";
    }

    /**
     * Wait for home page to load
     */
    public void waitForHomePageToLoad() {
        waitForElement(MAIN_CONTENT);
        waitForElement(NAVIGATION_MENU);
        waitForElement(USER_PROFILE);
        logger.info("Home page loaded successfully");
    }

    /**
     * Verify home page elements
     */
    public boolean verifyHomePageElements() {
        return isNavigationMenuDisplayed() &&
               isUserLoggedIn() &&
               isVisible(MAIN_CONTENT);
    }
}

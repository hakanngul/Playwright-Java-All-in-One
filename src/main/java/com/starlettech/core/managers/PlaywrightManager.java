package com.starlettech.core.managers;

import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.starlettech.config.BrowserConfig;
import com.starlettech.enums.BrowserType;

/**
 * Playwright Manager for handling browser instances and pages
 */
public class PlaywrightManager {
    private static final Logger logger = LogManager.getLogger(PlaywrightManager.class);
    private static final ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

    private static BrowserConfig browserConfig = BrowserConfig.getInstance();

    /**
     * Initialize Playwright instance
     */
    public static void initializePlaywright() {
        if (playwrightThreadLocal.get() == null) {
            Playwright playwright = Playwright.create();
            playwrightThreadLocal.set(playwright);
            logger.info("Playwright initialized successfully");
        }
    }

    /**
     * Launch browser with configuration
     */
    public static void launchBrowser() {
        launchBrowser(browserConfig.getBrowserType());
    }

    /**
     * Launch browser with specific browser type
     */
    public static void launchBrowser(BrowserType browserType) {
        initializePlaywright();

        Playwright playwright = playwrightThreadLocal.get();
        Browser browser = createBrowser(playwright, browserType);
        browserThreadLocal.set(browser);

        logger.info("Browser {} launched successfully", browserType.getBrowserName());
    }

    private static Browser createBrowser(Playwright playwright, BrowserType browserType) {
        @SuppressWarnings("deprecation")
        com.microsoft.playwright.BrowserType.LaunchOptions launchOptions = new com.microsoft.playwright.BrowserType.LaunchOptions()
                .setHeadless(browserConfig.isHeadless())
                .setSlowMo(browserConfig.isSlowMo() ? browserConfig.getSlowMoDelay() : 0)
                .setDevtools(browserConfig.isDevtools());

        List<String> args = browserConfig.getBrowserArgs();
        if (!args.isEmpty()) {
            launchOptions.setArgs(args);
        }

        return switch (browserType) {
            case CHROMIUM -> playwright.chromium().launch(launchOptions);
            case FIREFOX -> playwright.firefox().launch(launchOptions);
            case WEBKIT -> playwright.webkit().launch(launchOptions);
            case CHROME -> playwright.chromium().launch(launchOptions.setChannel("chrome"));
            case EDGE -> playwright.chromium().launch(launchOptions.setChannel("msedge"));
        };
    }

    /**
     * Create new browser context
     */
    public static void createContext() {
        Browser browser = browserThreadLocal.get();
        if (browser == null) {
            throw new RuntimeException("Browser is not launched. Call launchBrowser() first.");
        }

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();

        // Set viewport
        String viewport = browserConfig.getViewportSize();
        String[] dimensions = viewport.split("x");
        if (dimensions.length == 2) {
            contextOptions.setViewportSize(
                Integer.parseInt(dimensions[0]),
                Integer.parseInt(dimensions[1])
            );
        }

        // Set video recording if enabled
        if (browserConfig.isVideoRecording()) {
            contextOptions.setRecordVideoDir(Paths.get(browserConfig.getVideoPath()));
        }

        BrowserContext context = browser.newContext(contextOptions);

        // Set tracing if enabled
        if (browserConfig.isTracing()) {
            context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        }

        contextThreadLocal.set(context);
        logger.info("Browser context created successfully");
    }

    /**
     * Create new page
     */
    public static void createPage() {
        BrowserContext context = contextThreadLocal.get();
        if (context == null) {
            createContext();
            context = contextThreadLocal.get();
        }

        Page page = context.newPage();
        page.setDefaultTimeout(browserConfig.getTimeout());
        pageThreadLocal.set(page);
        logger.info("New page created successfully");
    }

    /**
     * Get current Playwright instance
     */
    public static Playwright getPlaywright() {
        return playwrightThreadLocal.get();
    }

    /**
     * Get current browser instance
     */
    public static Browser getBrowser() {
        return browserThreadLocal.get();
    }

    /**
     * Get current browser context
     */
    public static BrowserContext getContext() {
        return contextThreadLocal.get();
    }

    /**
     * Get current page instance
     */
    public static Page getPage() {
        return pageThreadLocal.get();
    }

    /**
     * Close current page
     */
    public static void closePage() {
        Page page = pageThreadLocal.get();
        if (page != null) {
            page.close();
            pageThreadLocal.remove();
            logger.info("Page closed successfully");
        }
    }

    /**
     * Close browser context
     */
    public static void closeContext() {
        BrowserContext context = contextThreadLocal.get();
        if (context != null) {
            // Stop tracing if enabled
            if (browserConfig.isTracing()) {
                context.tracing().stop(new Tracing.StopOptions()
                    .setPath(Paths.get(browserConfig.getTracePath(), "trace.zip")));
            }
            context.close();
            contextThreadLocal.remove();
            logger.info("Browser context closed successfully");
        }
    }

    /**
     * Close browser
     */
    public static void closeBrowser() {
        Browser browser = browserThreadLocal.get();
        if (browser != null) {
            browser.close();
            browserThreadLocal.remove();
            logger.info("Browser closed successfully");
        }
    }

    /**
     * Close Playwright
     */
    public static void closePlaywright() {
        Playwright playwright = playwrightThreadLocal.get();
        if (playwright != null) {
            playwright.close();
            playwrightThreadLocal.remove();
            logger.info("Playwright closed successfully");
        }
    }

    /**
     * Cleanup all resources
     */
    public static void cleanup() {
        closePage();
        closeContext();
        closeBrowser();
        closePlaywright();
        logger.info("All Playwright resources cleaned up");
    }
}

package com.starlettech.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * Thread-safe manager for Playwright resources and test data
 */
public class ThreadLocalManager {
    private static final Logger logger = LogManager.getLogger(ThreadLocalManager.class);

    // Playwright resources
    private static final ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<APIRequestContext> apiContextThreadLocal = new ThreadLocal<>();

    // Test data and configuration
    private static final ThreadLocal<Map<String, Object>> testDataThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> currentTestNameThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Long> testStartTimeThreadLocal = new ThreadLocal<>();

    // Thread tracking for cleanup
    private static final Map<Long, String> activeThreads = new ConcurrentHashMap<>();

    // ========== Playwright Resource Management ==========

    public static void setPlaywright(Playwright playwright) {
        playwrightThreadLocal.set(playwright);
        trackThread("Playwright");
    }

    public static Playwright getPlaywright() {
        return playwrightThreadLocal.get();
    }

    public static void setBrowser(Browser browser) {
        browserThreadLocal.set(browser);
        trackThread("Browser");
    }

    public static Browser getBrowser() {
        return browserThreadLocal.get();
    }

    public static void setContext(BrowserContext context) {
        contextThreadLocal.set(context);
        trackThread("Context");
    }

    public static BrowserContext getContext() {
        return contextThreadLocal.get();
    }

    public static void setPage(Page page) {
        pageThreadLocal.set(page);
        trackThread("Page");
    }

    public static Page getPage() {
        return pageThreadLocal.get();
    }

    public static void setApiContext(APIRequestContext apiContext) {
        apiContextThreadLocal.set(apiContext);
        trackThread("APIContext");
    }

    public static APIRequestContext getApiContext() {
        return apiContextThreadLocal.get();
    }

    // ========== Test Data Management ==========

    public static void setTestData(String key, Object value) {
        Map<String, Object> testData = testDataThreadLocal.get();
        if (testData == null) {
            testData = new HashMap<>();
            testDataThreadLocal.set(testData);
        }
        testData.put(key, value);
        logger.debug("Set test data: {} = {}", key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getTestData(String key) {
        return Optional.ofNullable(testDataThreadLocal.get())
                .map(data -> (T) data.get(key));
    }

    public static void removeTestData(String key) {
        Map<String, Object> testData = testDataThreadLocal.get();
        if (testData != null) {
            testData.remove(key);
            logger.debug("Removed test data: {}", key);
        }
    }

    public static Map<String, Object> getAllTestData() {
        Map<String, Object> testData = testDataThreadLocal.get();
        return testData != null ? new HashMap<>(testData) : new HashMap<>();
    }

    // ========== Test Execution Tracking ==========

    public static void setCurrentTestName(String testName) {
        currentTestNameThreadLocal.set(testName);
        trackThread("Test: " + testName);
    }

    public static String getCurrentTestName() {
        return currentTestNameThreadLocal.get();
    }

    public static void setTestStartTime(long startTime) {
        testStartTimeThreadLocal.set(startTime);
    }

    public static Long getTestStartTime() {
        return testStartTimeThreadLocal.get();
    }

    public static long getTestDuration() {
        Long startTime = getTestStartTime();
        return startTime != null ? System.currentTimeMillis() - startTime : 0;
    }

    // ========== Thread Tracking ==========

    private static void trackThread(String resource) {
        long threadId = Thread.currentThread().getId();
        String threadName = Thread.currentThread().getName();
        activeThreads.put(threadId, threadName + " (" + resource + ")");
    }

    public static Map<Long, String> getActiveThreads() {
        return new HashMap<>(activeThreads);
    }

    public static int getActiveThreadCount() {
        return activeThreads.size();
    }

    // ========== Resource Cleanup ==========

    public static void cleanupCurrentThread() {
        long threadId = Thread.currentThread().getId();
        String threadName = Thread.currentThread().getName();

        logger.debug("Cleaning up resources for thread: {} ({})", threadId, threadName);

        // Close Playwright resources
        try {
            Page page = getPage();
            if (page != null && !page.isClosed()) {
                page.close();
                logger.debug("Page closed for thread: {}", threadId);
            }
        } catch (Exception e) {
            logger.warn("Error closing page for thread {}: {}", threadId, e.getMessage());
        }

        try {
            BrowserContext context = getContext();
            if (context != null) {
                context.close();
                logger.debug("Context closed for thread: {}", threadId);
            }
        } catch (Exception e) {
            logger.warn("Error closing context for thread {}: {}", threadId, e.getMessage());
        }

        try {
            Browser browser = getBrowser();
            if (browser != null && browser.isConnected()) {
                browser.close();
                logger.debug("Browser closed for thread: {}", threadId);
            }
        } catch (Exception e) {
            logger.warn("Error closing browser for thread {}: {}", threadId, e.getMessage());
        }

        try {
            APIRequestContext apiContext = getApiContext();
            if (apiContext != null) {
                apiContext.dispose();
                logger.debug("API context disposed for thread: {}", threadId);
            }
        } catch (Exception e) {
            logger.warn("Error disposing API context for thread {}: {}", threadId, e.getMessage());
        }

        try {
            Playwright playwright = getPlaywright();
            if (playwright != null) {
                playwright.close();
                logger.debug("Playwright closed for thread: {}", threadId);
            }
        } catch (Exception e) {
            logger.warn("Error closing Playwright for thread {}: {}", threadId, e.getMessage());
        }

        // Clear ThreadLocal variables
        playwrightThreadLocal.remove();
        browserThreadLocal.remove();
        contextThreadLocal.remove();
        pageThreadLocal.remove();
        apiContextThreadLocal.remove();
        testDataThreadLocal.remove();
        currentTestNameThreadLocal.remove();
        testStartTimeThreadLocal.remove();

        // Remove from active threads
        activeThreads.remove(threadId);

        logger.info("Thread cleanup completed for: {} ({})", threadId, threadName);
    }

    public static void cleanupAllThreads() {
        logger.info("Starting cleanup for all active threads. Active threads: {}", activeThreads.size());

        for (Long threadId : activeThreads.keySet()) {
            logger.debug("Force cleanup for thread: {}", threadId);
        }

        // Clear all tracking
        activeThreads.clear();

        logger.info("All threads cleanup completed");
    }

    // ========== Utility Methods ==========

    public static boolean hasActiveResources() {
        return getPlaywright() != null || getBrowser() != null ||
                getContext() != null || getPage() != null || getApiContext() != null;
    }

    public static String getThreadInfo() {
        long threadId = Thread.currentThread().getId();
        String threadName = Thread.currentThread().getName();
        String testName = getCurrentTestName();
        long duration = getTestDuration();

        return String.format("Thread[%d:%s] Test[%s] Duration[%dms]",
                threadId, threadName, testName != null ? testName : "N/A", duration);
    }
}

package com.starlettech.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test Metrics Collector for framework monitoring and reporting
 */
public class TestMetricsCollector {
    private static final Logger logger = LogManager.getLogger(TestMetricsCollector.class);
    
    // Test execution metrics
    private static final AtomicInteger totalTests = new AtomicInteger(0);
    private static final AtomicInteger passedTests = new AtomicInteger(0);
    private static final AtomicInteger failedTests = new AtomicInteger(0);
    private static final AtomicInteger skippedTests = new AtomicInteger(0);
    private static final AtomicInteger retriedTests = new AtomicInteger(0);
    
    // Performance metrics
    private static final AtomicLong totalExecutionTime = new AtomicLong(0);
    private static final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
    private static final AtomicLong maxExecutionTime = new AtomicLong(0);
    
    // Test details
    private static final Map<String, TestExecution> testExecutions = new ConcurrentHashMap<>();
    private static final Map<String, List<Long>> testExecutionTimes = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> testRetryCount = new ConcurrentHashMap<>();
    
    // Browser and environment metrics
    private static final Map<String, AtomicInteger> browserUsage = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> environmentUsage = new ConcurrentHashMap<>();
    
    // Error tracking
    private static final Map<String, AtomicInteger> errorTypes = new ConcurrentHashMap<>();
    private static final List<TestFailure> recentFailures = Collections.synchronizedList(new ArrayList<>());
    
    // Session tracking
    private static final long sessionStartTime = System.currentTimeMillis();
    private static final String sessionId = UUID.randomUUID().toString().substring(0, 8);

    /**
     * Record test start
     */
    public static void recordTestStart(String testName, String className, String browserType, String environment) {
        totalTests.incrementAndGet();
        
        TestExecution execution = new TestExecution(testName, className, browserType, environment);
        testExecutions.put(getTestKey(className, testName), execution);
        
        // Track browser usage
        browserUsage.computeIfAbsent(browserType, k -> new AtomicInteger(0)).incrementAndGet();
        
        // Track environment usage
        environmentUsage.computeIfAbsent(environment, k -> new AtomicInteger(0)).incrementAndGet();
        
        logger.debug("Test started: {}.{} [Browser: {}, Environment: {}]", className, testName, browserType, environment);
    }

    /**
     * Record test completion
     */
    public static void recordTestCompletion(String testName, String className, TestResult result, long executionTime, Throwable error) {
        String testKey = getTestKey(className, testName);
        TestExecution execution = testExecutions.get(testKey);
        
        if (execution != null) {
            execution.complete(result, executionTime, error);
        }
        
        // Update counters
        switch (result) {
            case PASSED -> passedTests.incrementAndGet();
            case FAILED -> {
                failedTests.incrementAndGet();
                recordTestFailure(testName, className, error);
            }
            case SKIPPED -> skippedTests.incrementAndGet();
        }
        
        // Update performance metrics
        totalExecutionTime.addAndGet(executionTime);
        updateMinMaxExecutionTime(executionTime);
        
        // Track execution times for this test
        testExecutionTimes.computeIfAbsent(testKey, k -> Collections.synchronizedList(new ArrayList<>()))
                          .add(executionTime);
        
        logger.debug("Test completed: {}.{} [Result: {}, Duration: {}ms]", className, testName, result, executionTime);
    }

    /**
     * Record test retry
     */
    public static void recordTestRetry(String testName, String className) {
        retriedTests.incrementAndGet();
        String testKey = getTestKey(className, testName);
        testRetryCount.computeIfAbsent(testKey, k -> new AtomicInteger(0)).incrementAndGet();
        
        logger.debug("Test retry recorded: {}.{}", className, testName);
    }

    /**
     * Record test failure details
     */
    private static void recordTestFailure(String testName, String className, Throwable error) {
        if (error != null) {
            String errorType = error.getClass().getSimpleName();
            errorTypes.computeIfAbsent(errorType, k -> new AtomicInteger(0)).incrementAndGet();
            
            TestFailure failure = new TestFailure(testName, className, error);
            recentFailures.add(failure);
            
            // Keep only recent failures (last 50)
            if (recentFailures.size() > 50) {
                recentFailures.remove(0);
            }
        }
    }

    /**
     * Update min/max execution times
     */
    private static void updateMinMaxExecutionTime(long executionTime) {
        minExecutionTime.updateAndGet(current -> Math.min(current, executionTime));
        maxExecutionTime.updateAndGet(current -> Math.max(current, executionTime));
    }

    /**
     * Get test execution summary
     */
    public static TestExecutionSummary getExecutionSummary() {
        return new TestExecutionSummary(
            sessionId,
            sessionStartTime,
            totalTests.get(),
            passedTests.get(),
            failedTests.get(),
            skippedTests.get(),
            retriedTests.get(),
            totalExecutionTime.get(),
            minExecutionTime.get() == Long.MAX_VALUE ? 0 : minExecutionTime.get(),
            maxExecutionTime.get(),
            calculateAverageExecutionTime(),
            calculateSuccessRate(),
            new HashMap<>(browserUsage),
            new HashMap<>(environmentUsage),
            new HashMap<>(errorTypes)
        );
    }

    /**
     * Get detailed test metrics
     */
    public static Map<String, TestMetrics> getDetailedMetrics() {
        Map<String, TestMetrics> metrics = new HashMap<>();
        
        for (Map.Entry<String, TestExecution> entry : testExecutions.entrySet()) {
            String testKey = entry.getKey();
            TestExecution execution = entry.getValue();
            
            List<Long> executionTimes = testExecutionTimes.getOrDefault(testKey, new ArrayList<>());
            int retryCount = testRetryCount.getOrDefault(testKey, new AtomicInteger(0)).get();
            
            TestMetrics testMetrics = new TestMetrics(
                execution.getTestName(),
                execution.getClassName(),
                execution.getBrowserType(),
                execution.getEnvironment(),
                execution.getResult(),
                execution.getExecutionTime(),
                executionTimes,
                retryCount,
                execution.getStartTime(),
                execution.getEndTime(),
                execution.getError()
            );
            
            metrics.put(testKey, testMetrics);
        }
        
        return metrics;
    }

    /**
     * Get recent test failures
     */
    public static List<TestFailure> getRecentFailures() {
        return new ArrayList<>(recentFailures);
    }

    /**
     * Calculate average execution time
     */
    private static long calculateAverageExecutionTime() {
        int total = totalTests.get();
        return total > 0 ? totalExecutionTime.get() / total : 0;
    }

    /**
     * Calculate success rate
     */
    private static double calculateSuccessRate() {
        int total = totalTests.get();
        return total > 0 ? (double) passedTests.get() / total * 100 : 0.0;
    }

    /**
     * Generate test key
     */
    private static String getTestKey(String className, String testName) {
        return className + "." + testName;
    }

    /**
     * Reset all metrics
     */
    public static void reset() {
        totalTests.set(0);
        passedTests.set(0);
        failedTests.set(0);
        skippedTests.set(0);
        retriedTests.set(0);
        totalExecutionTime.set(0);
        minExecutionTime.set(Long.MAX_VALUE);
        maxExecutionTime.set(0);
        
        testExecutions.clear();
        testExecutionTimes.clear();
        testRetryCount.clear();
        browserUsage.clear();
        environmentUsage.clear();
        errorTypes.clear();
        recentFailures.clear();
        
        logger.info("Test metrics reset");
    }

    /**
     * Print metrics summary to console
     */
    public static void printSummary() {
        TestExecutionSummary summary = getExecutionSummary();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST EXECUTION SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println("Session ID: " + summary.getSessionId());
        System.out.println("Start Time: " + LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(summary.getSessionStartTime()), 
            java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("Duration: " + formatDuration(System.currentTimeMillis() - summary.getSessionStartTime()));
        System.out.println();
        System.out.println("Test Results:");
        System.out.println("  Total: " + summary.getTotalTests());
        System.out.println("  Passed: " + summary.getPassedTests());
        System.out.println("  Failed: " + summary.getFailedTests());
        System.out.println("  Skipped: " + summary.getSkippedTests());
        System.out.println("  Retried: " + summary.getRetriedTests());
        System.out.println("  Success Rate: " + String.format("%.2f%%", summary.getSuccessRate()));
        System.out.println();
        System.out.println("Performance:");
        System.out.println("  Total Execution Time: " + formatDuration(summary.getTotalExecutionTime()));
        System.out.println("  Average Execution Time: " + formatDuration(summary.getAverageExecutionTime()));
        System.out.println("  Min Execution Time: " + formatDuration(summary.getMinExecutionTime()));
        System.out.println("  Max Execution Time: " + formatDuration(summary.getMaxExecutionTime()));
        System.out.println("=".repeat(60));
    }

    /**
     * Format duration in milliseconds to human readable format
     */
    private static String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else if (milliseconds < 60000) {
            return String.format("%.2fs", milliseconds / 1000.0);
        } else {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }

    // ========== Inner Classes ==========

    public enum TestResult {
        PASSED, FAILED, SKIPPED
    }

    public static class TestExecution {
        private final String testName;
        private final String className;
        private final String browserType;
        private final String environment;
        private final long startTime;
        private long endTime;
        private TestResult result;
        private long executionTime;
        private Throwable error;

        public TestExecution(String testName, String className, String browserType, String environment) {
            this.testName = testName;
            this.className = className;
            this.browserType = browserType;
            this.environment = environment;
            this.startTime = System.currentTimeMillis();
        }

        public void complete(TestResult result, long executionTime, Throwable error) {
            this.endTime = System.currentTimeMillis();
            this.result = result;
            this.executionTime = executionTime;
            this.error = error;
        }

        // Getters
        public String getTestName() { return testName; }
        public String getClassName() { return className; }
        public String getBrowserType() { return browserType; }
        public String getEnvironment() { return environment; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public TestResult getResult() { return result; }
        public long getExecutionTime() { return executionTime; }
        public Throwable getError() { return error; }
    }

    public static class TestExecutionSummary {
        private final String sessionId;
        private final long sessionStartTime;
        private final int totalTests;
        private final int passedTests;
        private final int failedTests;
        private final int skippedTests;
        private final int retriedTests;
        private final long totalExecutionTime;
        private final long minExecutionTime;
        private final long maxExecutionTime;
        private final long averageExecutionTime;
        private final double successRate;
        private final Map<String, AtomicInteger> browserUsage;
        private final Map<String, AtomicInteger> environmentUsage;
        private final Map<String, AtomicInteger> errorTypes;

        public TestExecutionSummary(String sessionId, long sessionStartTime, int totalTests, int passedTests,
                                   int failedTests, int skippedTests, int retriedTests, long totalExecutionTime,
                                   long minExecutionTime, long maxExecutionTime, long averageExecutionTime,
                                   double successRate, Map<String, AtomicInteger> browserUsage,
                                   Map<String, AtomicInteger> environmentUsage, Map<String, AtomicInteger> errorTypes) {
            this.sessionId = sessionId;
            this.sessionStartTime = sessionStartTime;
            this.totalTests = totalTests;
            this.passedTests = passedTests;
            this.failedTests = failedTests;
            this.skippedTests = skippedTests;
            this.retriedTests = retriedTests;
            this.totalExecutionTime = totalExecutionTime;
            this.minExecutionTime = minExecutionTime;
            this.maxExecutionTime = maxExecutionTime;
            this.averageExecutionTime = averageExecutionTime;
            this.successRate = successRate;
            this.browserUsage = browserUsage;
            this.environmentUsage = environmentUsage;
            this.errorTypes = errorTypes;
        }

        // Getters
        public String getSessionId() { return sessionId; }
        public long getSessionStartTime() { return sessionStartTime; }
        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
        public int getFailedTests() { return failedTests; }
        public int getSkippedTests() { return skippedTests; }
        public int getRetriedTests() { return retriedTests; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public long getMinExecutionTime() { return minExecutionTime; }
        public long getMaxExecutionTime() { return maxExecutionTime; }
        public long getAverageExecutionTime() { return averageExecutionTime; }
        public double getSuccessRate() { return successRate; }
        public Map<String, AtomicInteger> getBrowserUsage() { return browserUsage; }
        public Map<String, AtomicInteger> getEnvironmentUsage() { return environmentUsage; }
        public Map<String, AtomicInteger> getErrorTypes() { return errorTypes; }
    }

    public static class TestMetrics {
        private final String testName;
        private final String className;
        private final String browserType;
        private final String environment;
        private final TestResult result;
        private final long executionTime;
        private final List<Long> allExecutionTimes;
        private final int retryCount;
        private final long startTime;
        private final long endTime;
        private final Throwable error;

        public TestMetrics(String testName, String className, String browserType, String environment,
                          TestResult result, long executionTime, List<Long> allExecutionTimes, int retryCount,
                          long startTime, long endTime, Throwable error) {
            this.testName = testName;
            this.className = className;
            this.browserType = browserType;
            this.environment = environment;
            this.result = result;
            this.executionTime = executionTime;
            this.allExecutionTimes = allExecutionTimes;
            this.retryCount = retryCount;
            this.startTime = startTime;
            this.endTime = endTime;
            this.error = error;
        }

        // Getters
        public String getTestName() { return testName; }
        public String getClassName() { return className; }
        public String getBrowserType() { return browserType; }
        public String getEnvironment() { return environment; }
        public TestResult getResult() { return result; }
        public long getExecutionTime() { return executionTime; }
        public List<Long> getAllExecutionTimes() { return allExecutionTimes; }
        public int getRetryCount() { return retryCount; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public Throwable getError() { return error; }
    }

    public static class TestFailure {
        private final String testName;
        private final String className;
        private final Throwable error;
        private final long timestamp;

        public TestFailure(String testName, String className, Throwable error) {
            this.testName = testName;
            this.className = className;
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public String getTestName() { return testName; }
        public String getClassName() { return className; }
        public Throwable getError() { return error; }
        public long getTimestamp() { return timestamp; }
    }
}

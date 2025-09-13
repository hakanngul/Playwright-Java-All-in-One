package com.starlettech.core;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.starlettech.annotations.PerformanceTest;
import com.sun.management.OperatingSystemMXBean;

/**
 * Handler for performance test monitoring and validation
 */
public class PerformanceTestHandler {
    private static final Logger logger = LogManager.getLogger(PerformanceTestHandler.class);
    private static final ConcurrentHashMap<String, PerformanceMetrics> activeTests = new ConcurrentHashMap<>();
    private static final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    /**
     * Start performance monitoring for a test
     */
    public static PerformanceMetrics startPerformanceMonitoring(Method testMethod) {
        PerformanceTest annotation = getPerformanceAnnotation(testMethod);
        if (annotation == null) {
            return null;
        }

        String testKey = testMethod.getDeclaringClass().getSimpleName() + "." + testMethod.getName();
        PerformanceMetrics metrics = new PerformanceMetrics(annotation);
        activeTests.put(testKey, metrics);

        logger.info("Started performance monitoring for test: {} with maxResponseTime: {}ms, concurrentUsers: {}", 
                   testKey, annotation.maxResponseTime(), annotation.concurrentUsers());

        return metrics;
    }

    /**
     * Stop performance monitoring and validate results
     */
    public static PerformanceResult stopPerformanceMonitoring(Method testMethod) {
        PerformanceTest annotation = getPerformanceAnnotation(testMethod);
        if (annotation == null) {
            return null;
        }

        String testKey = testMethod.getDeclaringClass().getSimpleName() + "." + testMethod.getName();
        PerformanceMetrics metrics = activeTests.remove(testKey);
        
        if (metrics == null) {
            logger.warn("No performance metrics found for test: {}", testKey);
            return null;
        }

        metrics.stopMonitoring();
        PerformanceResult result = validatePerformance(metrics, annotation);

        logger.info("Performance monitoring completed for test: {} - Result: {}", 
                   testKey, result.isPassed() ? "PASSED" : "FAILED");

        return result;
    }

    /**
     * Record a request for performance tracking
     */
    public static void recordRequest(Method testMethod, long responseTime) {
        String testKey = testMethod.getDeclaringClass().getSimpleName() + "." + testMethod.getName();
        PerformanceMetrics metrics = activeTests.get(testKey);
        
        if (metrics != null) {
            metrics.recordRequest(responseTime);
        }
    }

    /**
     * Get performance annotation from method or class
     */
    private static PerformanceTest getPerformanceAnnotation(Method method) {
        PerformanceTest methodAnnotation = method.getAnnotation(PerformanceTest.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return method.getDeclaringClass().getAnnotation(PerformanceTest.class);
    }

    /**
     * Validate performance metrics against annotation requirements
     */
    private static PerformanceResult validatePerformance(PerformanceMetrics metrics, PerformanceTest annotation) {
        PerformanceResult result = new PerformanceResult();
        result.setMetrics(metrics);

        // Validate response time
        if (metrics.getMaxResponseTime() > annotation.maxResponseTime()) {
            result.addViolation("Max response time exceeded: " + metrics.getMaxResponseTime() + "ms > " + annotation.maxResponseTime() + "ms");
        }

        // Validate average response time (should be within 70% of max allowed)
        double avgThreshold = annotation.maxResponseTime() * 0.7;
        if (metrics.getAverageResponseTime() > avgThreshold) {
            result.addViolation("Average response time too high: " + metrics.getAverageResponseTime() + "ms > " + avgThreshold + "ms");
        }

        // Validate CPU usage
        if (metrics.getMaxCpuUsage() > annotation.maxCpuUsage()) {
            result.addViolation("Max CPU usage exceeded: " + metrics.getMaxCpuUsage() + "% > " + annotation.maxCpuUsage() + "%");
        }

        // Validate memory usage
        if (metrics.getMaxMemoryUsage() > annotation.maxMemoryUsage()) {
            result.addViolation("Max memory usage exceeded: " + metrics.getMaxMemoryUsage() + "MB > " + annotation.maxMemoryUsage() + "MB");
        }

        // Validate throughput
        if (annotation.expectedThroughput() > 0 && metrics.getThroughput() < annotation.expectedThroughput()) {
            result.addViolation("Throughput below expected: " + metrics.getThroughput() + " req/s < " + annotation.expectedThroughput() + " req/s");
        }

        // Validate test duration
        if (metrics.getActualDuration() > annotation.duration() * 1.1) { // Allow 10% tolerance
            result.addViolation("Test duration exceeded: " + metrics.getActualDuration() + "s > " + annotation.duration() + "s");
        }

        result.setPassed(result.getViolations().isEmpty());
        return result;
    }

    /**
     * Performance metrics tracking class
     */
    public static class PerformanceMetrics {
        private final PerformanceTest annotation;
        private final Instant startTime;
        private Instant endTime;
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final AtomicLong maxResponseTime = new AtomicLong(0);
        private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        private volatile double maxCpuUsage = 0.0;
        private volatile long maxMemoryUsage = 0L;

        public PerformanceMetrics(PerformanceTest annotation) {
            this.annotation = annotation;
            this.startTime = Instant.now();
            updateSystemMetrics();
        }

        public void recordRequest(long responseTime) {
            totalResponseTime.addAndGet(responseTime);
            requestCount.incrementAndGet();
            maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));
            minResponseTime.updateAndGet(current -> Math.min(current, responseTime));
            updateSystemMetrics();
        }

        public void stopMonitoring() {
            this.endTime = Instant.now();
            updateSystemMetrics();
        }

        private void updateSystemMetrics() {
            // Update CPU usage
            double cpuUsage = osBean.getProcessCpuLoad() * 100;
            if (cpuUsage > 0) {
                maxCpuUsage = Math.max(maxCpuUsage, cpuUsage);
            }

            // Update memory usage
            long memoryUsage = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024); // Convert to MB
            maxMemoryUsage = Math.max(maxMemoryUsage, memoryUsage);
        }

        // Getters
        public double getAverageResponseTime() {
            return requestCount.get() > 0 ? (double) totalResponseTime.get() / requestCount.get() : 0;
        }

        public long getMaxResponseTime() {
            return maxResponseTime.get();
        }

        public long getMinResponseTime() {
            return minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get();
        }

        public double getThroughput() {
            long duration = Duration.between(startTime, endTime != null ? endTime : Instant.now()).toSeconds();
            return duration > 0 ? (double) requestCount.get() / duration : 0;
        }

        public double getMaxCpuUsage() {
            return maxCpuUsage;
        }

        public long getMaxMemoryUsage() {
            return maxMemoryUsage;
        }

        public long getActualDuration() {
            return Duration.between(startTime, endTime != null ? endTime : Instant.now()).toSeconds();
        }

        public int getRequestCount() {
            return requestCount.get();
        }

        public PerformanceTest getAnnotation() {
            return annotation;
        }
    }

    /**
     * Performance test result class
     */
    public static class PerformanceResult {
        private PerformanceMetrics metrics;
        private boolean passed;
        private java.util.List<String> violations = new java.util.ArrayList<>();

        public void addViolation(String violation) {
            violations.add(violation);
        }

        // Getters and setters
        public PerformanceMetrics getMetrics() {
            return metrics;
        }

        public void setMetrics(PerformanceMetrics metrics) {
            this.metrics = metrics;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public java.util.List<String> getViolations() {
            return violations;
        }

        public String getViolationsAsString() {
            return String.join("; ", violations);
        }
    }
}
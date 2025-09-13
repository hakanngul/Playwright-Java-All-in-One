package com.starlettech.core.managers;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Resource Cleanup Manager for preventing memory leaks and zombie processes
 */
public class ResourceCleanupManager {
    private static final Logger logger = LogManager.getLogger(ResourceCleanupManager.class);
    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private static final AtomicBoolean isShutdownHookRegistered = new AtomicBoolean(false);
    private static ScheduledExecutorService cleanupScheduler;
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    // Memory thresholds
    private static final double MEMORY_WARNING_THRESHOLD = 0.8; // 80%
    private static final double MEMORY_CRITICAL_THRESHOLD = 0.9; // 90%
    private static final long CLEANUP_INTERVAL_SECONDS = 30;

    /**
     * Initialize the cleanup manager
     */
    public static synchronized void initialize() {
        if (isInitialized.get()) {
            logger.debug("ResourceCleanupManager already initialized");
            return;
        }

        logger.info("Initializing ResourceCleanupManager");

        // Register shutdown hook
        registerShutdownHook();

        // Start periodic cleanup
        startPeriodicCleanup();

        // Start memory monitoring
        startMemoryMonitoring();

        isInitialized.set(true);
        logger.info("ResourceCleanupManager initialized successfully");
    }

    /**
     * Register JVM shutdown hook
     */
    private static void registerShutdownHook() {
        if (isShutdownHookRegistered.get()) {
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("JVM shutdown detected. Starting emergency cleanup...");
            emergencyCleanup();
        }, "ResourceCleanupShutdownHook"));

        isShutdownHookRegistered.set(true);
        logger.debug("Shutdown hook registered");
    }

    /**
     * Start periodic cleanup task
     */
    private static void startPeriodicCleanup() {
        cleanupScheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "ResourceCleanupScheduler");
            t.setDaemon(true);
            return t;
        });

        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                performPeriodicCleanup();
            } catch (Exception e) {
                logger.error("Error during periodic cleanup: {}", e.getMessage(), e);
            }
        }, CLEANUP_INTERVAL_SECONDS, CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);

        logger.debug("Periodic cleanup scheduled every {} seconds", CLEANUP_INTERVAL_SECONDS);
    }

    /**
     * Start memory monitoring
     */
    private static void startMemoryMonitoring() {
        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                monitorMemoryUsage();
            } catch (Exception e) {
                logger.error("Error during memory monitoring: {}", e.getMessage(), e);
            }
        }, 10, 10, TimeUnit.SECONDS);

        logger.debug("Memory monitoring started");
    }

    /**
     * Perform periodic cleanup
     */
    private static void performPeriodicCleanup() {
        logger.debug("Performing periodic cleanup");

        // Check for orphaned threads
        int activeThreads = ThreadLocalManager.getActiveThreadCount();
        if (activeThreads > 0) {
            logger.debug("Found {} active threads", activeThreads);
            
            // Log active threads for debugging
            ThreadLocalManager.getActiveThreads().forEach((threadId, threadInfo) -> {
                logger.debug("Active thread: {} - {}", threadId, threadInfo);
            });
        }

        // Force garbage collection if memory usage is high
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        double memoryUsagePercent = (double) heapUsage.getUsed() / heapUsage.getMax();
        
        if (memoryUsagePercent > MEMORY_WARNING_THRESHOLD) {
            logger.debug("High memory usage detected ({}%). Suggesting garbage collection", 
                String.format("%.1f", memoryUsagePercent * 100));
            System.gc();
        }
    }

    /**
     * Monitor memory usage and take action if needed
     */
    private static void monitorMemoryUsage() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        double heapUsagePercent = (double) heapUsage.getUsed() / heapUsage.getMax();
        double nonHeapUsagePercent = (double) nonHeapUsage.getUsed() / nonHeapUsage.getMax();

        // Log memory usage periodically
        if (logger.isDebugEnabled()) {
            logger.debug("Memory Usage - Heap: {:.1f}% ({} MB / {} MB), Non-Heap: {:.1f}% ({} MB / {} MB)",
                heapUsagePercent * 100, heapUsage.getUsed() / 1024 / 1024, heapUsage.getMax() / 1024 / 1024,
                nonHeapUsagePercent * 100, nonHeapUsage.getUsed() / 1024 / 1024, nonHeapUsage.getMax() / 1024 / 1024);
        }

        // Warning threshold
        if (heapUsagePercent > MEMORY_WARNING_THRESHOLD) {
            logger.warn("High heap memory usage: {:.1f}%", heapUsagePercent * 100);
        }

        // Critical threshold - force cleanup
        if (heapUsagePercent > MEMORY_CRITICAL_THRESHOLD) {
            logger.error("Critical heap memory usage: {:.1f}%. Forcing cleanup!", heapUsagePercent * 100);
            forceCleanup();
        }
    }

    /**
     * Force immediate cleanup
     */
    public static void forceCleanup() {
        logger.warn("Forcing immediate resource cleanup");

        try {
            // Cleanup all thread-local resources
            ThreadLocalManager.cleanupAllThreads();

            // Force garbage collection
            System.gc();
            // Wait a bit for GC to complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.gc();

            logger.info("Force cleanup completed");
        } catch (Exception e) {
            logger.error("Error during force cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Emergency cleanup during shutdown
     */
    private static void emergencyCleanup() {
        try {
            logger.info("Starting emergency cleanup");

            // Stop scheduler
            if (cleanupScheduler != null && !cleanupScheduler.isShutdown()) {
                cleanupScheduler.shutdown();
                try {
                    if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        cleanupScheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    cleanupScheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            // Cleanup all resources
            ThreadLocalManager.cleanupAllThreads();

            // Final garbage collection
            System.gc();

            logger.info("Emergency cleanup completed");
        } catch (Exception e) {
            logger.error("Error during emergency cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Shutdown the cleanup manager
     */
    public static synchronized void shutdown() {
        if (!isInitialized.get()) {
            return;
        }

        logger.info("Shutting down ResourceCleanupManager");

        try {
            if (cleanupScheduler != null && !cleanupScheduler.isShutdown()) {
                cleanupScheduler.shutdown();
                if (!cleanupScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    logger.warn("Cleanup scheduler did not terminate gracefully, forcing shutdown");
                    cleanupScheduler.shutdownNow();
                }
            }

            // Final cleanup
            ThreadLocalManager.cleanupAllThreads();

            isInitialized.set(false);
            logger.info("ResourceCleanupManager shutdown completed");
        } catch (Exception e) {
            logger.error("Error during ResourceCleanupManager shutdown: {}", e.getMessage(), e);
        }
    }

    /**
     * Get current memory usage information
     */
    public static MemoryInfo getMemoryInfo() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        return new MemoryInfo(heapUsage, nonHeapUsage);
    }

    /**
     * Check if cleanup manager is initialized
     */
    public static boolean isInitialized() {
        return isInitialized.get();
    }

    /**
     * Memory information holder
     */
    public static class MemoryInfo {
        private final MemoryUsage heapUsage;
        private final MemoryUsage nonHeapUsage;

        public MemoryInfo(MemoryUsage heapUsage, MemoryUsage nonHeapUsage) {
            this.heapUsage = heapUsage;
            this.nonHeapUsage = nonHeapUsage;
        }

        public double getHeapUsagePercent() {
            return (double) heapUsage.getUsed() / heapUsage.getMax();
        }

        public double getNonHeapUsagePercent() {
            return (double) nonHeapUsage.getUsed() / nonHeapUsage.getMax();
        }

        public long getHeapUsedMB() {
            return heapUsage.getUsed() / 1024 / 1024;
        }

        public long getHeapMaxMB() {
            return heapUsage.getMax() / 1024 / 1024;
        }

        public long getNonHeapUsedMB() {
            return nonHeapUsage.getUsed() / 1024 / 1024;
        }

        public long getNonHeapMaxMB() {
            return nonHeapUsage.getMax() / 1024 / 1024;
        }

        @Override
        public String toString() {
            return String.format("Heap: %.1f%% (%d/%d MB), Non-Heap: %.1f%% (%d/%d MB)",
                getHeapUsagePercent() * 100, getHeapUsedMB(), getHeapMaxMB(),
                getNonHeapUsagePercent() * 100, getNonHeapUsedMB(), getNonHeapMaxMB());
        }
    }
}

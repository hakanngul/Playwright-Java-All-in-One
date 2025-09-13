package com.starlettech.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for performance testing requirements
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PerformanceTest {
    /**
     * Maximum acceptable response time in milliseconds
     */
    long maxResponseTime() default 5000;
    
    /**
     * Number of concurrent users/threads
     */
    int concurrentUsers() default 1;
    
    /**
     * Test duration in seconds
     */
    int duration() default 60;
    
    /**
     * CPU usage threshold (percentage)
     */
    double maxCpuUsage() default 80.0;
    
    /**
     * Memory usage threshold (MB)
     */
    long maxMemoryUsage() default 512;
    
    /**
     * Expected throughput (requests per second)
     */
    double expectedThroughput() default 0.0;
    
    /**
     * Performance test type
     */
    PerformanceType type() default PerformanceType.LOAD;
    
    enum PerformanceType {
        LOAD, STRESS, SPIKE, VOLUME, ENDURANCE
    }
}
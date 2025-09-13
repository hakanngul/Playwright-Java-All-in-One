package com.starlettech.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for retry configuration
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
    /**
     * Maximum number of retry attempts
     */
    int maxAttempts() default 3;
    
    /**
     * Delay between retries in milliseconds
     */
    long delay() default 1000;
    
    /**
     * Exponential backoff multiplier
     */
    double backoffMultiplier() default 1.5;
    
    /**
     * Maximum delay between retries
     */
    long maxDelay() default 30000;
    
    /**
     * Specific exceptions that should trigger retry
     */
    Class<? extends Throwable>[] retryOn() default {};
    
    /**
     * Exceptions that should NOT trigger retry
     */
    Class<? extends Throwable>[] abortOn() default {};
    
    /**
     * Whether to retry on any exception
     */
    boolean retryOnAnyException() default true;
}
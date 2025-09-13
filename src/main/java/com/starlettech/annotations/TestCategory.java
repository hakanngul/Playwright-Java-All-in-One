package com.starlettech.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to categorize tests by type and level
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestCategory {
    /**
     * Test category
     */
    Category value() default Category.FUNCTIONAL;
    
    /**
     * Test level
     */
    Level level() default Level.INTEGRATION;
    
    /**
     * Test environment requirements
     */
    String[] environments() default {"DEV", "TEST"};
    
    /**
     * Risk level of the test
     */
    Risk riskLevel() default Risk.MEDIUM;
    
    /**
     * Whether test is flaky
     */
    boolean isFlaky() default false;
    
    enum Category {
        SMOKE, SANITY, REGRESSION, FUNCTIONAL, SECURITY, 
        PERFORMANCE, ACCESSIBILITY, USABILITY, COMPATIBILITY
    }
    
    enum Level {
        UNIT, COMPONENT, INTEGRATION, SYSTEM, E2E
    }
    
    enum Risk {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
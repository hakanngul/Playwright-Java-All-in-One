package com.starlettech.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.starlettech.enums.TestPriority;

/**
 * Annotation to provide additional test information for both ReportPortal and Allure reporting
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestInfo {
    String description() default "";
    String author() default "";
    String[] tags() default {};
    TestPriority priority() default TestPriority.MEDIUM;
    String jiraId() default "";
    String[] requirements() default {};
    String estimatedDuration() default "";
    boolean isBlocking() default false;
    
    // Allure-specific fields
    String epic() default "";
    String feature() default "";
    String story() default "";
}

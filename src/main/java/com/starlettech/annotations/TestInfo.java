package com.starlettech.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.starlettech.enums.TestPriority;

/**
 * Annotation to provide additional test information
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestInfo {
    String MEDIUM = null;

    String description() default "";
    String author() default "";
    String[] tags() default {};
    TestPriority priority() default TestPriority.MEDIUM;
    String jiraId() default "";
    String[] requirements() default {};
    String estimatedDuration() default "";
    boolean isBlocking() default false;
    
}

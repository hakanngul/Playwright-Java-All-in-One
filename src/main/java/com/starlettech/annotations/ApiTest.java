package com.starlettech.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark API test methods
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiTest {
    String endpoint() default "";
    String method() default "GET";
    boolean requiresAuth() default false;
    String description() default "";
    String author() default "";
    String[] tags() default {};
    String priority() default "MEDIUM";
    String jiraId() default "";
}

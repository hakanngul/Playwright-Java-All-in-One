package com.starlettech.annotations;

import com.starlettech.enums.BrowserType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify browser type for test methods
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Browser {
    BrowserType value() default BrowserType.CHROMIUM;
    boolean headless() default true;
    String[] args() default {};
}

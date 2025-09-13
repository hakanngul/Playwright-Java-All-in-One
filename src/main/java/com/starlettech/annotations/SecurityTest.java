package com.starlettech.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for security testing configuration
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityTest {
    /**
     * Security test types to perform
     */
    SecurityType[] types() default {SecurityType.AUTHENTICATION};
    
    /**
     * OWASP Top 10 categories to test
     */
    OwaspCategory[] owaspCategories() default {};
    
    /**
     * Required user roles for testing
     */
    String[] requiredRoles() default {};
    
    /**
     * Sensitive data handling required
     */
    boolean sensitiveData() default false;
    
    /**
     * Expected security level
     */
    SecurityLevel level() default SecurityLevel.MEDIUM;
    
    enum SecurityType {
        AUTHENTICATION, AUTHORIZATION, INPUT_VALIDATION, 
        SQL_INJECTION, XSS, CSRF, SESSION_MANAGEMENT,
        ENCRYPTION, ACCESS_CONTROL
    }
    
    enum OwaspCategory {
        A01_BROKEN_ACCESS_CONTROL,
        A02_CRYPTOGRAPHIC_FAILURES,
        A03_INJECTION,
        A04_INSECURE_DESIGN,
        A05_SECURITY_MISCONFIGURATION,
        A06_VULNERABLE_COMPONENTS,
        A07_IDENTIFICATION_FAILURES,
        A08_SOFTWARE_INTEGRITY_FAILURES,
        A09_SECURITY_LOGGING_FAILURES,
        A10_SERVER_SIDE_REQUEST_FORGERY
    }
    
    enum SecurityLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
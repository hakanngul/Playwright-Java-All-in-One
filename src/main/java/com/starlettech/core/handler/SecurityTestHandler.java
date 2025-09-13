package com.starlettech.core.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.starlettech.annotations.SecurityTest;

/**
 * Handler for security test validation and enforcement
 */
public class SecurityTestHandler {
    private static final Logger logger = LogManager.getLogger(SecurityTestHandler.class);
    
    // Common security patterns for validation
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror)"
    );
    
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)(<script|javascript:|vbscript:|onload=|onerror=|onclick=|onmouseover=|<iframe|<object|<embed)"
    );
    
    private static final Pattern SENSITIVE_DATA_PATTERN = Pattern.compile(
        "(?i)(password|ssn|social.security|credit.card|cvv|pin|secret|key|token|api.key)"
    );

    /**
     * Validate security requirements before test execution
     */
    public static SecurityValidationResult validateSecurityRequirements(Method testMethod) {
        SecurityTest annotation = getSecurityAnnotation(testMethod);
        if (annotation == null) {
            return null;
        }

        SecurityValidationResult result = new SecurityValidationResult();
        String testKey = testMethod.getDeclaringClass().getSimpleName() + "." + testMethod.getName();

        logger.info("Starting security validation for test: {} with types: {}", 
                   testKey, Arrays.toString(annotation.types()));

        // Validate required roles
        if (annotation.requiredRoles().length > 0) {
            result.addRequirement("Required roles: " + Arrays.toString(annotation.requiredRoles()));
        }

        // Validate sensitive data handling
        if (annotation.sensitiveData()) {
            result.addRequirement("Sensitive data handling required - ensure encryption and secure storage");
        }

        // Validate security level
        result.addRequirement("Security level: " + annotation.level().name());

        // Validate OWASP categories
        if (annotation.owaspCategories().length > 0) {
            result.addRequirement("OWASP categories to test: " + Arrays.toString(annotation.owaspCategories()));
        }

        result.setSecurityTest(annotation);
        return result;
    }

    /**
     * Validate input for security vulnerabilities
     */
    public static SecurityInputValidation validateInput(String input, SecurityTest.SecurityType[] types) {
        SecurityInputValidation validation = new SecurityInputValidation();
        
        if (input == null || input.isEmpty()) {
            return validation;
        }

        for (SecurityTest.SecurityType type : types) {
            switch (type) {
                case SQL_INJECTION -> {
                    if (SQL_INJECTION_PATTERN.matcher(input).find()) {
                        validation.addViolation("Potential SQL injection detected in input: " + input);
                    }
                }
                case XSS -> {
                    if (XSS_PATTERN.matcher(input).find()) {
                        validation.addViolation("Potential XSS vulnerability detected in input: " + input);
                    }
                }
                case INPUT_VALIDATION -> validateInputFormat(input, validation);
                default -> {
                }
            }
        }

        return validation;
    }

    /**
     * Validate response for sensitive data exposure
     */
    public static SecurityResponseValidation validateResponse(String response, SecurityTest annotation) {
        SecurityResponseValidation validation = new SecurityResponseValidation();
        
        if (response == null || response.isEmpty()) {
            return validation;
        }

        // Check for sensitive data exposure
        if (annotation.sensitiveData()) {
            if (SENSITIVE_DATA_PATTERN.matcher(response).find()) {
                validation.addViolation("Potential sensitive data exposure detected in response");
            }
        }

        // Check for information disclosure
        if (response.toLowerCase().contains("error") || response.toLowerCase().contains("exception")) {
            validation.addWarning("Potential information disclosure through error messages");
        }

        // Check for session information
        if (response.toLowerCase().contains("sessionid") || response.toLowerCase().contains("jsessionid")) {
            validation.addWarning("Session information might be exposed in response");
        }

        return validation;
    }

    /**
     * Validate authentication and authorization
     */
    public static SecurityAuthValidation validateAuthentication(String userRole, String[] requiredRoles, String token) {
        SecurityAuthValidation validation = new SecurityAuthValidation();

        // Validate user role
        if (requiredRoles.length > 0) {
            boolean hasRequiredRole = Arrays.asList(requiredRoles).contains(userRole);
            if (!hasRequiredRole) {
                validation.addViolation("User role '" + userRole + "' does not have required permissions: " + Arrays.toString(requiredRoles));
            }
        }

        // Validate token format (basic validation)
        if (token != null && !token.isEmpty()) {
            if (token.length() < 10) {
                validation.addViolation("Authentication token appears to be too short for security");
            }
            if (!token.contains(".") && !token.startsWith("Bearer")) {
                validation.addWarning("Token format might not follow standard patterns");
            }
        }

        return validation;
    }

    /**
     * Get security annotation from method or class
     */
    private static SecurityTest getSecurityAnnotation(Method method) {
        SecurityTest methodAnnotation = method.getAnnotation(SecurityTest.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return method.getDeclaringClass().getAnnotation(SecurityTest.class);
    }

    /**
     * Validate input format and structure
     */
    private static void validateInputFormat(String input, SecurityInputValidation validation) {
        // Check for excessively long input (potential buffer overflow)
        if (input.length() > 10000) {
            validation.addViolation("Input length exceeds safe limits: " + input.length() + " characters");
        }

        // Check for null bytes
        if (input.contains("\0")) {
            validation.addViolation("Input contains null bytes - potential security risk");
        }

        // Check for path traversal
        if (input.contains("../") || input.contains("..\\")) {
            validation.addViolation("Potential path traversal attack detected");
        }

        // Check for command injection
        if (input.contains(";") || input.contains("|") || input.contains("&")) {
            validation.addWarning("Input contains command separators - review for command injection risk");
        }
    }

    /**
     * Security validation result class
     */
    public static class SecurityValidationResult {
        private SecurityTest securityTest;
        private List<String> requirements = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public void addRequirement(String requirement) {
            requirements.add(requirement);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        // Getters and setters
        public SecurityTest getSecurityTest() {
            return securityTest;
        }

        public void setSecurityTest(SecurityTest securityTest) {
            this.securityTest = securityTest;
        }

        public List<String> getRequirements() {
            return requirements;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public boolean hasRequirements() {
            return !requirements.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }

    /**
     * Security input validation result class
     */
    public static class SecurityInputValidation {
        private List<String> violations = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public void addViolation(String violation) {
            violations.add(violation);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public List<String> getViolations() {
            return violations;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public boolean hasViolations() {
            return !violations.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public boolean isValid() {
            return violations.isEmpty();
        }
    }

    /**
     * Security response validation result class
     */
    public static class SecurityResponseValidation {
        private List<String> violations = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public void addViolation(String violation) {
            violations.add(violation);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public List<String> getViolations() {
            return violations;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public boolean hasViolations() {
            return !violations.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public boolean isSecure() {
            return violations.isEmpty();
        }
    }

    /**
     * Security authentication validation result class
     */
    public static class SecurityAuthValidation {
        private List<String> violations = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public void addViolation(String violation) {
            violations.add(violation);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public List<String> getViolations() {
            return violations;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public boolean hasViolations() {
            return !violations.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public boolean isAuthorized() {
            return violations.isEmpty();
        }
    }
}
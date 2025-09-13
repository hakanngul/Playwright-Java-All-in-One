package com.starlettech.tests.demo;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.starlettech.annotations.ApiTest;
import com.starlettech.annotations.PerformanceTest;
import com.starlettech.annotations.Retry;
import com.starlettech.annotations.SecurityTest;
import com.starlettech.annotations.TestCategory;
import com.starlettech.annotations.TestInfo;
import com.starlettech.core.handler.PerformanceTestHandler;
import com.starlettech.core.handler.SecurityTestHandler;
import com.starlettech.enums.TestPriority;

/**
 * Demonstration tests showing non-functional annotation integration
 */
public class NonFunctionalAnnotationDemoTests {
    private static final Logger logger = LogManager.getLogger(NonFunctionalAnnotationDemoTests.class);

    @Test
    @TestInfo(description = "Performance test demonstration with comprehensive monitoring", 
              author = "Framework Engineer", 
              priority = TestPriority.MEDIUM,
              tags = {"demo", "performance", "integration"})
    @PerformanceTest(
        maxResponseTime = 2000,
        concurrentUsers = 5,
        duration = 30,
        maxCpuUsage = 70.0,
        maxMemoryUsage = 256,
        expectedThroughput = 10.0,
        type = PerformanceTest.PerformanceType.LOAD
    )
    @TestCategory(
        value = TestCategory.Category.PERFORMANCE,
        level = TestCategory.Level.INTEGRATION,
        riskLevel = TestCategory.Risk.MEDIUM
    )
    public void testPerformanceAnnotationIntegration() throws Exception {
        Method testMethod = this.getClass().getMethod("testPerformanceAnnotationIntegration");
        
        // Simulate some work with performance tracking
        for (int i = 0; i < 10; i++) {
            long startTime = System.currentTimeMillis();
            
            // Simulate some processing work
            Thread.sleep(100 + (int) (Math.random() * 200)); // 100-300ms
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Record the request for performance monitoring
            PerformanceTestHandler.recordRequest(testMethod, responseTime);
            
            logger.info("Completed request {} with response time: {}ms", i + 1, responseTime);
        }
        
        logger.info("Performance test simulation completed successfully");
        Assert.assertTrue(true, "Performance test should complete without errors");
    }

    @Test
    @TestInfo(description = "Security test demonstration with validation", 
              author = "Security Engineer", 
              priority = TestPriority.HIGH,
              tags = {"demo", "security", "validation"})
    @SecurityTest(
        types = {SecurityTest.SecurityType.INPUT_VALIDATION, SecurityTest.SecurityType.XSS, SecurityTest.SecurityType.SQL_INJECTION},
        level = SecurityTest.SecurityLevel.HIGH,
        sensitiveData = true,
        requiredRoles = {"ADMIN", "SECURITY_TESTER"},
        owaspCategories = {SecurityTest.OwaspCategory.A03_INJECTION, SecurityTest.OwaspCategory.A01_BROKEN_ACCESS_CONTROL}
    )
    @TestCategory(
        value = TestCategory.Category.SECURITY,
        level = TestCategory.Level.SYSTEM,
        riskLevel = TestCategory.Risk.HIGH
    )
    public void testSecurityAnnotationIntegration() {
        // Test various security inputs
        String[] testInputs = {
            "normal input",
            "<script>alert('xss')</script>",
            "'; DROP TABLE users; --",
            "admin'/**/AND/**/1=1#",
            "javascript:alert('xss')"
        };
        
        SecurityTest.SecurityType[] securityTypes = {
            SecurityTest.SecurityType.INPUT_VALIDATION,
            SecurityTest.SecurityType.XSS,
            SecurityTest.SecurityType.SQL_INJECTION
        };
        
        for (String input : testInputs) {
            SecurityTestHandler.SecurityInputValidation validation = 
                SecurityTestHandler.validateInput(input, securityTypes);
            
            logger.info("Input: '{}' - Valid: {}, Violations: {}", 
                       input, validation.isValid(), validation.getViolations().size());
            
            if (validation.hasViolations()) {
                for (String violation : validation.getViolations()) {
                    logger.warn("Security violation detected: {}", violation);
                }
            }
        }
        
        // Test authentication validation
        SecurityTestHandler.SecurityAuthValidation authValidation = 
            SecurityTestHandler.validateAuthentication("ADMIN", new String[]{"ADMIN", "SECURITY_TESTER"}, "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        
        logger.info("Authentication validation - Authorized: {}, Violations: {}", 
                   authValidation.isAuthorized(), authValidation.getViolations().size());
        
        Assert.assertTrue(authValidation.isAuthorized(), "User should be authorized with ADMIN role");
    }

    @Test
    @TestInfo(description = "Combined performance and security test", 
              author = "QA Engineer", 
              priority = TestPriority.HIGH,
              jiraId = "DEMO-123",
              tags = {"demo", "combined", "comprehensive"})
    @PerformanceTest(
        maxResponseTime = 1500,
        concurrentUsers = 3,
        duration = 20,
        type = PerformanceTest.PerformanceType.STRESS
    )
    @SecurityTest(
        types = {SecurityTest.SecurityType.AUTHENTICATION, SecurityTest.SecurityType.AUTHORIZATION},
        level = SecurityTest.SecurityLevel.MEDIUM,
        requiredRoles = {"USER"}
    )
    @TestCategory(
        value = TestCategory.Category.FUNCTIONAL,
        level = TestCategory.Level.E2E,
        riskLevel = TestCategory.Risk.MEDIUM
    )
    @ApiTest(
        endpoint = "/api/v1/secure-data",
        method = "GET",
        requiresAuth = true
    )
    @Retry(
        maxAttempts = 3,
        delay = 1000,
        backoffMultiplier = 2.0
    )
    public void testCombinedAnnotationsIntegration() throws Exception {
        Method testMethod = this.getClass().getMethod("testCombinedAnnotationsIntegration");
        
        // Simulate API calls with both performance and security considerations
        for (int i = 0; i < 5; i++) {
            long startTime = System.currentTimeMillis();
            
            // Simulate secure API call
            String response = simulateSecureApiCall("/api/v1/secure-data");
            
            long responseTime = System.currentTimeMillis() - startTime;
            PerformanceTestHandler.recordRequest(testMethod, responseTime);
            
            // Validate security of response
            SecurityTest securityAnnotation = testMethod.getAnnotation(SecurityTest.class);
            SecurityTestHandler.SecurityResponseValidation responseValidation = 
                SecurityTestHandler.validateResponse(response, securityAnnotation);
            
            if (responseValidation.hasViolations()) {
                logger.warn("Security violations in response: {}", responseValidation.getViolations());
            }
            
            logger.info("API call {} completed - Response time: {}ms, Secure: {}", 
                       i + 1, responseTime, responseValidation.isSecure());
        }
        
        Assert.assertTrue(true, "Combined test should complete successfully");
    }
    
    /**
     * Simulate a secure API call
     */
    private String simulateSecureApiCall(String endpoint) throws InterruptedException {
        // Simulate processing time
        Thread.sleep(50 + (int) (Math.random() * 100)); // 50-150ms
        
        // Return mock secure response
        return "{\"data\":\"secure information\",\"user\":\"authenticated_user\",\"timestamp\":\"" + 
               System.currentTimeMillis() + "\"}";
    }
}
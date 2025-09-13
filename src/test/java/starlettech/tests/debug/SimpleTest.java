package starlettech.tests.debug;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.starlettech.annotations.TestInfo;
import com.starlettech.enums.TestPriority;

/**
 * Simple test without ReportPortal listener
 */
public class SimpleTest {

    @Test(groups = {"simple", "smoke"})
    @TestInfo(description = "Basic test without ReportPortal", author = "Test Engineer", priority = TestPriority.HIGH)
    public void testBasicFunctionality() {
        System.out.println("=== Simple Test Running ===");
        Assert.assertTrue(true, "Basic test should pass");
    }
    
    @Test(groups = {"simple", "smoke"})
    @TestInfo(description = "Another basic test", author = "Test Engineer", priority = TestPriority.HIGH) 
    public void testAnotherBasicFunctionality() {
        System.out.println("=== Another Simple Test Running ===");
        Assert.assertTrue(true, "Another basic test should pass");
    }
}
package starlettech.tests.debug;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.starlettech.listeners.ReportPortalListener;

/**
 * Simple test to verify ReportPortal integration
 */
public class SimpleReportPortalTest {

    @Test
    public void testReportPortalBasicLogging() {
        ReportPortalListener.logInfo("Starting basic ReportPortal test");
        
        ReportPortalListener.logInfo("Performing simple assertion");
        Assert.assertTrue(true, "This should always pass");
        
        ReportPortalListener.logInfo("Test completed successfully");
    }

    @Test(enabled = false) // Disabled for now to avoid build failures
    public void testReportPortalWithFailure() {
        ReportPortalListener.logInfo("Starting test that will fail");
        
        ReportPortalListener.logWarning("This test is designed to fail for demonstration");
        
        try {
            Assert.assertTrue(false, "This test intentionally fails");
        } catch (AssertionError e) {
            ReportPortalListener.logError("Test failed as expected: " + e.getMessage());
            throw e;
        }
    }
}
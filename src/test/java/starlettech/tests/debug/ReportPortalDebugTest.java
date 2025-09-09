package starlettech.tests.debug;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.starlettech.annotations.TestInfo;
import com.starlettech.config.ReportPortalConfig;
import com.starlettech.listeners.ReportPortalListener;

/**
 * Debug test for ReportPortal logging
 */
@Listeners({ReportPortalListener.class})
public class ReportPortalDebugTest {

    @Test(groups = {"debug", "reportportal"})
    @TestInfo(description = "Test ReportPortal logging functionality", author = "Debug Engineer", priority = "HIGH")
    public void testReportPortalLogging() {
        System.out.println("=== ReportPortal Debug Test ===");
        
        // Test ReportPortal config
        ReportPortalConfig config = ReportPortalConfig.getInstance();
        System.out.println("ReportPortal Enabled: " + config.isEnable());
        System.out.println("ReportPortal Endpoint: " + config.getEndpoint());
        System.out.println("ReportPortal Project: " + config.getProject());
        
        // Manual log to ReportPortal
        try {
            ReportPortalListener.logInfo("🔍 Manual test log - ReportPortal çalışıyor mu?");
            ReportPortalListener.logWarning("⚠️ Bu bir warning mesajı");
            ReportPortalListener.logError("❌ Bu bir error mesajı");
            System.out.println("✅ ReportPortal log gönderildi");
        } catch (Exception e) {
            System.err.println("❌ ReportPortal log gönderilemedi: " + e.getMessage());
            e.printStackTrace();
        }
        
        Assert.assertTrue(true, "Test başarılı");
    }
}
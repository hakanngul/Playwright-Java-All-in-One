# Playwright Test Otomasyon Framework'ü - Hızlı Referans

## 📋 İçindekiler
- [Temel Komutlar](#temel-komutlar)
- [Test Yazma Şablonları](#test-yazma-şablonları)
- [Konfigürasyon Örnekleri](#konfigürasyon-örnekleri)
- [Sık Kullanılan Code Snippet'ler](#sık-kullanılan-code-snippetler)
- [Troubleshooting Checklist](#troubleshooting-checklist)
- [Performans İpuçları](#performans-ipuçları)

## Temel Komutlar

### Maven Komutları
```bash
# Temel test çalıştırma
mvn test                                    # Tüm testleri çalıştır
mvn clean test                              # Temizleyip testleri çalıştır
mvn test -DskipTests                        # Testleri atla

# Test grupları
mvn test -Dgroups=smoke                     # Smoke testleri
mvn test -Dgroups=regression                # Regression testleri
mvn test -Dgroups="smoke,api"               # Birden fazla grup

# Belirli testler
mvn test -Dtest=LoginTests                  # Belirli sınıf
mvn test -Dtest=LoginTests#testValidLogin   # Belirli method
mvn test -Dtest="*Login*"                   # Pattern matching

# Suite dosyaları
mvn test -DsuiteXmlFile=src/test/resources/suites/ui-smoke.xml
mvn test -DsuiteXmlFile=src/test/resources/suites/api-smoke.xml
mvn test -DsuiteXmlFile=src/test/resources/suites/regression.xml

# Environment ayarları
mvn test -Denvironment=TEST                 # Test ortamı
mvn test -Denvironment=STAGING              # Staging ortamı
mvn test -Dbase.url=https://test.example.com

# Browser ayarları
mvn test -Dbrowser.type=firefox             # Firefox kullan
mvn test -Dbrowser.headless=false           # Headed mode
mvn test -Dbrowser.slowmo=1000              # Slow motion (debug)

# Paralel yürütme
mvn test -Dparallel.execution=true          # Paralel aktif
mvn test -Dthread.count=3                   # Thread sayısı

# Raporlama
mvn test -Dscreenshot.on.failure=true       # Screenshot aktif
mvn test -Dvideo.recording=true             # Video kayıt
mvn test -Dreportportal.enable=true         # ReportPortal aktif
```

### Docker Komutları
```bash
# Docker Compose
docker-compose up --build                   # Build ve çalıştır
docker-compose down                         # Durdur ve temizle
docker-compose run tests mvn test           # Sadece testleri çalıştır

# Docker run
docker run --rm -v $(pwd)/reports:/app/reports playwright-tests mvn test
docker run --rm -e BROWSER_HEADLESS=true playwright-tests mvn test -Dgroups=smoke
```

### Playwright CLI
```bash
# Browser kurulumu
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --force"

# System dependencies (Linux)
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install-deps"
```

## Test Yazma Şablonları

### UI Test Şablonu
```java
package starlettech.tests.ui;

import com.starlettech.core.BaseTest;
import com.starlettech.annotations.TestInfo;
import starlettech.pages.LoginPage;
import starlettech.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExampleUITest extends BaseTest {
    
    @Test(groups = {"smoke", "ui"}, priority = 1)
    @TestInfo(description = "Test açıklaması", 
              author = "Test Engineer", 
              priority = "HIGH")
    public void testExample() {
        // Arrange - Test verilerini hazırla
        LoginPage loginPage = new LoginPage();
        HomePage homePage = new HomePage();
        
        // Act - Test adımlarını gerçekleştir
        loginPage.navigateToLoginPage();
        loginPage.login("testuser", "password");
        
        // Assert - Sonuçları doğrula
        Assert.assertTrue(homePage.isUserLoggedIn(), 
                         "Kullanıcı giriş yapmış olmalı");
        
        logger.info("Test başarıyla tamamlandı");
    }
}
```

### API Test Şablonu
```java
package starlettech.tests.api;

import com.starlettech.core.BaseApiTest;
import com.starlettech.annotations.TestInfo;
import starlettech.api.clients.UserApiClient;
import starlettech.api.models.User;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExampleApiTest extends BaseApiTest {
    
    private UserApiClient userApiClient;
    
    @BeforeMethod
    public void setUp() {
        userApiClient = new UserApiClient();
        // Authentication gerekirse
        loginWithTestUser();
    }
    
    @Test(groups = {"smoke", "api"}, priority = 1)
    @TestInfo(description = "API test açıklaması", 
              author = "API Test Engineer", 
              priority = "HIGH")
    public void testApiExample() {
        // Arrange
        User testUser = TestDataFactory.createValidUser();
        
        // Act
        User createdUser = userApiClient.createUser(testUser);
        
        // Assert
        Assert.assertNotNull(createdUser.getId(), 
                           "Kullanıcı ID'si atanmalı");
        Assert.assertEquals(createdUser.getUsername(), 
                          testUser.getUsername());
        
        logger.info("API test başarıyla tamamlandı");
    }
}
```

### Page Object Şablonu
```java
package starlettech.pages;

import com.starlettech.core.BasePage;

public class ExamplePage extends BasePage {
    
    // Locator'lar - private static final
    private static final String MAIN_CONTAINER = "#main-container";
    private static final String SUBMIT_BUTTON = "[data-testid='submit']";
    private static final String ERROR_MESSAGE = ".error-message";
    
    // Navigation method'ları
    public void navigateToPage() {
        navigateTo(testConfig.getBaseUrl() + "/example");
        waitForElement(MAIN_CONTAINER);
        logger.info("Example sayfasına yönlendirildi");
    }
    
    // Action method'ları
    public void clickSubmit() {
        logger.info("Submit butonuna tıklanıyor");
        click(SUBMIT_BUTTON);
    }
    
    // Verification method'ları
    public boolean isPageLoaded() {
        return isVisible(MAIN_CONTAINER);
    }
    
    public String getErrorMessage() {
        waitForElement(ERROR_MESSAGE);
        return getText(ERROR_MESSAGE);
    }
    
    public boolean isErrorDisplayed() {
        return isVisible(ERROR_MESSAGE);
    }
}
```

### API Client Şablonu
```java
package starlettech.api.clients;

import com.microsoft.playwright.APIResponse;
import starlettech.api.models.ExampleModel;
import java.util.List;

public class ExampleApiClient extends BaseApiClient {
    
    private static final String BASE_ENDPOINT = "/api/examples";
    
    public List<ExampleModel> getAll() {
        logger.info("Tüm örnekler getiriliyor");
        
        APIResponse response = makeRequest("GET", BASE_ENDPOINT, null);
        validateResponse(response, 200);
        
        List<ExampleModel> examples = jsonUtils.fromJson(response.text(), 
                                                        new TypeReference<List<ExampleModel>>() {});
        
        logger.info("{} örnek getirildi", examples.size());
        return examples;
    }
    
    public ExampleModel getById(Long id) {
        logger.info("Örnek getiriliyor - ID: {}", id);
        
        String endpoint = BASE_ENDPOINT + "/" + id;
        APIResponse response = makeRequest("GET", endpoint, null);
        
        if (response.status() == 404) {
            return null;
        }
        
        validateResponse(response, 200);
        return parseResponse(response, ExampleModel.class);
    }
    
    public ExampleModel create(ExampleModel model) {
        logger.info("Yeni örnek oluşturuluyor");
        
        APIResponse response = makeRequest("POST", BASE_ENDPOINT, model);
        validateResponse(response, 201);
        
        ExampleModel created = parseResponse(response, ExampleModel.class);
        logger.info("Örnek oluşturuldu - ID: {}", created.getId());
        
        return created;
    }
}
```

## Konfigürasyon Örnekleri

### TestNG Suite Örneği
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="Example Test Suite" parallel="methods" thread-count="2">
    
    <listeners>
        <listener class-name="com.starlettech.listeners.TestListener"/>
        <listener class-name="com.starlettech.listeners.ReportPortalListener"/>
    </listeners>
    
    <test name="Smoke Tests">
        <groups>
            <run>
                <include name="smoke"/>
            </run>
        </groups>
        <classes>
            <class name="starlettech.tests.ui.LoginTests"/>
            <class name="starlettech.tests.api.UserApiTests"/>
        </classes>
    </test>
    
</suite>
```

### Properties Konfigürasyon
```properties
# Environment
environment=TEST
base.url=https://test.example.com
api.base.url=https://api-test.example.com

# Browser
browser.type=CHROMIUM
browser.headless=true
browser.timeout=30000
browser.viewport=1920x1080

# Test Data
testdata.path=src/test/resources/testdata

# Reporting
screenshot.on.failure=true
video.recording=false
reportportal.enable=false

# Parallel Execution
parallel.execution=true
thread.count=3
```

### Docker Compose Örneği
```yaml
version: '3.8'

services:
  playwright-tests:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: playwright-framework
    environment:
      - BROWSER_TYPE=${BROWSER_TYPE:-chromium}
      - BROWSER_HEADLESS=${BROWSER_HEADLESS:-true}
      - ENVIRONMENT=${ENVIRONMENT:-test}
    volumes:
      - ./screenshots:/app/screenshots
      - ./videos:/app/videos
      - ./logs:/app/logs
      - ./target/surefire-reports:/app/reports
    networks:
      - test-network

networks:
  test-network:
    driver: bridge
```

## Sık Kullanılan Code Snippet'ler

### Wait Utilities
```java
// Element'i bekle
waitForElement("#submit-button");

// Element'in kaybolmasını bekle
waitForElementToDisappear(".loading-spinner");

// Sayfa yüklenmesini bekle
waitForPageLoad();

// Custom condition ile bekle
page.waitForFunction("() => document.readyState === 'complete'");

// API response bekle
page.waitForResponse(response -> 
    response.url().contains("/api/users") && response.status() == 200);
```

### Element Interactions
```java
// Temel işlemler
click("#button");
type("#input", "test value");
String text = getText("#element");
boolean visible = isVisible("#element");

// Gelişmiş işlemler
selectFromDropdown("#dropdown", "Option 1");
uploadFile("#file-input", "path/to/file.txt");
scrollToElement("#bottom-element");

// Multiple elements
List<String> texts = getAllTexts(".item-list li");
clickElementByText("Click me");
```

### API Request Helpers
```java
// GET request
APIResponse response = ApiRequestManager.authenticatedGet("/api/users");

// POST request
User newUser = new User("username", "email@test.com");
APIResponse response = ApiRequestManager.authenticatedPost("/api/users", newUser);

// Custom headers
Map<String, String> headers = new HashMap<>();
headers.put("Custom-Header", "value");
APIResponse response = ApiUtils.get("/api/endpoint", headers);

// Response validation
Assert.assertEquals(response.status(), 200);
User user = JsonUtils.fromJson(response.text(), User.class);
```

### Test Data Management
```java
// JSON test data
JsonNode userData = testDataReader.getUserData("testuser1");
String username = userData.get("username").asText();

// Properties test data
String baseUrl = testConfig.getProperty("base.url");

// Dynamic test data
User randomUser = TestDataFactory.createRandomUser();
String uniqueEmail = "test_" + System.currentTimeMillis() + "@example.com";
```

## Troubleshooting Checklist

### Test Başarısız Olduğunda
- [ ] Screenshot alındı mı? (`screenshots/` klasörünü kontrol et)
- [ ] Log dosyalarını kontrol et (`logs/` klasörü)
- [ ] Element locator'ı doğru mu?
- [ ] Wait strategy yeterli mi?
- [ ] Test verisi doğru mu?

### Performance Sorunları
- [ ] Paralel thread sayısını azalt
- [ ] Wait timeout'larını optimize et
- [ ] Headless mode kullan
- [ ] Gereksiz screenshot'ları kapat
- [ ] JVM memory ayarlarını kontrol et

### Environment Sorunları
- [ ] Java version (21+) doğru mu?
- [ ] Maven version (3.6+) doğru mu?
- [ ] Playwright browser'ları yüklü mü?
- [ ] Network bağlantısı var mı?
- [ ] Proxy ayarları doğru mu?

## Performans İpuçları

### Test Hızlandırma
```java
// Kısa timeout'lar kullan
page.setDefaultTimeout(10000); // 10 saniye

// DOMCONTENTLOADED kullan
page.navigate(url, new Page.NavigateOptions()
    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

// Gereksiz wait'leri kaldır
// ❌ Thread.sleep(5000);
// ✅ waitForElement("#element");
```

### Memory Optimizasyonu
```bash
# JVM ayarları
export MAVEN_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"

# Browser resource sınırlaması
--disable-dev-shm-usage
--no-sandbox
--disable-gpu
```

### Paralel Yürütme
```xml
<!-- Optimal thread sayısı: CPU core sayısı -->
<suite parallel="methods" thread-count="4">
    <!-- Test isolation için -->
    <test preserve-order="false">
```

---

## 🔗 Hızlı Linkler

- **[Ana Dokümantasyon](index.md)** - Framework genel bakış
- **[Kurulum Rehberi](03-kurulum-konfigurasyon.md)** - Adım adım kurulum
- **[Kullanım Kılavuzu](04-kullanim-kilavuzu.md)** - Test yazma rehberi
- **[API Referansı](06-api-referansi.md)** - Sınıf ve method detayları
- **[Sorun Giderme](07-sorun-giderme.md)** - Yaygın sorunlar
- **[SSS](08-sss-sik-sorulan-sorular.md)** - Sık sorulan sorular

**Bu referans kartını bookmark'layın! 🔖**

# Playwright Test Otomasyon Framework'ü - Kullanım Kılavuzu

## 📋 İçindekiler
- [Test Yazma](#test-yazma)
- [Page Object Kullanımı](#page-object-kullanımı)
- [API Test Yazma](#api-test-yazma)
- [Test Verilerini Yönetme](#test-verilerini-yönetme)
- [Test Çalıştırma](#test-çalıştırma)
- [Raporlama](#raporlama)

## Test Yazma

### UI Test Yazma

#### Temel UI Test Yapısı
```java
package starlettech.tests.ui;

import com.starlettech.core.BaseTest;
import com.starlettech.annotations.TestInfo;
import starlettech.pages.LoginPage;
import starlettech.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTests extends BaseTest {
    
    @Test(groups = {"smoke", "ui", "login"}, priority = 1)
    @TestInfo(description = "Geçerli kullanıcı girişi testi", 
              author = "Test Engineer", 
              priority = "HIGH")
    public void testValidLogin() {
        // Page Object'leri oluştur
        LoginPage loginPage = new LoginPage();
        HomePage homePage = new HomePage();
        
        // Test adımları
        loginPage.navigateToLoginPage();
        Assert.assertTrue(loginPage.isLoginFormDisplayed(), 
                         "Giriş formu görüntülenmeli");
        
        loginPage.login("testuser1", "password123");
        homePage.waitForHomePageToLoad();
        
        // Doğrulamalar
        Assert.assertTrue(homePage.isUserLoggedIn(), 
                         "Kullanıcı giriş yapmış olmalı");
        Assert.assertTrue(homePage.isWelcomeMessageDisplayed(), 
                         "Hoş geldin mesajı görüntülenmeli");
    }
    
    @Test(groups = {"regression", "ui", "login"}, priority = 2)
    @TestInfo(description = "Geçersiz kullanıcı girişi testi", 
              author = "Test Engineer", 
              priority = "HIGH")
    public void testInvalidLogin() {
        LoginPage loginPage = new LoginPage();
        
        loginPage.navigateToLoginPage();
        loginPage.login("invaliduser", "wrongpassword");
        
        // Hata mesajını doğrula
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), 
                         "Hata mesajı görüntülenmeli");
        Assert.assertEquals(loginPage.getErrorMessage(), 
                           "Geçersiz kullanıcı adı veya şifre");
    }
}
```

#### Test Grupları ve Öncelikler
```java
// Test grupları
@Test(groups = {"smoke"})        // Smoke testleri
@Test(groups = {"regression"})   // Regresyon testleri
@Test(groups = {"ui"})          // UI testleri
@Test(groups = {"api"})         // API testleri
@Test(groups = {"hybrid"})      // Hibrit testleri

// Test öncelikleri
@Test(priority = 1)  // Yüksek öncelik
@Test(priority = 2)  // Orta öncelik
@Test(priority = 3)  // Düşük öncelik
```

#### Test Annotations
```java
@TestInfo(
    description = "Test açıklaması",
    author = "Test yazarı",
    priority = "HIGH|MEDIUM|LOW",
    jiraId = "JIRA-123"
)
```

## Page Object Kullanımı

### Page Object Oluşturma

#### 1. Temel Page Object Yapısı
```java
package starlettech.pages;

import com.starlettech.core.BasePage;

public class LoginPage extends BasePage {
    
    // Element locator'ları
    private static final String USERNAME_INPUT = "#username";
    private static final String PASSWORD_INPUT = "#password";
    private static final String LOGIN_BUTTON = "#login-btn";
    private static final String ERROR_MESSAGE = ".error-message";
    private static final String LOGIN_FORM = "#login-form";
    
    // Sayfa işlemleri
    public void navigateToLoginPage() {
        navigateTo(testConfig.getBaseUrl() + "/login");
        waitForElement(LOGIN_FORM);
        logger.info("Giriş sayfasına yönlendirildi");
    }
    
    public void enterUsername(String username) {
        logger.info("Kullanıcı adı giriliyor: {}", username);
        type(USERNAME_INPUT, username);
    }
    
    public void enterPassword(String password) {
        logger.info("Şifre giriliyor");
        type(PASSWORD_INPUT, password);
    }
    
    public void clickLoginButton() {
        logger.info("Giriş butonuna tıklanıyor");
        click(LOGIN_BUTTON);
    }
    
    public void login(String username, String password) {
        logger.info("Giriş yapılıyor: {}", username);
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
    }
    
    // Doğrulama methodları
    public boolean isLoginFormDisplayed() {
        return isVisible(LOGIN_FORM);
    }
    
    public boolean isErrorMessageDisplayed() {
        return isVisible(ERROR_MESSAGE);
    }
    
    public String getErrorMessage() {
        waitForElement(ERROR_MESSAGE);
        return getText(ERROR_MESSAGE);
    }
}
```

#### 2. Gelişmiş Page Object Özellikleri
```java
public class HomePage extends BasePage {
    
    // Dynamic locator'lar
    private String getUserMenuLocator(String username) {
        return String.format("[data-username='%s']", username);
    }
    
    // Conditional wait'ler
    public void waitForUserSpecificContent(String username) {
        waitForElement(getUserMenuLocator(username));
        waitForElement(WELCOME_MESSAGE);
    }
    
    // Complex interactions
    public void selectFromDropdown(String dropdownSelector, String optionText) {
        click(dropdownSelector);
        waitForElement(".dropdown-menu");
        click(String.format("//option[text()='%s']", optionText));
    }
    
    // File upload
    public void uploadFile(String fileInputSelector, String filePath) {
        page.setInputFiles(fileInputSelector, Paths.get(filePath));
        logger.info("Dosya yüklendi: {}", filePath);
    }
}
```

### BasePage Kullanımı

BasePage sınıfı, tüm page object'lerin ortak işlevselliğini sağlar:

```java
// Element işlemleri
click(selector)                    // Element'e tıklama
type(selector, text)              // Metin girme
getText(selector)                 // Metin alma
getAttribute(selector, attribute) // Attribute alma

// Bekleme işlemleri
waitForElement(selector)          // Element'in görünmesini bekleme
waitForElementToDisappear(selector) // Element'in kaybolmasını bekleme
waitForPageLoad()                 // Sayfa yüklenmesini bekleme

// Doğrulama işlemleri
isVisible(selector)               // Element görünür mü?
isEnabled(selector)               // Element aktif mi?
isSelected(selector)              // Element seçili mi?

// Navigasyon işlemleri
navigateTo(url)                   // URL'e gitme
getCurrentUrl()                   // Mevcut URL'i alma
getTitle()                        // Sayfa başlığını alma
refresh()                         // Sayfayı yenileme
```

## API Test Yazma

### API Test Yapısı

#### 1. Temel API Test
```java
package starlettech.tests.api;

import com.starlettech.core.BaseApiTest;
import com.starlettech.annotations.TestInfo;
import starlettech.api.clients.UserApiClient;
import starlettech.api.clients.AuthApiClient;
import starlettech.api.models.User;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.List;

public class UserApiTests extends BaseApiTest {
    
    private UserApiClient userApiClient;
    private AuthApiClient authApiClient;
    
    @BeforeMethod
    public void setUp() {
        userApiClient = new UserApiClient();
        authApiClient = new AuthApiClient();
        
        // Admin kullanıcısı ile giriş yap
        authApiClient.loginAndSetToken("admin", "admin123");
    }
    
    @Test(groups = {"smoke", "api", "users"}, priority = 1)
    @TestInfo(description = "Tüm kullanıcıları getirme testi", 
              author = "API Test Engineer", 
              priority = "HIGH")
    public void testGetAllUsers() {
        // API çağrısı yap
        List<User> users = userApiClient.getAllUsers();
        
        // Doğrulamalar
        Assert.assertNotNull(users, "Kullanıcı listesi null olmamalı");
        Assert.assertTrue(users.size() > 0, "En az bir kullanıcı olmalı");
        
        // İlk kullanıcının yapısını doğrula
        User firstUser = users.get(0);
        Assert.assertNotNull(firstUser.getId(), "Kullanıcı ID'si null olmamalı");
        Assert.assertNotNull(firstUser.getUsername(), "Kullanıcı adı null olmamalı");
        Assert.assertNotNull(firstUser.getEmail(), "E-posta null olmamalı");
        
        logger.info("Kullanıcı listesi testi tamamlandı - {} kullanıcı bulundu", 
                   users.size());
    }
    
    @Test(groups = {"regression", "api", "users"}, priority = 2)
    @TestInfo(description = "Yeni kullanıcı oluşturma testi", 
              author = "API Test Engineer", 
              priority = "HIGH")
    public void testCreateUser() {
        // Test kullanıcısı oluştur
        User newUser = new User(
            "testuser_" + System.currentTimeMillis(),
            "test" + System.currentTimeMillis() + "@example.com",
            "password123",
            "Test",
            "User"
        );
        
        // Kullanıcıyı oluştur
        User createdUser = userApiClient.createUser(newUser);
        
        // Doğrulamalar
        Assert.assertNotNull(createdUser, "Oluşturulan kullanıcı null olmamalı");
        Assert.assertNotNull(createdUser.getId(), "Kullanıcı ID'si atanmalı");
        Assert.assertEquals(createdUser.getUsername(), newUser.getUsername());
        Assert.assertEquals(createdUser.getEmail(), newUser.getEmail());
        
        logger.info("Kullanıcı oluşturma testi tamamlandı: {}", 
                   createdUser.getUsername());
    }
}
```

#### 2. API Client Yapısı
```java
package starlettech.api.clients;

import com.microsoft.playwright.APIResponse;
import com.starlettech.core.ApiRequestManager;
import starlettech.api.models.User;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

public class UserApiClient extends BaseApiClient {
    
    private static final String USERS_ENDPOINT = "/api/users";
    
    public List<User> getAllUsers() {
        APIResponse response = ApiRequestManager.authenticatedGet(USERS_ENDPOINT);
        
        if (response.status() == 200) {
            return jsonUtils.fromJson(response.text(), new TypeReference<List<User>>() {});
        } else {
            throw new RuntimeException("Kullanıcılar getirilemedi: " + response.status());
        }
    }
    
    public User getUserById(Long id) {
        String endpoint = USERS_ENDPOINT + "/" + id;
        APIResponse response = ApiRequestManager.authenticatedGet(endpoint);
        
        if (response.status() == 200) {
            return jsonUtils.fromJson(response.text(), User.class);
        } else if (response.status() == 404) {
            return null;
        } else {
            throw new RuntimeException("Kullanıcı getirilemedi: " + response.status());
        }
    }
    
    public User createUser(User user) {
        APIResponse response = ApiRequestManager.authenticatedPost(USERS_ENDPOINT, user);
        
        if (response.status() == 201) {
            return jsonUtils.fromJson(response.text(), User.class);
        } else {
            throw new RuntimeException("Kullanıcı oluşturulamadı: " + response.status());
        }
    }
    
    public User updateUser(Long id, User user) {
        String endpoint = USERS_ENDPOINT + "/" + id;
        APIResponse response = ApiRequestManager.authenticatedPut(endpoint, user);
        
        if (response.status() == 200) {
            return jsonUtils.fromJson(response.text(), User.class);
        } else {
            throw new RuntimeException("Kullanıcı güncellenemedi: " + response.status());
        }
    }
    
    public boolean deleteUser(Long id) {
        String endpoint = USERS_ENDPOINT + "/" + id;
        APIResponse response = ApiRequestManager.authenticatedDelete(endpoint);
        
        return response.status() == 204;
    }
}
```

## Test Verilerini Yönetme

### JSON Test Verileri

#### 1. Test Verisi Dosyası
**Konum**: `src/test/resources/testdata/users.json`

```json
{
  "validUsers": [
    {
      "username": "testuser1",
      "password": "password123",
      "email": "testuser1@example.com",
      "firstName": "Test",
      "lastName": "User1"
    },
    {
      "username": "testuser2",
      "password": "password456",
      "email": "testuser2@example.com",
      "firstName": "Test",
      "lastName": "User2"
    }
  ],
  "invalidUsers": [
    {
      "username": "invaliduser",
      "password": "wrongpassword",
      "expectedError": "Geçersiz kullanıcı adı veya şifre"
    }
  ],
  "adminUser": {
    "username": "admin",
    "password": "admin123",
    "role": "ADMIN"
  }
}
```

#### 2. Test Verisi Kullanımı
```java
public class LoginTests extends BaseTest {
    
    @Test
    public void testValidLoginWithTestData() {
        // Test verisini oku
        JsonNode userData = testDataReader.getUserData("testuser1");
        
        String username = userData.get("username").asText();
        String password = userData.get("password").asText();
        
        // Test adımları
        LoginPage loginPage = new LoginPage();
        loginPage.navigateToLoginPage();
        loginPage.login(username, password);
        
        // Doğrulama
        HomePage homePage = new HomePage();
        Assert.assertTrue(homePage.isUserLoggedIn());
    }
    
    @Test(dataProvider = "validUsersProvider")
    public void testMultipleValidLogins(String username, String password) {
        LoginPage loginPage = new LoginPage();
        loginPage.navigateToLoginPage();
        loginPage.login(username, password);
        
        HomePage homePage = new HomePage();
        Assert.assertTrue(homePage.isUserLoggedIn());
        
        // Çıkış yap
        homePage.clickLogout();
    }
    
    @DataProvider(name = "validUsersProvider")
    public Object[][] validUsersProvider() {
        JsonNode validUsers = testDataReader.getTestData("users", "validUsers");
        Object[][] data = new Object[validUsers.size()][2];
        
        for (int i = 0; i < validUsers.size(); i++) {
            JsonNode user = validUsers.get(i);
            data[i][0] = user.get("username").asText();
            data[i][1] = user.get("password").asText();
        }
        
        return data;
    }
}
```

### Properties Test Verileri

#### 1. Test Verisi Dosyası
**Konum**: `src/test/resources/testdata/test-users.properties`

```properties
# Geçerli Kullanıcılar
valid.user1.username=testuser1
valid.user1.password=password123
valid.user1.email=testuser1@example.com

valid.user2.username=testuser2
valid.user2.password=password456
valid.user2.email=testuser2@example.com

# Admin Kullanıcı
admin.username=admin
admin.password=admin123
admin.role=ADMIN

# Test URL'leri
test.login.url=/login
test.home.url=/home
test.profile.url=/profile
```

#### 2. Properties Kullanımı
```java
public class TestDataUtils {
    
    private static Properties testUsers;
    
    static {
        testUsers = new Properties();
        try (InputStream input = TestDataUtils.class.getResourceAsStream("/testdata/test-users.properties")) {
            testUsers.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Test verileri yüklenemedi", e);
        }
    }
    
    public static String getValidUsername(int userIndex) {
        return testUsers.getProperty("valid.user" + userIndex + ".username");
    }
    
    public static String getValidPassword(int userIndex) {
        return testUsers.getProperty("valid.user" + userIndex + ".password");
    }
    
    public static String getAdminUsername() {
        return testUsers.getProperty("admin.username");
    }
}
```

## Test Çalıştırma

### Maven ile Test Çalıştırma

#### 1. Tüm Testleri Çalıştırma
```bash
# Tüm testleri çalıştır
mvn test

# Temizlik yapıp testleri çalıştır
mvn clean test

# Paralel testleri çalıştır
mvn test -Dparallel.execution=true -Dthread.count=3
```

#### 2. Belirli Test Gruplarını Çalıştırma
```bash
# Smoke testlerini çalıştır
mvn test -Dgroups=smoke

# UI testlerini çalıştır
mvn test -Dgroups=ui

# API testlerini çalıştır
mvn test -Dgroups=api

# Birden fazla grubu çalıştır
mvn test -Dgroups="smoke,regression"
```

#### 3. Belirli Test Suite'lerini Çalıştırma
```bash
# UI smoke testleri
mvn test -DsuiteXmlFile=src/test/resources/suites/ui-smoke.xml

# API smoke testleri
mvn test -DsuiteXmlFile=src/test/resources/suites/api-smoke.xml

# Regresyon testleri
mvn test -DsuiteXmlFile=src/test/resources/suites/regression.xml

# Hibrit testler
mvn test -DsuiteXmlFile=src/test/resources/suites/hybrid-tests.xml
```

#### 4. Belirli Test Sınıflarını Çalıştırma
```bash
# Belirli test sınıfı
mvn test -Dtest=LoginTests

# Belirli test methodu
mvn test -Dtest=LoginTests#testValidLogin

# Birden fazla test sınıfı
mvn test -Dtest=LoginTests,HomeTests
```

### Ortam Parametreleri ile Çalıştırma

```bash
# Test ortamında çalıştır
mvn test -Denvironment=TEST

# Staging ortamında çalıştır
mvn test -Denvironment=STAGING

# Headless modda çalıştır
mvn test -Dbrowser.headless=true

# Farklı browser ile çalıştır
mvn test -Dbrowser.type=firefox

# Screenshot'ları etkinleştir
mvn test -Dscreenshot.on.failure=true

# Video kaydını etkinleştir
mvn test -Dbrowser.video.enabled=true
```

## Raporlama

### TestNG Raporları

TestNG otomatik olarak HTML raporları oluşturur:
- **Konum**: `target/surefire-reports/`
- **Ana Rapor**: `index.html`
- **Detaylı Rapor**: `emailable-report.html`

### ReportPortal Entegrasyonu

#### 1. ReportPortal Konfigürasyonu
```properties
# reportportal.properties
rp.endpoint=http://localhost:8080
rp.project=your-project
rp.launch=Playwright Tests
rp.enable=true
```

#### 2. ReportPortal Kullanımı
```java
@Test
public void testWithReportPortalLogging() {
    // Test adımlarını logla
    ReportPortal.emitLog("Test başlatılıyor", "INFO", new Date());
    
    LoginPage loginPage = new LoginPage();
    loginPage.navigateToLoginPage();
    
    ReportPortal.emitLog("Giriş sayfasına yönlendirildi", "INFO", new Date());
    
    // Screenshot ekle
    if (testConfig.isScreenshotOnFailure()) {
        File screenshot = ScreenshotUtils.takeScreenshot("login_page");
        ReportPortalListener.attachFile("Giriş sayfası screenshot", screenshot);
    }
}
```

### Custom Raporlama

#### 1. Test Sonuçlarını JSON'a Kaydetme
```java
public class CustomReportListener implements ITestListener {
    
    private List<TestResult> testResults = new ArrayList<>();
    
    @Override
    public void onTestSuccess(ITestResult result) {
        testResults.add(new TestResult(result, "PASSED"));
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        testResults.add(new TestResult(result, "FAILED"));
    }
    
    @Override
    public void onFinish(ITestContext context) {
        // JSON raporu oluştur
        String jsonReport = JsonUtils.toJson(testResults);
        FileUtils.writeToFile("target/custom-report.json", jsonReport);
    }
}
```

---

**Sonraki Bölüm**: [En İyi Uygulamalar](05-en-iyi-uygulamalar.md)

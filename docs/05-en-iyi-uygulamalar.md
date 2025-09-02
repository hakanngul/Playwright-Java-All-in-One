# Playwright Test Otomasyon Framework'ü - En İyi Uygulamalar

## 📋 İçindekiler
- [Kodlama Standartları](#kodlama-standartları)
- [Test Organizasyonu](#test-organizasyonu)
- [Page Object Best Practices](#page-object-best-practices)
- [API Test Best Practices](#api-test-best-practices)
- [Test Verisi Yönetimi](#test-verisi-yönetimi)
- [Performans Optimizasyonu](#performans-optimizasyonu)
- [Hata Yönetimi](#hata-yönetimi)
- [Bakım ve Sürdürülebilirlik](#bakım-ve-sürdürülebilirlik)

## Kodlama Standartları

### Java Kodlama Kuralları

#### 1. Naming Conventions
```java
// ✅ Doğru kullanım
public class LoginPageTests extends BaseTest {
    private static final String USERNAME_INPUT = "#username";
    private LoginPage loginPage;
    
    @Test
    public void testValidUserLogin() {
        // Test kodu
    }
}

// ❌ Yanlış kullanım
public class loginpagetests extends BaseTest {
    private static final String usernameInput = "#username";
    private LoginPage lp;
    
    @Test
    public void test1() {
        // Test kodu
    }
}
```

#### 2. Method Yapısı
```java
// ✅ Doğru method yapısı
@Test(groups = {"smoke", "ui", "login"}, priority = 1)
@TestInfo(description = "Geçerli kullanıcı girişi testi", 
          author = "Test Engineer", 
          priority = "HIGH")
public void testValidUserLogin() {
    // Arrange - Test verilerini hazırla
    String username = testDataReader.getUserData("testuser1").get("username").asText();
    String password = testDataReader.getUserData("testuser1").get("password").asText();
    
    // Act - Test adımlarını gerçekleştir
    LoginPage loginPage = new LoginPage();
    loginPage.navigateToLoginPage();
    loginPage.login(username, password);
    
    // Assert - Sonuçları doğrula
    HomePage homePage = new HomePage();
    Assert.assertTrue(homePage.isUserLoggedIn(), "Kullanıcı giriş yapmış olmalı");
    
    logger.info("Geçerli kullanıcı giriş testi başarıyla tamamlandı");
}
```

#### 3. Exception Handling
```java
// ✅ Doğru exception handling
public void clickElement(String selector) {
    try {
        waitForElement(selector);
        page.click(selector);
        logger.debug("Element'e tıklandı: {}", selector);
    } catch (TimeoutError e) {
        logger.error("Element bulunamadı: {}", selector);
        throw new RuntimeException("Element tıklanamadı: " + selector, e);
    } catch (Exception e) {
        logger.error("Beklenmeyen hata: {}", e.getMessage());
        throw new RuntimeException("Element tıklama işlemi başarısız", e);
    }
}
```

#### 4. Logging Best Practices
```java
// ✅ Doğru logging
public void login(String username, String password) {
    logger.info("Giriş işlemi başlatılıyor - Kullanıcı: {}", username);
    
    try {
        enterUsername(username);
        logger.debug("Kullanıcı adı girildi");
        
        enterPassword(password);
        logger.debug("Şifre girildi");
        
        clickLoginButton();
        logger.debug("Giriş butonuna tıklandı");
        
        logger.info("Giriş işlemi tamamlandı - Kullanıcı: {}", username);
    } catch (Exception e) {
        logger.error("Giriş işlemi başarısız - Kullanıcı: {}, Hata: {}", username, e.getMessage());
        throw e;
    }
}

// ❌ Yanlış logging
public void login(String username, String password) {
    System.out.println("Login starting");
    enterUsername(username);
    enterPassword(password);
    clickLoginButton();
    System.out.println("Login done");
}
```

### Test Sınıfı Yapısı

#### 1. Test Sınıfı Template
```java
package starlettech.tests.ui;

import com.starlettech.core.BaseTest;
import com.starlettech.annotations.TestInfo;
import starlettech.pages.LoginPage;
import starlettech.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * Login işlevselliği için test sınıfı
 * 
 * @author Test Engineer
 * @version 1.0
 * @since 2024-01-01
 */
public class LoginTests extends BaseTest {
    
    private LoginPage loginPage;
    private HomePage homePage;
    
    @BeforeClass
    public void setUpClass() {
        logger.info("Login test sınıfı başlatılıyor");
    }
    
    @BeforeMethod
    public void setUpMethod() {
        loginPage = new LoginPage();
        homePage = new HomePage();
    }
    
    @Test(groups = {"smoke", "ui", "login"}, priority = 1)
    @TestInfo(description = "Geçerli kullanıcı girişi testi", 
              author = "Test Engineer", 
              priority = "HIGH")
    public void testValidLogin() {
        // Test implementasyonu
    }
    
    @AfterClass
    public void tearDownClass() {
        logger.info("Login test sınıfı tamamlandı");
    }
}
```

## Test Organizasyonu

### Test Grupları Stratejisi

#### 1. Test Grupları Hiyerarşisi
```java
// Seviye 1: Test türü
@Test(groups = {"ui"})      // UI testleri
@Test(groups = {"api"})     // API testleri
@Test(groups = {"hybrid"})  // Hibrit testleri

// Seviye 2: Test kategorisi
@Test(groups = {"smoke"})      // Smoke testleri
@Test(groups = {"regression"}) // Regresyon testleri
@Test(groups = {"integration"}) // Entegrasyon testleri

// Seviye 3: Fonksiyonel alan
@Test(groups = {"login"})    // Giriş testleri
@Test(groups = {"user"})     // Kullanıcı testleri
@Test(groups = {"payment"})  // Ödeme testleri

// Kombinasyon kullanımı
@Test(groups = {"smoke", "ui", "login"})
public void testValidLogin() {
    // Test kodu
}
```

#### 2. Test Suite Organizasyonu
```
src/test/resources/suites/
├── smoke/
│   ├── ui-smoke.xml
│   ├── api-smoke.xml
│   └── hybrid-smoke.xml
├── regression/
│   ├── ui-regression.xml
│   ├── api-regression.xml
│   └── full-regression.xml
├── integration/
│   ├── ui-api-integration.xml
│   └── end-to-end.xml
└── nightly/
    └── nightly-tests.xml
```

### Test Verisi Organizasyonu

#### 1. Test Verisi Yapısı
```
src/test/resources/testdata/
├── environments/
│   ├── dev-data.json
│   ├── test-data.json
│   └── staging-data.json
├── users/
│   ├── valid-users.json
│   ├── invalid-users.json
│   └── admin-users.json
├── products/
│   ├── product-catalog.json
│   └── pricing-data.json
└── common/
    ├── test-config.properties
    └── error-messages.json
```

#### 2. Environment-Specific Data
```java
// ✅ Doğru environment yönetimi
public class TestDataManager {
    
    private static final String ENVIRONMENT = System.getProperty("environment", "TEST");
    
    public static JsonNode getUserData(String userType) {
        String fileName = String.format("environments/%s-data.json", 
                                       ENVIRONMENT.toLowerCase());
        return testDataReader.getTestData(fileName, "users", userType);
    }
    
    public static String getBaseUrl() {
        return testConfig.getEnvironmentProperty("base.url");
    }
}
```

## Page Object Best Practices

### Element Locator Stratejisi

#### 1. Locator Öncelik Sırası
```java
// ✅ Öncelik sırası (en iyi → en kötü)
private static final String USERNAME_INPUT = "#username";           // ID
private static final String LOGIN_BUTTON = "[data-testid='login']"; // Test ID
private static final String ERROR_MESSAGE = ".error-message";       // Class
private static final String SUBMIT_BTN = "button[type='submit']";    // Attribute
private static final String TITLE = "h1";                          // Tag
private static final String LINK = "//a[text()='Click here']";      // XPath (son çare)
```

#### 2. Dynamic Locator'lar
```java
// ✅ Doğru dynamic locator kullanımı
public class UserProfilePage extends BasePage {
    
    private static final String USER_MENU_TEMPLATE = "[data-user='%s'] .menu";
    private static final String NOTIFICATION_TEMPLATE = "//div[@class='notification' and contains(text(),'%s')]";
    
    public void clickUserMenu(String username) {
        String locator = String.format(USER_MENU_TEMPLATE, username);
        click(locator);
    }
    
    public boolean isNotificationDisplayed(String message) {
        String locator = String.format(NOTIFICATION_TEMPLATE, message);
        return isVisible(locator);
    }
}
```

### Page Object Yapısı

#### 1. Modüler Page Object
```java
// ✅ Doğru modüler yapı
public class LoginPage extends BasePage {
    
    // Locator'lar - private ve static final
    private static final String USERNAME_INPUT = "#username";
    private static final String PASSWORD_INPUT = "#password";
    private static final String LOGIN_BUTTON = "#login-btn";
    
    // Atomic işlemler - private
    private void enterUsername(String username) {
        type(USERNAME_INPUT, username);
    }
    
    private void enterPassword(String password) {
        type(PASSWORD_INPUT, password);
    }
    
    private void clickLoginButton() {
        click(LOGIN_BUTTON);
    }
    
    // Business işlemler - public
    public void login(String username, String password) {
        logger.info("Giriş yapılıyor: {}", username);
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
    }
    
    // Doğrulama işlemleri - public
    public boolean isLoginFormDisplayed() {
        return isVisible(USERNAME_INPUT) && 
               isVisible(PASSWORD_INPUT) && 
               isVisible(LOGIN_BUTTON);
    }
}
```

#### 2. Page Factory Pattern
```java
// ✅ Page Factory kullanımı
public class PageFactory {
    
    public static <T extends BasePage> T createPage(Class<T> pageClass) {
        try {
            return pageClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Page oluşturulamadı: " + pageClass.getSimpleName(), e);
        }
    }
}

// Kullanım
LoginPage loginPage = PageFactory.createPage(LoginPage.class);
```

## API Test Best Practices

### API Client Yapısı

#### 1. Base API Client
```java
// ✅ Doğru base client yapısı
public abstract class BaseApiClient {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());
    protected final JsonUtils jsonUtils = JsonUtils.getInstance();
    protected final ApiConfig apiConfig = ApiConfig.getInstance();
    
    protected APIResponse makeRequest(String method, String endpoint, Object body) {
        return ApiRequestManager.makeRequestWithRetry(method, endpoint, body);
    }
    
    protected <T> T parseResponse(APIResponse response, Class<T> responseType) {
        if (response.status() >= 200 && response.status() < 300) {
            return jsonUtils.fromJson(response.text(), responseType);
        } else {
            throw new ApiException("API çağrısı başarısız: " + response.status(), 
                                 response.status(), response.text());
        }
    }
    
    protected void validateResponse(APIResponse response, int expectedStatus) {
        if (response.status() != expectedStatus) {
            throw new ApiException("Beklenmeyen status kodu: " + response.status(), 
                                 response.status(), response.text());
        }
    }
}
```

#### 2. Specific API Client
```java
// ✅ Doğru specific client yapısı
public class UserApiClient extends BaseApiClient {
    
    private static final String USERS_ENDPOINT = "/api/users";
    
    public List<User> getAllUsers() {
        logger.info("Tüm kullanıcılar getiriliyor");
        
        APIResponse response = makeRequest("GET", USERS_ENDPOINT, null);
        validateResponse(response, 200);
        
        List<User> users = jsonUtils.fromJson(response.text(), 
                                            new TypeReference<List<User>>() {});
        
        logger.info("{} kullanıcı getirildi", users.size());
        return users;
    }
    
    public User createUser(User user) {
        logger.info("Yeni kullanıcı oluşturuluyor: {}", user.getUsername());
        
        APIResponse response = makeRequest("POST", USERS_ENDPOINT, user);
        validateResponse(response, 201);
        
        User createdUser = parseResponse(response, User.class);
        logger.info("Kullanıcı oluşturuldu - ID: {}", createdUser.getId());
        
        return createdUser;
    }
}
```

### API Test Yapısı

#### 1. Test Data Builder Pattern
```java
// ✅ Builder pattern kullanımı
public class UserBuilder {
    
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    
    public static UserBuilder aUser() {
        return new UserBuilder();
    }
    
    public UserBuilder withUsername(String username) {
        this.username = username;
        return this;
    }
    
    public UserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public UserBuilder withRandomData() {
        this.username = "user_" + System.currentTimeMillis();
        this.email = "test_" + System.currentTimeMillis() + "@example.com";
        this.password = "password123";
        this.firstName = "Test";
        this.lastName = "User";
        return this;
    }
    
    public User build() {
        return new User(username, email, password, firstName, lastName);
    }
}

// Kullanım
@Test
public void testCreateUser() {
    User testUser = UserBuilder.aUser()
                              .withRandomData()
                              .withEmail("specific@example.com")
                              .build();
    
    User createdUser = userApiClient.createUser(testUser);
    Assert.assertNotNull(createdUser.getId());
}
```

## Test Verisi Yönetimi

### Test Data Patterns

#### 1. Test Data Factory
```java
// ✅ Test data factory pattern
public class TestDataFactory {
    
    private static final Random random = new Random();
    
    public static User createValidUser() {
        return UserBuilder.aUser()
                         .withUsername("user_" + generateRandomString(8))
                         .withEmail(generateRandomEmail())
                         .withPassword("Password123!")
                         .withFirstName("Test")
                         .withLastName("User")
                         .build();
    }
    
    public static User createInvalidUser() {
        return UserBuilder.aUser()
                         .withUsername("") // Invalid username
                         .withEmail("invalid-email")
                         .withPassword("123") // Too short
                         .build();
    }
    
    private static String generateRandomString(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }
    
    private static String generateRandomEmail() {
        return "test_" + System.currentTimeMillis() + "@example.com";
    }
}
```

#### 2. Environment-Specific Configuration
```java
// ✅ Environment configuration
public class EnvironmentConfig {
    
    private static final String ENVIRONMENT = System.getProperty("environment", "TEST");
    
    public static String getBaseUrl() {
        return switch (ENVIRONMENT.toUpperCase()) {
            case "DEV" -> "https://dev.example.com";
            case "TEST" -> "https://test.example.com";
            case "STAGING" -> "https://staging.example.com";
            case "PROD" -> "https://example.com";
            default -> throw new IllegalArgumentException("Geçersiz environment: " + ENVIRONMENT);
        };
    }
    
    public static DatabaseConfig getDatabaseConfig() {
        return DatabaseConfig.builder()
                           .url(getProperty("db.url"))
                           .username(getProperty("db.username"))
                           .password(getProperty("db.password"))
                           .build();
    }
}
```

## Performans Optimizasyonu

### Test Yürütme Optimizasyonu

#### 1. Paralel Test Yürütme
```xml
<!-- ✅ Doğru paralel konfigürasyon -->
<suite name="Parallel Tests" parallel="methods" thread-count="3" 
       data-provider-thread-count="2">
    
    <test name="UI Tests" parallel="methods" thread-count="2">
        <classes>
            <class name="starlettech.tests.ui.LoginTests"/>
            <class name="starlettech.tests.ui.HomeTests"/>
        </classes>
    </test>
    
    <test name="API Tests" parallel="methods" thread-count="3">
        <classes>
            <class name="starlettech.tests.api.UserApiTests"/>
            <class name="starlettech.tests.api.AuthApiTests"/>
        </classes>
    </test>
</suite>
```

#### 2. Resource Management
```java
// ✅ Doğru resource yönetimi
public class PlaywrightManager {
    
    private static final int MAX_BROWSER_INSTANCES = 5;
    private static final Semaphore browserSemaphore = new Semaphore(MAX_BROWSER_INSTANCES);
    
    public static void launchBrowser() {
        try {
            browserSemaphore.acquire();
            // Browser launch logic
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Browser launch interrupted", e);
        }
    }
    
    public static void cleanup() {
        try {
            // Cleanup logic
        } finally {
            browserSemaphore.release();
        }
    }
}
```

### Wait Strategy Optimizasyonu

#### 1. Smart Waits
```java
// ✅ Akıllı bekleme stratejisi
public class SmartWaitUtils {
    
    public void waitForElementWithCondition(String selector, 
                                          Predicate<Locator> condition) {
        Locator element = page.locator(selector);
        
        element.waitFor(new Locator.WaitForOptions()
                      .setState(WaitForSelectorState.VISIBLE)
                      .setTimeout(30000));
        
        // Additional condition check
        if (!condition.test(element)) {
            throw new TimeoutError("Element condition not met: " + selector);
        }
    }
    
    public void waitForApiResponse(String endpoint, int expectedStatus) {
        page.waitForResponse(response -> 
            response.url().contains(endpoint) && 
            response.status() == expectedStatus
        );
    }
}
```

## Hata Yönetimi

### Exception Handling Strategy

#### 1. Custom Exceptions
```java
// ✅ Custom exception hierarchy
public class TestAutomationException extends RuntimeException {
    public TestAutomationException(String message) {
        super(message);
    }
    
    public TestAutomationException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class ElementNotFoundException extends TestAutomationException {
    public ElementNotFoundException(String selector) {
        super("Element bulunamadı: " + selector);
    }
}

public class ApiException extends TestAutomationException {
    private final int statusCode;
    private final String responseBody;
    
    public ApiException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
}
```

#### 2. Retry Mechanism
```java
// ✅ Retry mechanism
public class RetryUtils {
    
    public static <T> T executeWithRetry(Supplier<T> operation, 
                                       int maxRetries, 
                                       long delayMs) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                logger.warn("İşlem başarısız (deneme {}/{}): {}", 
                           attempt, maxRetries, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delayMs * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("İşlem " + maxRetries + " denemeden sonra başarısız", 
                                 lastException);
    }
}
```

## Bakım ve Sürdürülebilirlik

### Code Review Checklist

#### 1. Test Code Review Points
- [ ] Test adı açıklayıcı ve anlaşılır mı?
- [ ] Test tek bir işlevselliği test ediyor mu?
- [ ] Assertion'lar anlamlı hata mesajları içeriyor mu?
- [ ] Test verileri hard-coded değil mi?
- [ ] Exception handling uygun mu?
- [ ] Logging yeterli ve anlamlı mı?
- [ ] Test grupları doğru atanmış mı?

#### 2. Page Object Review Points
- [ ] Locator'lar stabil ve maintainable mı?
- [ ] Method'lar single responsibility principle'a uyuyor mu?
- [ ] Wait stratejileri uygun mu?
- [ ] Error handling implement edilmiş mi?
- [ ] Documentation yeterli mi?

### Refactoring Guidelines

#### 1. Test Refactoring
```java
// ❌ Refactoring öncesi
@Test
public void test1() {
    page.navigate("https://example.com/login");
    page.fill("#username", "testuser");
    page.fill("#password", "password");
    page.click("#login-btn");
    Assert.assertTrue(page.isVisible("#welcome"));
}

// ✅ Refactoring sonrası
@Test(groups = {"smoke", "ui", "login"})
@TestInfo(description = "Geçerli kullanıcı girişi testi", 
          author = "Test Engineer", 
          priority = "HIGH")
public void testValidUserLogin() {
    // Arrange
    User testUser = TestDataFactory.createValidUser();
    
    // Act
    LoginPage loginPage = new LoginPage();
    loginPage.navigateToLoginPage();
    loginPage.login(testUser.getUsername(), testUser.getPassword());
    
    // Assert
    HomePage homePage = new HomePage();
    Assert.assertTrue(homePage.isWelcomeMessageDisplayed(), 
                     "Hoş geldin mesajı görüntülenmeli");
}
```

---

**Sonraki Bölüm**: [API Referansı](06-api-referansi.md)

# Playwright Test Otomasyon Framework'ü - API Referansı

## 📋 İçindekiler
- [Çekirdek Sınıflar](#çekirdek-sınıflar)
- [Page Object Sınıfları](#page-object-sınıfları)
- [API Client Sınıfları](#api-client-sınıfları)
- [Utility Sınıfları](#utility-sınıfları)
- [Konfigürasyon Sınıfları](#konfigürasyon-sınıfları)
- [Annotation'lar](#annotationlar)
- [Enum'lar](#enumlar)

## Çekirdek Sınıflar

### PlaywrightManager

Framework'ün Playwright yaşam döngüsünü yöneten ana sınıf.

```java
public class PlaywrightManager {
    
    /**
     * Playwright instance'ını başlatır
     */
    public static void initializePlaywright()
    
    /**
     * Browser'ı başlatır
     * @param browserType Browser türü (CHROMIUM, FIREFOX, WEBKIT)
     */
    public static void launchBrowser(BrowserType browserType)
    
    /**
     * Yeni browser context oluşturur
     */
    public static void createContext()
    
    /**
     * Yeni sayfa oluşturur
     */
    public static void createPage()
    
    /**
     * Mevcut Playwright instance'ını döndürür
     * @return Playwright instance
     */
    public static Playwright getPlaywright()
    
    /**
     * Mevcut Browser instance'ını döndürür
     * @return Browser instance
     */
    public static Browser getBrowser()
    
    /**
     * Mevcut BrowserContext instance'ını döndürür
     * @return BrowserContext instance
     */
    public static BrowserContext getContext()
    
    /**
     * Mevcut Page instance'ını döndürür
     * @return Page instance
     */
    public static Page getPage()
    
    /**
     * Tüm kaynakları temizler
     */
    public static void cleanup()
}
```

### BaseTest

UI testleri için temel sınıf.

```java
public abstract class BaseTest {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());
    protected TestConfig testConfig;
    protected TestDataReader testDataReader;
    
    /**
     * Test öncesi kurulum
     * @param method Test method bilgisi
     */
    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method)
    
    /**
     * Test sonrası temizlik
     * @param result Test sonucu
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result)
    
    /**
     * Suite öncesi kurulum
     */
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite()
    
    /**
     * Suite sonrası temizlik
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuite()
}
```

### BaseApiTest

API testleri için temel sınıf.

```java
public abstract class BaseApiTest {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());
    protected ApiConfig apiConfig;
    protected TestConfig testConfig;
    protected TestDataReader testDataReader;
    
    /**
     * Suite öncesi API kurulumu
     */
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite()
    
    /**
     * Method öncesi kurulum
     */
    @BeforeMethod(alwaysRun = true)
    public void beforeMethod()
    
    /**
     * Method sonrası temizlik
     * @param result Test sonucu
     */
    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result)
    
    /**
     * Test kullanıcısı ile giriş yapar
     * @return Authentication token
     */
    protected String loginWithTestUser()
}
```

### BasePage

Tüm page object'lerin temel sınıfı.

```java
public abstract class BasePage {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());
    protected Page page;
    protected TestConfig testConfig;
    protected WaitUtils waitUtils;
    
    /**
     * URL'e yönlendirir
     * @param url Hedef URL
     */
    public void navigateTo(String url)
    
    /**
     * Mevcut URL'i döndürür
     * @return Mevcut URL
     */
    public String getCurrentUrl()
    
    /**
     * Sayfa başlığını döndürür
     * @return Sayfa başlığı
     */
    public String getTitle()
    
    /**
     * Element'e tıklar
     * @param selector Element selector'ı
     */
    public void click(String selector)
    
    /**
     * Element'e metin yazar
     * @param selector Element selector'ı
     * @param text Yazılacak metin
     */
    public void type(String selector, String text)
    
    /**
     * Element'in metnini alır
     * @param selector Element selector'ı
     * @return Element metni
     */
    public String getText(String selector)
    
    /**
     * Element'in görünür olup olmadığını kontrol eder
     * @param selector Element selector'ı
     * @return Görünürlük durumu
     */
    public boolean isVisible(String selector)
    
    /**
     * Element'in aktif olup olmadığını kontrol eder
     * @param selector Element selector'ı
     * @return Aktiflik durumu
     */
    public boolean isEnabled(String selector)
    
    /**
     * Element'i bekler
     * @param selector Element selector'ı
     */
    public void waitForElement(String selector)
    
    /**
     * Sayfa yüklenmesini bekler
     */
    public void waitForPageLoad()
}
```

## Page Object Sınıfları

### LoginPage

Giriş sayfası işlemleri için page object sınıfı.

```java
public class LoginPage extends BasePage {
    
    // Locator sabitleri
    private static final String USERNAME_INPUT = "#username";
    private static final String PASSWORD_INPUT = "#password";
    private static final String LOGIN_BUTTON = "#login-btn";
    private static final String ERROR_MESSAGE = ".error-message";
    private static final String FORGOT_PASSWORD_LINK = "#forgot-password";
    private static final String REMEMBER_ME_CHECKBOX = "#remember-me";
    private static final String LOGIN_FORM = "#login-form";
    
    /**
     * Giriş sayfasına yönlendirir
     */
    public void navigateToLoginPage()
    
    /**
     * Kullanıcı adı girer
     * @param username Kullanıcı adı
     */
    public void enterUsername(String username)
    
    /**
     * Şifre girer
     * @param password Şifre
     */
    public void enterPassword(String password)
    
    /**
     * Giriş butonuna tıklar
     */
    public void clickLoginButton()
    
    /**
     * Giriş işlemini gerçekleştirir
     * @param username Kullanıcı adı
     * @param password Şifre
     */
    public void login(String username, String password)
    
    /**
     * Beni hatırla seçeneği ile giriş yapar
     * @param username Kullanıcı adı
     * @param password Şifre
     */
    public void loginWithRememberMe(String username, String password)
    
    /**
     * Hata mesajını alır
     * @return Hata mesajı
     */
    public String getErrorMessage()
    
    /**
     * Giriş formunun görünür olup olmadığını kontrol eder
     * @return Form görünürlük durumu
     */
    public boolean isLoginFormDisplayed()
    
    /**
     * Hata mesajının görünür olup olmadığını kontrol eder
     * @return Hata mesajı görünürlük durumu
     */
    public boolean isErrorMessageDisplayed()
}
```

### HomePage

Ana sayfa işlemleri için page object sınıfı.

```java
public class HomePage extends BasePage {
    
    // Locator sabitleri
    private static final String WELCOME_MESSAGE = "#welcome-message";
    private static final String USER_PROFILE = "#user-profile";
    private static final String LOGOUT_BUTTON = "#logout-btn";
    private static final String NAVIGATION_MENU = "#nav-menu";
    private static final String SEARCH_BOX = "#search-box";
    private static final String MAIN_CONTENT = "#main-content";
    
    /**
     * Ana sayfaya yönlendirir
     */
    public void navigateToHomePage()
    
    /**
     * Hoş geldin mesajını alır
     * @return Hoş geldin mesajı
     */
    public String getWelcomeMessage()
    
    /**
     * Çıkış butonuna tıklar
     */
    public void clickLogout()
    
    /**
     * Arama yapar
     * @param searchTerm Arama terimi
     */
    public void search(String searchTerm)
    
    /**
     * Kullanıcının giriş yapıp yapmadığını kontrol eder
     * @return Giriş durumu
     */
    public boolean isUserLoggedIn()
    
    /**
     * Hoş geldin mesajının görünür olup olmadığını kontrol eder
     * @return Mesaj görünürlük durumu
     */
    public boolean isWelcomeMessageDisplayed()
    
    /**
     * Ana sayfa elementlerini doğrular
     * @return Doğrulama sonucu
     */
    public boolean verifyHomePageElements()
}
```

## API Client Sınıfları

### BaseApiClient

Tüm API client'ların temel sınıfı.

```java
public abstract class BaseApiClient {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());
    protected final JsonUtils jsonUtils = JsonUtils.getInstance();
    protected final ApiConfig apiConfig = ApiConfig.getInstance();
    
    /**
     * HTTP isteği yapar
     * @param method HTTP method
     * @param endpoint API endpoint
     * @param body İstek gövdesi
     * @return API yanıtı
     */
    protected APIResponse makeRequest(String method, String endpoint, Object body)
    
    /**
     * API yanıtını parse eder
     * @param response API yanıtı
     * @param responseType Yanıt türü
     * @return Parse edilmiş nesne
     */
    protected <T> T parseResponse(APIResponse response, Class<T> responseType)
    
    /**
     * API yanıtını doğrular
     * @param response API yanıtı
     * @param expectedStatus Beklenen status kodu
     */
    protected void validateResponse(APIResponse response, int expectedStatus)
}
```

### UserApiClient

Kullanıcı API işlemleri için client sınıfı.

```java
public class UserApiClient extends BaseApiClient {
    
    private static final String USERS_ENDPOINT = "/api/users";
    
    /**
     * Tüm kullanıcıları getirir
     * @return Kullanıcı listesi
     */
    public List<User> getAllUsers()
    
    /**
     * ID'ye göre kullanıcı getirir
     * @param id Kullanıcı ID'si
     * @return Kullanıcı nesnesi
     */
    public User getUserById(Long id)
    
    /**
     * Yeni kullanıcı oluşturur
     * @param user Kullanıcı nesnesi
     * @return Oluşturulan kullanıcı
     */
    public User createUser(User user)
    
    /**
     * Kullanıcıyı günceller
     * @param id Kullanıcı ID'si
     * @param user Güncellenmiş kullanıcı nesnesi
     * @return Güncellenmiş kullanıcı
     */
    public User updateUser(Long id, User user)
    
    /**
     * Kullanıcıyı siler
     * @param id Kullanıcı ID'si
     * @return Silme işlemi sonucu
     */
    public boolean deleteUser(Long id)
}
```

### AuthApiClient

Kimlik doğrulama API işlemleri için client sınıfı.

```java
public class AuthApiClient extends BaseApiClient {
    
    private static final String AUTH_ENDPOINT = "/api/auth";
    
    /**
     * Kullanıcı girişi yapar
     * @param username Kullanıcı adı
     * @param password Şifre
     * @return Authentication token
     */
    public String login(String username, String password)
    
    /**
     * Giriş yapar ve token'ı ayarlar
     * @param username Kullanıcı adı
     * @param password Şifre
     */
    public void loginAndSetToken(String username, String password)
    
    /**
     * Token'ı doğrular
     * @param token Authentication token
     * @return Doğrulama sonucu
     */
    public boolean validateToken(String token)
    
    /**
     * Çıkış yapar
     * @return Çıkış işlemi sonucu
     */
    public boolean logout()
    
    /**
     * Şifre sıfırlama isteği gönderir
     * @param email E-posta adresi
     * @return İşlem sonucu
     */
    public boolean requestPasswordReset(String email)
}
```

## Utility Sınıfları

### WaitUtils

Bekleme işlemleri için utility sınıfı.

```java
public class WaitUtils {
    
    private final Page page;
    private final TestConfig testConfig;
    
    /**
     * Constructor
     * @param page Playwright Page nesnesi
     */
    public WaitUtils(Page page)
    
    /**
     * Element'in görünür olmasını bekler
     * @param selector Element selector'ı
     */
    public void waitForVisible(String selector)
    
    /**
     * Element'in görünür olmasını bekler (timeout ile)
     * @param selector Element selector'ı
     * @param timeoutMs Timeout süresi (ms)
     */
    public void waitForVisible(String selector, int timeoutMs)
    
    /**
     * Element'in kaybolmasını bekler
     * @param selector Element selector'ı
     */
    public void waitForHidden(String selector)
    
    /**
     * Element'in tıklanabilir olmasını bekler
     * @param selector Element selector'ı
     */
    public void waitForClickable(String selector)
    
    /**
     * Sayfa yüklenmesini bekler
     */
    public void waitForPageLoad()
    
    /**
     * URL'in belirli metni içermesini bekler
     * @param text Beklenen metin
     */
    public void waitForUrlContains(String text)
    
    /**
     * API yanıtını bekler
     * @param urlPattern URL pattern'i
     * @param statusCode Beklenen status kodu
     */
    public void waitForApiResponse(String urlPattern, int statusCode)
}
```

### ScreenshotUtils

Ekran görüntüsü işlemleri için utility sınıfı.

```java
public class ScreenshotUtils {
    
    /**
     * Ekran görüntüsü alır
     * @return Screenshot dosya yolu
     */
    public static String takeScreenshot()
    
    /**
     * İsimlendirilmiş ekran görüntüsü alır
     * @param screenshotName Screenshot adı
     * @return Screenshot dosya yolu
     */
    public static String takeScreenshot(String screenshotName)
    
    /**
     * Element'in ekran görüntüsünü alır
     * @param selector Element selector'ı
     * @return Screenshot dosya yolu
     */
    public static String takeElementScreenshot(String selector)
    
    /**
     * Tam sayfa ekran görüntüsü alır
     * @return Screenshot dosya yolu
     */
    public static String takeFullPageScreenshot()
    
    /**
     * Screenshot'ı ReportPortal'a ekler
     * @param screenshotPath Screenshot dosya yolu
     * @param description Açıklama
     */
    public static void attachScreenshotToReport(String screenshotPath, String description)
}
```

### TestDataReader

Test verisi okuma işlemleri için utility sınıfı.

```java
public class TestDataReader {
    
    private static TestDataReader instance;
    
    /**
     * Singleton instance döndürür
     * @return TestDataReader instance
     */
    public static TestDataReader getInstance()
    
    /**
     * JSON test verisini okur
     * @param fileName Dosya adı
     * @param keys JSON anahtarları
     * @return JSON node
     */
    public JsonNode getTestData(String fileName, String... keys)
    
    /**
     * Kullanıcı verisini okur
     * @param userKey Kullanıcı anahtarı
     * @return Kullanıcı verisi
     */
    public JsonNode getUserData(String userKey)
    
    /**
     * Properties dosyasını okur
     * @param fileName Dosya adı
     * @return Properties nesnesi
     */
    public Properties readProperties(String fileName)
    
    /**
     * CSV dosyasını okur
     * @param fileName Dosya adı
     * @return CSV verileri
     */
    public List<Map<String, String>> readCsvData(String fileName)
}
```

## Konfigürasyon Sınıfları

### TestConfig

Test konfigürasyon yönetimi için sınıf.

```java
public class TestConfig {
    
    private static TestConfig instance;
    
    /**
     * Singleton instance döndürür
     * @return TestConfig instance
     */
    public static TestConfig getInstance()
    
    /**
     * Base URL'i döndürür
     * @return Base URL
     */
    public String getBaseUrl()
    
    /**
     * Browser türünü döndürür
     * @return Browser türü
     */
    public BrowserType getBrowserType()
    
    /**
     * Headless modunu döndürür
     * @return Headless modu
     */
    public boolean isHeadless()
    
    /**
     * Timeout değerini döndürür
     * @return Timeout (ms)
     */
    public int getTimeout()
    
    /**
     * Screenshot ayarını döndürür
     * @return Screenshot aktif mi
     */
    public boolean isScreenshotOnFailure()
    
    /**
     * Retry ayarını döndürür
     * @return Retry aktif mi
     */
    public boolean isRetryEnabled()
    
    /**
     * Retry sayısını döndürür
     * @return Retry sayısı
     */
    public int getRetryCount()
}
```

### ApiConfig

API konfigürasyon yönetimi için sınıf.

```java
public class ApiConfig {
    
    private static ApiConfig instance;
    
    /**
     * Singleton instance döndürür
     * @return ApiConfig instance
     */
    public static ApiConfig getInstance()
    
    /**
     * API base URL'i döndürür
     * @return API base URL
     */
    public String getApiBaseUrl()
    
    /**
     * API timeout değerini döndürür
     * @return API timeout (ms)
     */
    public int getApiTimeout()
    
    /**
     * Content type döndürür
     * @return Content type
     */
    public String getContentType()
    
    /**
     * API logging ayarını döndürür
     * @return Logging aktif mi
     */
    public boolean isApiLoggingEnabled()
    
    /**
     * Auth endpoint döndürür
     * @return Auth endpoint
     */
    public String getAuthEndpoint()
}
```

## Annotation'lar

### @TestInfo

Test metadata bilgileri için annotation.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestInfo {
    
    /**
     * Test açıklaması
     * @return Açıklama
     */
    String description() default "";
    
    /**
     * Test yazarı
     * @return Yazar
     */
    String author() default "";
    
    /**
     * Test önceliği
     * @return Öncelik (HIGH, MEDIUM, LOW)
     */
    String priority() default "MEDIUM";
    
    /**
     * JIRA ticket ID
     * @return JIRA ID
     */
    String jiraId() default "";
    
    /**
     * Test kategorisi
     * @return Kategori
     */
    String category() default "";
}
```

### @ApiTest

API testleri için marker annotation.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApiTest {
    
    /**
     * API versiyonu
     * @return Versiyon
     */
    String version() default "v1";
    
    /**
     * Base endpoint
     * @return Endpoint
     */
    String baseEndpoint() default "";
}
```

## Enum'lar

### BrowserType

Desteklenen browser türleri.

```java
public enum BrowserType {
    CHROMIUM("chromium"),
    FIREFOX("firefox"),
    WEBKIT("webkit");
    
    private final String browserName;
    
    BrowserType(String browserName) {
        this.browserName = browserName;
    }
    
    public String getBrowserName() {
        return browserName;
    }
    
    public static BrowserType fromString(String browserName) {
        for (BrowserType type : values()) {
            if (type.browserName.equalsIgnoreCase(browserName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported browser: " + browserName);
    }
}
```

### Environment

Test ortamları.

```java
public enum Environment {
    DEV("dev", "https://dev.example.com"),
    TEST("test", "https://test.example.com"),
    STAGING("staging", "https://staging.example.com"),
    PROD("prod", "https://example.com");
    
    private final String name;
    private final String baseUrl;
    
    Environment(String name, String baseUrl) {
        this.name = name;
        this.baseUrl = baseUrl;
    }
    
    public String getName() {
        return name;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
}
```

---

**Sonraki Bölüm**: [Sorun Giderme](07-sorun-giderme.md)

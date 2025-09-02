# Playwright Test Otomasyon Framework'ü - Sorun Giderme

## 📋 İçindekiler
- [Kurulum Sorunları](#kurulum-sorunları)
- [Browser Sorunları](#browser-sorunları)
- [Test Yürütme Sorunları](#test-yürütme-sorunları)
- [API Test Sorunları](#api-test-sorunları)
- [Konfigürasyon Sorunları](#konfigürasyon-sorunları)
- [Performans Sorunları](#performans-sorunları)
- [Raporlama Sorunları](#raporlama-sorunları)
- [Docker Sorunları](#docker-sorunları)

## Kurulum Sorunları

### Java Version Uyumsuzluğu

**Sorun**: `java.lang.UnsupportedClassVersionError` hatası alınıyor.

**Çözüm**:
```bash
# Java versiyonunu kontrol edin
java -version

# Java 21 kurulu değilse yükleyin
# Windows için
winget install EclipseAdoptium.Temurin.21.JDK

# macOS için
brew install openjdk@21

# Ubuntu için
sudo apt install openjdk-21-jdk

# JAVA_HOME ayarlayın
export JAVA_HOME=/path/to/java21
export PATH=$JAVA_HOME/bin:$PATH
```

### Maven Bağımlılık Sorunları

**Sorun**: `Could not resolve dependencies` hatası alınıyor.

**Çözüm**:
```bash
# Maven cache'i temizleyin
mvn clean install -U

# Local repository'yi temizleyin
rm -rf ~/.m2/repository

# Proxy ayarları (gerekirse)
mvn clean install -Dhttp.proxyHost=proxy.company.com -Dhttp.proxyPort=8080
```

**Sorun**: `Plugin execution not covered by lifecycle configuration` hatası.

**Çözüm**:
```xml
<!-- pom.xml'e ekleyin -->
<plugin>
    <groupId>org.eclipse.m2e</groupId>
    <artifactId>lifecycle-mapping</artifactId>
    <version>1.0.0</version>
    <configuration>
        <lifecycleMappingMetadata>
            <pluginExecutions>
                <pluginExecution>
                    <pluginExecutionFilter>
                        <groupId>com.microsoft.playwright</groupId>
                        <artifactId>playwright</artifactId>
                        <versionRange>[1.0.0,)</versionRange>
                        <goals>
                            <goal>install</goal>
                        </goals>
                    </pluginExecutionFilter>
                    <action>
                        <ignore />
                    </action>
                </pluginExecution>
            </pluginExecutions>
        </lifecycleMappingMetadata>
    </configuration>
</plugin>
```

### Playwright Browser Kurulum Sorunları

**Sorun**: `Browser executable not found` hatası alınıyor.

**Çözüm**:
```bash
# Browser'ları manuel olarak yükleyin
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"

# Belirli browser yükleyin
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"

# Force install
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --force"

# System dependencies yükleyin (Linux)
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install-deps"
```

## Browser Sorunları

### Browser Başlatma Sorunları

**Sorun**: `Failed to launch browser` hatası alınıyor.

**Çözüm**:
```java
// Browser launch options'ı kontrol edin
Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
    .setIgnoreHTTPSErrors(true)
    .setViewportSize(1920, 1080);

// Headless mode'u devre dışı bırakın (debug için)
BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
    .setHeadless(false)
    .setSlowMo(1000);
```

**Sorun**: `Browser context creation failed` hatası.

**Çözüm**:
```bash
# Sistem kaynaklarını kontrol edin
free -h  # Linux/macOS
wmic OS get TotalVisibleMemorySize /value  # Windows

# Browser process'lerini temizleyin
pkill -f "chromium\|firefox\|webkit"  # Linux/macOS
taskkill /F /IM chrome.exe /IM firefox.exe  # Windows
```

### Element Bulunamama Sorunları

**Sorun**: `Element not found` veya `TimeoutError` hatası.

**Çözüm**:
```java
// Wait stratejisini iyileştirin
public void waitForElementWithRetry(String selector) {
    for (int i = 0; i < 3; i++) {
        try {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
            return;
        } catch (TimeoutError e) {
            logger.warn("Element bulunamadı, tekrar deneniyor: {}", selector);
            page.waitForTimeout(1000);
        }
    }
    throw new RuntimeException("Element 3 denemeden sonra bulunamadı: " + selector);
}

// Locator'ı iyileştirin
// ❌ Kötü locator
private static final String BUTTON = "button";

// ✅ İyi locator
private static final String LOGIN_BUTTON = "[data-testid='login-button']";
```

### Page Load Sorunları

**Sorun**: Sayfa yüklenmesi tamamlanmıyor.

**Çözüm**:
```java
// Load state'i kontrol edin
public void waitForPageLoadComplete() {
    page.waitForLoadState(LoadState.NETWORKIDLE);
    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    
    // JavaScript execution'ı bekleyin
    page.waitForFunction("() => document.readyState === 'complete'");
}

// Timeout'u artırın
page.setDefaultTimeout(60000); // 60 saniye
```

## Test Yürütme Sorunları

### Paralel Test Sorunları

**Sorun**: Paralel testlerde resource conflict'i oluşuyor.

**Çözüm**:
```java
// ThreadLocal kullanımını kontrol edin
public class PlaywrightManager {
    private static ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    
    public static void cleanup() {
        try {
            Playwright playwright = playwrightThreadLocal.get();
            if (playwright != null) {
                playwright.close();
            }
        } finally {
            playwrightThreadLocal.remove();
        }
    }
}

// Test isolation sağlayın
@BeforeMethod
public void setUp() {
    // Her test için yeni browser context
    PlaywrightManager.createContext();
    PlaywrightManager.createPage();
}
```

**Sorun**: Thread count ayarları çalışmıyor.

**Çözüm**:
```xml
<!-- TestNG suite konfigürasyonu -->
<suite name="Parallel Tests" parallel="methods" thread-count="3" 
       data-provider-thread-count="2" preserve-order="false">
    
    <test name="UI Tests" parallel="methods" thread-count="2">
        <classes>
            <class name="starlettech.tests.ui.LoginTests"/>
        </classes>
    </test>
</suite>
```

### Test Data Sorunları

**Sorun**: Test verileri bulunamıyor veya yüklenmiyor.

**Çözüm**:
```java
// Dosya yolunu kontrol edin
public class TestDataReader {
    
    public JsonNode getTestData(String fileName) {
        String resourcePath = "/testdata/" + fileName;
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);
        
        if (inputStream == null) {
            throw new RuntimeException("Test data dosyası bulunamadı: " + resourcePath);
        }
        
        try {
            return objectMapper.readTree(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Test data okunamadı: " + fileName, e);
        }
    }
}

// Classpath'i kontrol edin
mvn clean compile test-compile
```

### Memory Leak Sorunları

**Sorun**: Testler sırasında memory kullanımı artıyor.

**Çözüm**:
```java
// Resource cleanup'ı iyileştirin
@AfterMethod
public void tearDown() {
    try {
        // Screenshot al (gerekirse)
        if (testResult.getStatus() == ITestResult.FAILURE) {
            ScreenshotUtils.takeScreenshot();
        }
    } finally {
        // Her zaman cleanup yap
        PlaywrightManager.cleanup();
    }
}

// JVM memory ayarları
export MAVEN_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"
```

## API Test Sorunları

### Connection Timeout Sorunları

**Sorun**: API çağrılarında timeout hatası alınıyor.

**Çözüm**:
```java
// Timeout ayarlarını artırın
public class ApiConfig {
    public int getApiTimeout() {
        return Integer.parseInt(getProperty("api.timeout", "60000")); // 60 saniye
    }
}

// Retry mechanism ekleyin
public APIResponse makeRequestWithRetry(String method, String endpoint, Object body) {
    int maxRetries = 3;
    for (int attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            return ApiUtils.makeRequest(method, endpoint, body);
        } catch (Exception e) {
            if (attempt == maxRetries) {
                throw e;
            }
            logger.warn("API çağrısı başarısız (deneme {}/{}): {}", attempt, maxRetries, e.getMessage());
            try {
                Thread.sleep(1000 * attempt); // Exponential backoff
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Retry interrupted", ie);
            }
        }
    }
    return null;
}
```

### Authentication Sorunları

**Sorun**: API authentication başarısız oluyor.

**Çözüm**:
```java
// Token yönetimini kontrol edin
public class ApiRequestManager {
    
    private static ThreadLocal<String> authTokenThreadLocal = new ThreadLocal<>();
    
    public static void setAuthToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Auth token boş olamaz");
        }
        authTokenThreadLocal.set(token);
        logger.debug("Auth token ayarlandı");
    }
    
    public static Map<String, String> getAuthHeaders() {
        String token = authTokenThreadLocal.get();
        if (token == null) {
            throw new RuntimeException("Auth token ayarlanmamış");
        }
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        headers.put("Content-Type", "application/json");
        return headers;
    }
}
```

### JSON Parsing Sorunları

**Sorun**: API response parsing hatası alınıyor.

**Çözüm**:
```java
// JSON parsing'i iyileştirin
public class JsonUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            if (json == null || json.trim().isEmpty()) {
                throw new IllegalArgumentException("JSON string boş olamaz");
            }
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("JSON parsing hatası: {}", json);
            throw new RuntimeException("JSON parsing başarısız", e);
        }
    }
}
```

## Konfigürasyon Sorunları

### Properties Dosyası Sorunları

**Sorun**: Konfigürasyon dosyası bulunamıyor.

**Çözüm**:
```java
// Resource loading'i kontrol edin
public class TestConfig {
    
    private Properties loadProperties(String fileName) {
        Properties properties = new Properties();
        
        // Önce classpath'ten dene
        try (InputStream input = getClass().getResourceAsStream("/" + fileName)) {
            if (input != null) {
                properties.load(input);
                return properties;
            }
        } catch (IOException e) {
            logger.warn("Classpath'ten properties okunamadı: {}", fileName);
        }
        
        // Sonra file system'den dene
        try (InputStream input = new FileInputStream(fileName)) {
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Properties dosyası bulunamadı: " + fileName, e);
        }
    }
}
```

### Environment Variable Sorunları

**Sorun**: Environment variable'lar okunmuyor.

**Çözüm**:
```java
// Environment variable priority'sini ayarlayın
public String getProperty(String key, String defaultValue) {
    // 1. System property
    String value = System.getProperty(key);
    if (value != null) {
        return value;
    }
    
    // 2. Environment variable
    value = System.getenv(key.toUpperCase().replace('.', '_'));
    if (value != null) {
        return value;
    }
    
    // 3. Properties file
    value = properties.getProperty(key);
    if (value != null) {
        return value;
    }
    
    // 4. Default value
    return defaultValue;
}
```

## Performans Sorunları

### Yavaş Test Yürütme

**Sorun**: Testler çok yavaş çalışıyor.

**Çözüm**:
```java
// Wait stratejisini optimize edin
public void optimizedWait(String selector) {
    // Implicit wait yerine explicit wait kullanın
    page.waitForSelector(selector, new Page.WaitForSelectorOptions()
        .setTimeout(5000)); // Kısa timeout
}

// Page load stratejisini değiştirin
page.navigate(url, new Page.NavigateOptions()
    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)); // NETWORKIDLE yerine

// Paralel yürütmeyi artırın
<suite parallel="methods" thread-count="5">
```

### Memory Kullanımı

**Sorun**: Yüksek memory kullanımı.

**Çözüm**:
```bash
# JVM heap size'ı ayarlayın
export MAVEN_OPTS="-Xms512m -Xmx2g -XX:NewRatio=1 -XX:+UseG1GC"

# Browser resource'larını sınırlayın
--max-old-space-size=1024
--disable-dev-shm-usage
--no-sandbox
```

## Raporlama Sorunları

### ReportPortal Bağlantı Sorunları

**Sorun**: ReportPortal'a bağlanılamıyor.

**Çözüm**:
```properties
# reportportal.properties dosyasını kontrol edin
rp.endpoint=http://localhost:8080
rp.uuid=your-uuid-here
rp.project=your-project
rp.enable=true

# Network bağlantısını test edin
curl -X GET "http://localhost:8080/api/v1/project"
```

### Screenshot Sorunları

**Sorun**: Screenshot'lar alınamıyor.

**Çözüm**:
```java
// Screenshot path'ini kontrol edin
public static String takeScreenshot() {
    try {
        String screenshotDir = System.getProperty("user.dir") + "/screenshots";
        Files.createDirectories(Paths.get(screenshotDir));
        
        String fileName = "screenshot_" + System.currentTimeMillis() + ".png";
        String filePath = screenshotDir + "/" + fileName;
        
        Page page = PlaywrightManager.getPage();
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(filePath)));
        
        return filePath;
    } catch (Exception e) {
        logger.error("Screenshot alınamadı: {}", e.getMessage());
        return null;
    }
}
```

## Docker Sorunları

### Container Build Sorunları

**Sorun**: Docker image build edilemiyor.

**Çözüm**:
```dockerfile
# Multi-stage build kullanın
FROM maven:3.9-openjdk-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM mcr.microsoft.com/playwright/java:v1.54.0-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/src/test/resources ./src/test/resources

# Browser'ları yükle
RUN npx playwright install --with-deps

CMD ["java", "-jar", "app.jar"]
```

### Container Runtime Sorunları

**Sorun**: Container içinde testler çalışmıyor.

**Çözüm**:
```bash
# Docker run parametrelerini kontrol edin
docker run --rm \
  --shm-size=2g \
  -e BROWSER_HEADLESS=true \
  -e DISPLAY=:99 \
  -v $(pwd)/reports:/app/reports \
  playwright-tests

# Docker compose ile
version: '3.8'
services:
  tests:
    build: .
    shm_size: '2gb'
    environment:
      - BROWSER_HEADLESS=true
    volumes:
      - ./reports:/app/reports
```

## Genel Debugging İpuçları

### Logging Seviyesini Artırma

```properties
# log4j2.xml
<Logger name="com.starlettech" level="DEBUG"/>
<Logger name="com.microsoft.playwright" level="DEBUG"/>
```

### Test Debug Modu

```java
// Debug için headless'ı kapatın
@Test
public void debugTest() {
    System.setProperty("browser.headless", "false");
    System.setProperty("browser.slowmo", "1000");
    
    // Test kodunuz
}
```

### Network Monitoring

```java
// Network isteklerini logla
page.onRequest(request -> 
    logger.info("Request: {} {}", request.method(), request.url()));

page.onResponse(response -> 
    logger.info("Response: {} {} {}", response.status(), response.url(), response.statusText()));
```

---

**Framework Dokümantasyonu Tamamlandı**

Bu dokümantasyon seti, Playwright Test Otomasyon Framework'ünün kapsamlı kullanım kılavuzunu oluşturmaktadır. Her bölüm, framework'ün farklı yönlerini detaylı olarak açıklamakta ve pratik örnekler sunmaktadır.

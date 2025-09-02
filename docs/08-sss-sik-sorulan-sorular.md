# Playwright Test Otomasyon Framework'ü - SSS (Sık Sorulan Sorular)

## 📋 İçindekiler
- [Genel Sorular](#genel-sorular)
- [Kurulum ve Konfigürasyon](#kurulum-ve-konfigürasyon)
- [Test Yazma](#test-yazma)
- [Test Çalıştırma](#test-çalıştırma)
- [Performans ve Optimizasyon](#performans-ve-optimizasyon)
- [Hata Ayıklama](#hata-ayıklama)
- [CI/CD ve DevOps](#cicd-ve-devops)

## Genel Sorular

### ❓ Framework hangi teknolojileri kullanıyor?
**Cevap**: Framework temel olarak şu teknolojileri kullanır:
- **Java 21**: Ana programlama dili
- **Playwright 1.54.0**: Browser otomasyon motoru
- **TestNG 7.11.0**: Test framework'ü
- **Maven**: Proje yönetimi ve build tool
- **ReportPortal**: Gelişmiş raporlama
- **Log4j**: Loglama sistemi

### ❓ Hangi browser'ları destekliyor?
**Cevap**: Framework şu browser'ları destekler:
- ✅ **Chromium** (varsayılan)
- ✅ **Firefox**
- ✅ **Safari/WebKit** (macOS'ta)
- ✅ **Microsoft Edge** (Chromium tabanlı)

### ❓ Framework'ün diğer araçlardan farkı nedir?
**Cevap**: Ana avantajları:
- **Hızlı ve Kararlı**: Playwright'ın native performansı
- **Çoklu Platform**: Windows, macOS, Linux desteği
- **Modern Web Desteği**: SPA, PWA, modern JavaScript
- **API + UI**: Hibrit test yaklaşımı
- **Kolay Bakım**: Page Object Model ve modüler yapı

### ❓ Lisans durumu nedir?
**Cevap**: Framework açık kaynak bileşenler kullanır:
- Playwright: Apache 2.0 License
- TestNG: Apache 2.0 License
- Framework kodu: Şirket içi kullanım

## Kurulum ve Konfigürasyon

### ❓ Minimum sistem gereksinimleri nelerdir?
**Cevap**:
- **Java**: 21 veya üzeri
- **RAM**: Minimum 4GB, önerilen 8GB+
- **Disk**: 2GB boş alan
- **İşletim Sistemi**: Windows 10+, macOS 10.15+, Ubuntu 18.04+

### ❓ Java 8 veya 11 ile çalışır mı?
**Cevap**: Hayır, framework Java 21 gerektirir. Bunun nedenleri:
- Modern Java özelliklerinin kullanımı (Records, Pattern Matching)
- Playwright'ın Java 21 optimizasyonları
- Performans iyileştirmeleri

### ❓ Kurulum sırasında "Browser not found" hatası alıyorum?
**Cevap**: Browser'ları manuel olarak yükleyin:
```bash
# Tüm browser'ları yükle
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"

# Sadece Chromium yükle
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"

# Force install (sorun devam ederse)
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --force"
```

### ❓ Proxy arkasında nasıl çalıştırırım?
**Cevap**: Maven proxy ayarlarını yapın:
```bash
# Maven komutlarında proxy belirtin
mvn clean install -Dhttp.proxyHost=proxy.company.com -Dhttp.proxyPort=8080

# Veya ~/.m2/settings.xml dosyasında proxy ayarlayın
<proxies>
    <proxy>
        <host>proxy.company.com</host>
        <port>8080</port>
    </proxy>
</proxies>
```

### ❓ Farklı ortamlar için konfigürasyon nasıl yapılır?
**Cevap**: Environment-specific properties dosyaları kullanın:
```bash
# Test ortamı için
mvn test -Denvironment=TEST

# Staging ortamı için
mvn test -Denvironment=STAGING

# Konfigürasyon dosyaları
src/test/resources/config/
├── dev-config.properties
├── test-config.properties
└── staging-config.properties
```

## Test Yazma

### ❓ İlk testimi nasıl yazarım?
**Cevap**: Basit bir UI test örneği:
```java
public class MyFirstTest extends BaseTest {
    
    @Test(groups = {"smoke"})
    public void testLogin() {
        LoginPage loginPage = new LoginPage();
        HomePage homePage = new HomePage();
        
        loginPage.navigateToLoginPage();
        loginPage.login("testuser", "password");
        
        Assert.assertTrue(homePage.isUserLoggedIn());
    }
}
```

### ❓ Page Object nasıl oluştururum?
**Cevap**: BasePage'den türeterek:
```java
public class LoginPage extends BasePage {
    
    private static final String USERNAME_INPUT = "#username";
    private static final String PASSWORD_INPUT = "#password";
    private static final String LOGIN_BUTTON = "#login-btn";
    
    public void login(String username, String password) {
        type(USERNAME_INPUT, username);
        type(PASSWORD_INPUT, password);
        click(LOGIN_BUTTON);
    }
}
```

### ❓ API testleri nasıl yazarım?
**Cevap**: BaseApiTest'den türeterek:
```java
public class UserApiTest extends BaseApiTest {
    
    @Test
    public void testGetUsers() {
        UserApiClient client = new UserApiClient();
        List<User> users = client.getAllUsers();
        
        Assert.assertNotNull(users);
        Assert.assertTrue(users.size() > 0);
    }
}
```

### ❓ Test verilerini nasıl yönetirim?
**Cevap**: JSON dosyaları kullanın:
```json
// src/test/resources/testdata/users.json
{
  "validUser": {
    "username": "testuser1",
    "password": "password123"
  }
}
```

```java
// Test'te kullanım
JsonNode userData = testDataReader.getUserData("validUser");
String username = userData.get("username").asText();
```

### ❓ Dynamic element'leri nasıl handle ederim?
**Cevap**: Template locator'lar kullanın:
```java
private static final String USER_MENU_TEMPLATE = "[data-user='%s'] .menu";

public void clickUserMenu(String username) {
    String locator = String.format(USER_MENU_TEMPLATE, username);
    click(locator);
}
```

## Test Çalıştırma

### ❓ Testleri nasıl çalıştırırım?
**Cevap**: Maven komutları:
```bash
# Tüm testler
mvn test

# Belirli grup
mvn test -Dgroups=smoke

# Belirli sınıf
mvn test -Dtest=LoginTests

# Belirli method
mvn test -Dtest=LoginTests#testValidLogin

# Suite dosyası ile
mvn test -DsuiteXmlFile=src/test/resources/suites/ui-smoke.xml
```

### ❓ Paralel test nasıl çalıştırırım?
**Cevap**: TestNG suite'inde parallel ayarı:
```xml
<suite name="Parallel Tests" parallel="methods" thread-count="3">
    <test name="UI Tests">
        <classes>
            <class name="starlettech.tests.ui.LoginTests"/>
        </classes>
    </test>
</suite>
```

### ❓ Headless modda nasıl çalıştırırım?
**Cevap**: Browser ayarını değiştirin:
```bash
# Headless mode
mvn test -Dbrowser.headless=true

# Headed mode (debug için)
mvn test -Dbrowser.headless=false
```

### ❓ Farklı browser'da nasıl çalıştırırım?
**Cevap**: Browser type parametresi:
```bash
# Firefox ile
mvn test -Dbrowser.type=firefox

# WebKit ile
mvn test -Dbrowser.type=webkit

# Chromium ile (varsayılan)
mvn test -Dbrowser.type=chromium
```

### ❓ Test sonuçlarını nerede görebilirim?
**Cevap**: Raporlar şu konumlarda oluşur:
- **TestNG HTML**: `target/surefire-reports/index.html`
- **Screenshots**: `screenshots/` klasörü
- **Videos**: `videos/` klasörü (etkinse)
- **ReportPortal**: Web arayüzü (yapılandırılmışsa)

## Performans ve Optimizasyon

### ❓ Testlerim çok yavaş çalışıyor, nasıl hızlandırabilirim?
**Cevap**: Optimizasyon teknikleri:
```java
// 1. Wait stratejisini optimize edin
page.waitForSelector(selector, new Page.WaitForSelectorOptions()
    .setTimeout(5000)); // Kısa timeout

// 2. Page load stratejisini değiştirin
page.navigate(url, new Page.NavigateOptions()
    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

// 3. Paralel yürütmeyi artırın
<suite parallel="methods" thread-count="5">
```

### ❓ Memory kullanımı çok yüksek, nasıl azaltabilirim?
**Cevap**: Memory optimizasyonu:
```bash
# JVM heap ayarları
export MAVEN_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"

# Browser resource sınırlaması
--max-old-space-size=1024
--disable-dev-shm-usage
```

### ❓ Çok fazla browser instance açılıyor?
**Cevap**: Resource yönetimini kontrol edin:
```java
@AfterMethod
public void tearDown() {
    try {
        // Test işlemleri
    } finally {
        // Her zaman cleanup yapın
        PlaywrightManager.cleanup();
    }
}
```

## Hata Ayıklama

### ❓ Test başarısız oluyor ama neden anlamıyorum?
**Cevap**: Debug teknikleri:
```java
// 1. Headless'ı kapatın
System.setProperty("browser.headless", "false");

// 2. Slow motion ekleyin
System.setProperty("browser.slowmo", "1000");

// 3. Screenshot alın
ScreenshotUtils.takeScreenshot("debug_screenshot");

// 4. Logging seviyesini artırın
<Logger name="com.starlettech" level="DEBUG"/>
```

### ❓ Element bulunamıyor hatası alıyorum?
**Cevap**: Element bulma stratejileri:
```java
// 1. Wait ekleyin
waitForElement(selector);

// 2. Locator'ı kontrol edin
// ❌ Kötü: "button"
// ✅ İyi: "[data-testid='submit-button']"

// 3. Multiple locator deneyin
public void clickSubmit() {
    String[] selectors = {"#submit", ".submit-btn", "[type='submit']"};
    for (String selector : selectors) {
        if (isVisible(selector)) {
            click(selector);
            return;
        }
    }
    throw new RuntimeException("Submit button bulunamadı");
}
```

### ❓ API testlerinde authentication sorunu yaşıyorum?
**Cevap**: Auth token yönetimi:
```java
// 1. Token'ın doğru ayarlandığını kontrol edin
@BeforeMethod
public void setUp() {
    String token = authApiClient.login("admin", "password");
    ApiRequestManager.setAuthToken(token);
}

// 2. Token'ın header'da gönderildiğini doğrulayın
Map<String, String> headers = ApiRequestManager.getAuthHeaders();
logger.info("Auth headers: {}", headers);
```

## CI/CD ve DevOps

### ❓ Jenkins'te nasıl çalıştırırım?
**Cevap**: Jenkins pipeline örneği:
```groovy
pipeline {
    agent any
    
    stages {
        stage('Test') {
            steps {
                sh 'mvn clean test -Dbrowser.headless=true'
            }
        }
        
        stage('Reports') {
            steps {
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/surefire-reports',
                    reportFiles: 'index.html',
                    reportName: 'Test Report'
                ])
            }
        }
    }
}
```

### ❓ Docker'da nasıl çalıştırırım?
**Cevap**: Docker Compose kullanın:
```bash
# Docker container'ı çalıştır
docker-compose up --build

# Sadece testleri çalıştır
docker-compose run tests mvn test

# Belirli suite çalıştır
docker-compose run tests mvn test -DsuiteXmlFile=ui-smoke.xml
```

### ❓ GitHub Actions'ta nasıl çalıştırırım?
**Cevap**: Workflow örneği:
```yaml
name: Test Automation

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Run tests
      run: mvn clean test -Dbrowser.headless=true
    
    - name: Upload reports
      uses: actions/upload-artifact@v3
      with:
        name: test-reports
        path: target/surefire-reports/
```

### ❓ Test sonuçlarını Slack'e nasıl gönderebilirim?
**Cevap**: Webhook kullanın:
```java
public class SlackNotifier {
    
    public static void sendTestResults(String message) {
        String webhookUrl = System.getenv("SLACK_WEBHOOK_URL");
        
        // HTTP POST ile Slack'e mesaj gönder
        Map<String, Object> payload = Map.of(
            "text", message,
            "channel", "#test-results"
        );
        
        // HTTP client ile gönderim
    }
}
```

### ❓ Nightly testleri nasıl schedule ederim?
**Cevap**: Cron job veya CI/CD scheduler:
```bash
# Crontab örneği (her gece 02:00'da)
0 2 * * * cd /path/to/project && mvn test -DsuiteXmlFile=nightly-tests.xml

# Jenkins cron syntax
H 2 * * * // Her gece 2 civarında
```

---

## 🤝 Daha Fazla Yardım

Bu SSS'de cevabını bulamadığınız sorular için:

- **📧 Email**: support@starlettech.com
- **💬 Slack**: #playwright-framework
- **📋 GitHub Issues**: Bug report ve feature request
- **📚 Dokümantasyon**: [Ana Dokümantasyon](index.md)

**Başarılı testler! 🚀**

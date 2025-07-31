# Playwright Test Automation Framework

Bu proje, Java, TestNG ve Microsoft Playwright kullanarak geliştirilmiş kapsamlı bir test otomasyon framework'üdür. UI ve API testlerini destekler ve ReportPortal entegrasyonu ile gelişmiş raporlama sunar.

## 🚀 Özellikler

- **Multi-Browser Support**: Chromium, Firefox, WebKit, Chrome, Edge
- **Parallel Execution**: TestNG ile paralel test çalıştırma
- **API Testing**: RESTful API testleri için kapsamlı destek
- **Hybrid Testing**: UI ve API testlerinin entegrasyonu
- **ReportPortal Integration**: Gelişmiş test raporlama
- **Screenshot & Video Recording**: Test başarısızlıklarında otomatik ekran görüntüsü
- **Page Object Model**: Sürdürülebilir test yapısı
- **Configuration Management**: Çoklu ortam desteği
- **Logging**: Log4j2 ile kapsamlı loglama
- **Test Data Management**: JSON tabanlı test verisi yönetimi

## 📁 Proje Yapısı

```
core-playwright/
├── src/main/java/com/starlettech/
│   ├── config/          # Konfigürasyon sınıfları
│   ├── core/            # Çekirdek framework sınıfları
│   ├── utils/           # Yardımcı sınıflar
│   ├── listeners/       # TestNG dinleyicileri
│   ├── annotations/     # Özel annotation'lar
│   └── enums/           # Enum sınıfları
├── src/test/java/starlettech/
│   ├── pages/           # Page Object sınıfları
│   ├── api/             # API client'ları ve modelleri
│   └── tests/           # Test sınıfları
└── src/test/resources/
    ├── testdata/        # Test verileri
    └── suites/          # TestNG suite dosyaları
```

## 🛠️ Kurulum

### Gereksinimler
- Java 21+
- Maven 3.6+
- Node.js (Playwright için)

### Adımlar
1. Projeyi klonlayın:
```bash
git clone <repository-url>
cd core-playwright
```

2. Maven bağımlılıklarını yükleyin:
```bash
mvn clean install
```

3. Playwright browser'ları yükleyin:
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

## 🏃‍♂️ Testleri Çalıştırma

### Tüm testleri çalıştırma:
```bash
mvn test
```

### Belirli suite'i çalıştırma:
```bash
mvn test -DsuiteXmlFile=src/test/resources/suites/ui-smoke.xml
```

### Belirli ortamda çalıştırma:
```bash
mvn test -Denvironment=TEST
```

### Headless modda çalıştırma:
```bash
mvn test -Dbrowser.headless=true
```

### Belirli browser ile çalıştırma:
```bash
mvn test -Dbrowser.type=FIREFOX
```

## ⚙️ Konfigürasyon

### Browser Konfigürasyonu
`src/test/resources/testdata/test-config.properties` dosyasında browser ayarlarını yapılandırabilirsiniz:

```properties
browser.type=CHROMIUM
browser.headless=true
browser.timeout=30000
browser.viewport=1920x1080
```

### Ortam Konfigürasyonu
Farklı ortamlar için URL'leri ayarlayın:

```properties
base.url.dev=http://localhost:3000
base.url.test=https://test.example.com
base.url.staging=https://staging.example.com
```

### ReportPortal Konfigürasyonu
`src/test/resources/reportportal.properties` dosyasında ReportPortal ayarlarını yapın:

```properties
rp.endpoint=http://localhost:8080
rp.project=your-project
rp.launch=Playwright Tests
```

## 📝 Test Yazma

### Page Object Örneği:
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

### Test Sınıfı Örneği:
```java
@Browser(BrowserType.CHROMIUM)
public class LoginTests extends BaseTest {
    
    @Test(groups = {"smoke", "ui"})
    @TestInfo(description = "Valid login test", author = "Test Engineer")
    public void testValidLogin() {
        LoginPage loginPage = new LoginPage();
        loginPage.navigateTo(testConfig.getBaseUrl());
        loginPage.login("testuser", "password123");
        // Assertions...
    }
}
```

## 🔧 Utility Sınıfları

Framework aşağıdaki utility sınıflarını içerir:
- **WaitUtils**: Bekleme işlemleri
- **ElementUtils**: Element işlemleri
- **ScreenshotUtils**: Ekran görüntüsü alma
- **TestDataReader**: Test verisi okuma
- **ApiUtils**: API test yardımcıları
- **JsonUtils**: JSON işlemleri

## 📊 Raporlama

- **TestNG Reports**: `target/surefire-reports/`
- **Screenshots**: `screenshots/` klasörü
- **Videos**: `videos/` klasörü (etkinleştirilirse)
- **Traces**: `traces/` klasörü (etkinleştirilirse)
- **ReportPortal**: Web tabanlı gelişmiş raporlama

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add amazing feature'`)
4. Branch'inizi push edin (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

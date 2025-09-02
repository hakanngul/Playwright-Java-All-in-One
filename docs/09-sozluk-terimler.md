# Playwright Test Otomasyon Framework'ü - Sözlük ve Terimler

## 📋 İçindekiler
- [Framework Terimleri](#framework-terimleri)
- [Test Otomasyon Terimleri](#test-otomasyon-terimleri)
- [Playwright Terimleri](#playwright-terimleri)
- [Java ve Maven Terimleri](#java-ve-maven-terimleri)
- [CI/CD Terimleri](#cicd-terimleri)
- [Kısaltmalar](#kısaltmalar)

## Framework Terimleri

### **API Client**
API endpoint'leri ile iletişim kurmak için kullanılan sınıflar. Her API servisi için ayrı client sınıfı oluşturulur.
```java
public class UserApiClient extends BaseApiClient {
    public List<User> getAllUsers() { ... }
}
```

### **Base Classes (Temel Sınıflar)**
Framework'ün temel işlevselliğini sağlayan ve diğer sınıflar tarafından extend edilen sınıflar.
- `BaseTest`: UI testleri için
- `BaseApiTest`: API testleri için  
- `BasePage`: Page Object'ler için

### **Configuration Management (Konfigürasyon Yönetimi)**
Framework ayarlarının merkezi olarak yönetildiği sistem. Properties dosyaları ve environment variable'lar kullanılır.

### **Page Object Model (POM)**
Web sayfalarını sınıflar olarak modelleyen tasarım deseni. Her sayfa için ayrı bir sınıf oluşturulur.
```java
public class LoginPage extends BasePage {
    private static final String USERNAME_INPUT = "#username";
    public void login(String username, String password) { ... }
}
```

### **Test Data Management**
Test verilerinin JSON, Properties veya CSV dosyalarında saklanması ve testlerde kullanılması.

### **Thread Safety**
Paralel test yürütmesinde thread'ler arası veri paylaşımının güvenli olması. ThreadLocal kullanımı ile sağlanır.

## Test Otomasyon Terimleri

### **Assertion**
Test sonucunun beklenen değerle karşılaştırılması. TestNG'nin Assert sınıfı kullanılır.
```java
Assert.assertTrue(homePage.isUserLoggedIn(), "Kullanıcı giriş yapmış olmalı");
```

### **Data Provider**
TestNG'de test methoduna farklı veri setleri sağlayan mekanizma.
```java
@DataProvider(name = "loginData")
public Object[][] loginDataProvider() { ... }
```

### **Flaky Test**
Bazen başarılı bazen başarısız olan kararsız test. Genellikle timing veya environment sorunlarından kaynaklanır.

### **Headless Mode**
Browser'ın grafik arayüzü olmadan çalışması. CI/CD ortamlarında performans için kullanılır.

### **Hybrid Testing**
UI ve API testlerinin birlikte kullanıldığı test yaklaşımı.

### **Regression Testing**
Yeni değişikliklerin mevcut işlevselliği bozup bozmadığını kontrol eden testler.

### **Smoke Testing**
Uygulamanın temel işlevselliklerinin çalıştığını doğrulayan hızlı testler.

### **Test Suite**
Birlikte çalıştırılan test grupları. TestNG XML dosyalarında tanımlanır.

## Playwright Terimleri

### **Browser Context**
Browser instance'ı içinde izole edilmiş bir oturum. Cookies, local storage gibi veriler context'e özeldir.

### **Browser Instance**
Çalışan browser süreci. Chromium, Firefox veya WebKit olabilir.

### **Element Locator**
Web sayfasındaki elementleri bulmak için kullanılan selector'lar.
```java
private static final String LOGIN_BUTTON = "#login-btn";  // ID selector
private static final String ERROR_MSG = ".error-message"; // Class selector
```

### **Page**
Browser context içindeki bir web sayfası. JavaScript execution, navigation gibi işlemler page üzerinde yapılır.

### **Playwright**
Microsoft tarafından geliştirilen modern web otomasyon kütüphanesi.

### **Selector**
HTML elementlerini seçmek için kullanılan CSS veya XPath ifadeleri.

### **Trace**
Test yürütme sırasında browser aktivitelerinin kaydedilmesi. Debug için kullanılır.

### **Viewport**
Browser penceresinin görünen alanının boyutları (genişlik x yükseklik).

### **Wait Strategy**
Element'lerin yüklenmesini beklemek için kullanılan stratejiler.
- `waitForSelector`: Element görünene kadar bekle
- `waitForLoadState`: Sayfa yüklenene kadar bekle

## Java ve Maven Terimleri

### **Annotation**
Java sınıf, method veya field'lara metadata eklemek için kullanılan işaretler.
```java
@Test(groups = {"smoke"})
@BeforeMethod
@TestInfo(description = "Test açıklaması")
```

### **Dependency**
Projenin çalışması için gerekli olan harici kütüphaneler. pom.xml'de tanımlanır.

### **Maven**
Java projelerinin build, dependency management ve lifecycle yönetimi için kullanılan araç.

### **Maven Lifecycle**
Maven'in proje işleme aşamaları: compile, test, package, install, deploy.

### **POM (Project Object Model)**
Maven projesinin konfigürasyon dosyası (pom.xml).

### **Singleton Pattern**
Bir sınıftan sadece bir instance oluşturulmasını sağlayan tasarım deseni.
```java
public class TestConfig {
    private static TestConfig instance;
    public static TestConfig getInstance() { ... }
}
```

### **ThreadLocal**
Her thread için ayrı değer saklayan Java sınıfı. Paralel testlerde kullanılır.

## CI/CD Terimleri

### **Artifact**
Build sürecinde üretilen dosyalar (JAR, test reports, screenshots).

### **Build Pipeline**
Kod değişikliklerinin otomatik olarak test edilip deploy edildiği süreç.

### **Container**
Uygulamanın tüm bağımlılıklarıyla birlikte paketlendiği izole ortam (Docker).

### **Continuous Integration (CI)**
Kod değişikliklerinin sürekli olarak ana branch'e entegre edilmesi.

### **Continuous Deployment (CD)**
Başarılı testlerden geçen kodun otomatik olarak production'a deploy edilmesi.

### **Docker**
Uygulamaları container'larda çalıştırmak için kullanılan platform.

### **Docker Compose**
Çoklu container uygulamalarını tanımlamak ve çalıştırmak için araç.

### **Environment**
Uygulamanın çalıştığı ortam (dev, test, staging, production).

### **Jenkins**
Açık kaynak CI/CD automation server'ı.

### **Pipeline**
CI/CD sürecindeki adımların sıralı olarak tanımlandığı workflow.

## Kısaltmalar

### **API**
**Application Programming Interface** - Uygulamalar arası iletişim arayüzü

### **CI/CD**
**Continuous Integration/Continuous Deployment** - Sürekli entegrasyon/sürekli dağıtım

### **CSS**
**Cascading Style Sheets** - Web sayfası stil tanımlama dili

### **DOM**
**Document Object Model** - HTML dokümanının programatik temsili

### **HTML**
**HyperText Markup Language** - Web sayfası işaretleme dili

### **HTTP/HTTPS**
**HyperText Transfer Protocol (Secure)** - Web iletişim protokolü

### **IDE**
**Integrated Development Environment** - Entegre geliştirme ortamı

### **JSON**
**JavaScript Object Notation** - Veri değişim formatı

### **JVM**
**Java Virtual Machine** - Java sanal makinesi

### **REST**
**Representational State Transfer** - Web servisleri mimari stili

### **SPA**
**Single Page Application** - Tek sayfa uygulaması

### **SQL**
**Structured Query Language** - Veritabanı sorgulama dili

### **UI**
**User Interface** - Kullanıcı arayüzü

### **URL**
**Uniform Resource Locator** - Web adresi

### **XML**
**eXtensible Markup Language** - Genişletilebilir işaretleme dili

### **XPath**
**XML Path Language** - XML/HTML elementlerini seçmek için dil

## Framework Özel Terimler

### **ApiRequestManager**
API isteklerini yöneten ve retry mekanizması sağlayan sınıf.

### **BrowserConfig**
Browser ayarlarını yöneten konfigürasyon sınıfı.

### **PlaywrightManager**
Playwright yaşam döngüsünü yöneten merkezi sınıf.

### **ReportPortalListener**
TestNG testlerinin ReportPortal'a raporlanmasını sağlayan listener.

### **ScreenshotUtils**
Ekran görüntüsü alma işlemlerini yöneten utility sınıfı.

### **TestDataReader**
Test verilerini JSON/Properties dosyalarından okuyan sınıf.

### **TestInfo Annotation**
Test metadata bilgilerini tanımlamak için kullanılan custom annotation.

### **WaitUtils**
Çeşitli bekleme stratejilerini sağlayan utility sınıfı.

## Test Seviyeleri

### **Unit Test**
Tek bir kod birimini (method, sınıf) test eden testler.

### **Integration Test**
Farklı bileşenlerin birlikte çalışmasını test eden testler.

### **System Test**
Tüm sistemin end-to-end test edilmesi.

### **Acceptance Test**
İş gereksinimlerinin karşılandığını doğrulayan testler.

## Test Türleri

### **Functional Test**
Uygulamanın işlevsel gereksinimlerini test eden testler.

### **Non-Functional Test**
Performans, güvenlik, kullanılabilirlik gibi non-functional gereksinimleri test eden testler.

### **Positive Test**
Geçerli verilerle beklenen sonuçları test eden testler.

### **Negative Test**
Geçersiz verilerle hata durumlarını test eden testler.

## Metrikler ve KPI'lar

### **Code Coverage**
Test edilen kod satırlarının toplam kod satırlarına oranı.

### **Defect Density**
Birim kod başına düşen hata sayısı.

### **Test Execution Time**
Testlerin çalışma süresi.

### **Pass Rate**
Başarılı testlerin toplam testlere oranı.

### **Flakiness Rate**
Kararsız testlerin oranı.

---

## 📚 Ek Kaynaklar

Bu terimler hakkında daha detaylı bilgi için:

- **[Playwright Dokümantasyonu](https://playwright.dev/java/)**
- **[TestNG Dokümantasyonu](https://testng.org/doc/)**
- **[Maven Dokümantasyonu](https://maven.apache.org/guides/)**
- **[Framework Ana Dokümantasyonu](index.md)**

**Terimler sürekli güncellenmektedir. Yeni terim önerileri için lütfen iletişime geçin! 📖**

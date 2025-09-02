# Playwright Test Otomasyon Framework'ü - Kurulum ve Konfigürasyon

## 📋 İçindekiler
- [Sistem Gereksinimleri](#sistem-gereksinimleri)
- [Kurulum Adımları](#kurulum-adımları)
- [Konfigürasyon Dosyaları](#konfigürasyon-dosyaları)
- [Ortam Ayarları](#ortam-ayarları)
- [IDE Konfigürasyonu](#ide-konfigürasyonu)
- [Docker Kurulumu](#docker-kurulumu)

## Sistem Gereksinimleri

### Minimum Gereksinimler
| Bileşen | Minimum Versiyon | Önerilen Versiyon |
|---------|------------------|-------------------|
| **Java** | 21 | 21+ |
| **Maven** | 3.6.0 | 3.9.0+ |
| **Node.js** | 16.0 | 18.0+ |
| **RAM** | 4 GB | 8 GB+ |
| **Disk Alanı** | 2 GB | 5 GB+ |

### Desteklenen İşletim Sistemleri
- ✅ Windows 10/11
- ✅ macOS 10.15+
- ✅ Ubuntu 18.04+
- ✅ CentOS 7+

### Desteklenen Browser'lar
- ✅ Chromium (varsayılan)
- ✅ Firefox
- ✅ Safari (WebKit - macOS'ta)

## Kurulum Adımları

### 1. Java Kurulumu
```bash
# Java versiyonunu kontrol edin
java -version

# Java 21 kurulu değilse, Oracle JDK veya OpenJDK kurun
# Windows için: https://adoptium.net/
# macOS için: brew install openjdk@21
# Ubuntu için: sudo apt install openjdk-21-jdk
```

### 2. Maven Kurulumu
```bash
# Maven versiyonunu kontrol edin
mvn -version

# Maven kurulu değilse:
# Windows için: https://maven.apache.org/download.cgi
# macOS için: brew install maven
# Ubuntu için: sudo apt install maven
```

### 3. Node.js Kurulumu (Playwright için gerekli)
```bash
# Node.js versiyonunu kontrol edin
node --version

# Node.js kurulu değilse:
# https://nodejs.org/ adresinden indirin
```

### 4. Projeyi Klonlama
```bash
# Projeyi klonlayın
git clone <repository-url>
cd core-playwright

# Proje yapısını kontrol edin
ls -la
```

### 5. Maven Bağımlılıklarını Yükleme
```bash
# Tüm bağımlılıkları indirin ve projeyi derleyin
mvn clean install

# Test derleme işlemini kontrol edin
mvn test-compile
```

### 6. Playwright Browser'larını Yükleme
```bash
# Playwright browser'larını yükleyin
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"

# Alternatif olarak:
npx playwright install
```

### 7. Kurulum Doğrulama
```bash
# Basit bir test çalıştırarak kurulumu doğrulayın
mvn test -Dtest=starlettech.tests.ui.LoginTests#testValidLogin
```

## Konfigürasyon Dosyaları

### Ana Konfigürasyon Dosyası
**Konum**: `src/main/resources/default-config.properties`

```properties
# Temel Ayarlar
environment=TEST
base.url=https://example.com

# Browser Konfigürasyonu
browser.type=CHROMIUM
browser.headless=true
browser.timeout=30000
browser.viewport=1920x1080
browser.args=--disable-web-security,--disable-features=VizDisplayCompositor

# Bekleme Ayarları
wait.implicit=10
wait.explicit=30
wait.pageload=60

# Screenshot Ayarları
screenshot.on.failure=true
screenshot.path=screenshots

# Retry Ayarları
retry.enabled=true
retry.count=2

# Test Data Ayarları
testdata.path=src/test/resources/testdata

# Paralel Yürütme
parallel.execution=false
thread.count=1
```

### Test Ortamı Konfigürasyonu
**Konum**: `src/test/resources/testdata/test-config.properties`

```properties
# Test Ortamı Ayarları
test.environment=TEST
test.base.url=https://test.example.com
test.api.base.url=https://api-test.example.com

# API Konfigürasyonu
api.timeout=30000
api.retry.count=3
api.content.type=application/json
api.logging.enabled=true
api.auth.endpoint=/api/auth
api.user.endpoint=/api/users

# Video Kayıt
browser.video.enabled=false
browser.video.path=videos

# Trace Kayıt
browser.tracing.enabled=false
browser.trace.path=traces

# İndirme Klasörü
browser.download.path=downloads
```

### ReportPortal Konfigürasyonu
**Konum**: `src/test/resources/reportportal.properties`

```properties
# ReportPortal Bağlantı Ayarları
rp.endpoint=http://localhost:8080
rp.uuid=your-uuid-here
rp.project=default_personal
rp.launch=Playwright Tests

# ReportPortal Ayarları
rp.mode=DEFAULT
rp.enable=false
rp.description=Automated tests execution with Playwright
rp.tags=playwright;automation;regression
rp.skipped.issue=true
rp.batch.size.logs=20
rp.reporting.timeout=5

# Loglama Ayarları
rp.logging.level=INFO
rp.convertimage=true
rp.truncate.fields=true
```

### Log4j Konfigürasyonu
**Konum**: `src/main/resources/log4j2.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <!-- Console Appender -->
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        
        <!-- File Appender -->
        <File name="FileAppender" fileName="logs/playwright-tests.log">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </File>
        
        <!-- Error File Appender -->
        <File name="ErrorFileAppender" fileName="logs/playwright-errors.log">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
            <ThresholdFilter level="ERROR" onMatch="ACCEPT" onMismatch="DENY"/>
        </File>
    </Appenders>
    
    <Loggers>
        <Logger name="com.starlettech" level="INFO" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="FileAppender"/>
            <AppenderRef ref="ErrorFileAppender"/>
        </Logger>
        
        <Root level="WARN">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

## Ortam Ayarları

### Ortam Değişkenleri
Framework, aşağıdaki ortam değişkenlerini destekler:

```bash
# Browser Ayarları
export BROWSER_TYPE=chromium
export BROWSER_HEADLESS=true
export BROWSER_TIMEOUT=30000

# Test Ortamı
export ENVIRONMENT=TEST
export BASE_URL=https://test.example.com

# Paralel Yürütme
export PARALLEL_EXECUTION=true
export THREAD_COUNT=3

# Raporlama
export SCREENSHOT_ON_FAILURE=true
export VIDEO_RECORDING=false
export REPORT_PORTAL_ENABLE=false
```

### Ortam Bazlı Konfigürasyon
Farklı ortamlar için ayrı konfigürasyon dosyaları oluşturabilirsiniz:

```
src/test/resources/
├── config/
│   ├── dev-config.properties
│   ├── test-config.properties
│   ├── staging-config.properties
│   └── prod-config.properties
```

### Konfigürasyon Kullanımı
```bash
# Belirli ortam için test çalıştırma
mvn test -Denvironment=TEST
mvn test -Denvironment=STAGING
mvn test -Denvironment=PROD
```

## IDE Konfigürasyonu

### IntelliJ IDEA Ayarları

#### 1. Proje İçe Aktarma
1. `File` → `Open` → Proje klasörünü seçin
2. Maven projesini otomatik olarak tanıyacaktır
3. `Import Maven projects automatically` seçeneğini işaretleyin

#### 2. JDK Ayarları
1. `File` → `Project Structure` → `Project`
2. `Project SDK`: Java 21 seçin
3. `Project language level`: 21 seçin

#### 3. TestNG Plugin
1. `File` → `Settings` → `Plugins`
2. `TestNG` plugin'ini yükleyin
3. IDE'yi yeniden başlatın

#### 4. Run Configuration
```xml
<!-- TestNG Run Configuration -->
<configuration name="All Tests" type="TestNG">
    <module name="core-playwright"/>
    <option name="SUITE_NAME" value="src/test/resources/testng.xml"/>
    <option name="VM_PARAMETERS" value="-Denvironment=TEST"/>
</configuration>
```

### Eclipse Ayarları

#### 1. Proje İçe Aktarma
1. `File` → `Import` → `Existing Maven Projects`
2. Proje klasörünü seçin
3. `Import` butonuna tıklayın

#### 2. TestNG Plugin
1. `Help` → `Eclipse Marketplace`
2. "TestNG" arayın ve yükleyin
3. Eclipse'i yeniden başlatın

### VS Code Ayarları

#### 1. Gerekli Extension'lar
```json
{
    "recommendations": [
        "vscjava.vscode-java-pack",
        "vscjava.vscode-maven",
        "ms-vscode.test-adapter-converter"
    ]
}
```

#### 2. Workspace Ayarları
```json
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.test.defaultConfig": "testng",
    "maven.executable.path": "/usr/local/bin/mvn"
}
```

## Docker Kurulumu

### Docker Compose ile Çalıştırma

#### 1. Docker Compose Dosyası
**Konum**: `docker-compose.yml`

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
      - PARALLEL_EXECUTION=${PARALLEL_EXECUTION:-true}
      - THREAD_COUNT=${THREAD_COUNT:-2}
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

#### 2. Docker ile Çalıştırma
```bash
# Docker container'ı oluştur ve çalıştır
docker-compose up --build

# Sadece testleri çalıştır
docker-compose run playwright-tests mvn test

# Belirli suite'i çalıştır
docker-compose run playwright-tests mvn test -DsuiteXmlFile=src/test/resources/suites/ui-smoke.xml
```

### Dockerfile
```dockerfile
FROM mcr.microsoft.com/playwright/java:v1.54.0-jammy

WORKDIR /app

# Maven ve Java kurulumu
COPY pom.xml .
RUN mvn dependency:go-offline

# Proje dosyalarını kopyala
COPY src ./src

# Testleri çalıştır
CMD ["mvn", "test"]
```

## Kurulum Sorun Giderme

### Yaygın Sorunlar ve Çözümleri

#### 1. Java Version Sorunu
```bash
# Hata: "java.lang.UnsupportedClassVersionError"
# Çözüm: Java 21 kurulumunu kontrol edin
java -version
export JAVA_HOME=/path/to/java21
```

#### 2. Maven Bağımlılık Sorunu
```bash
# Hata: "Could not resolve dependencies"
# Çözüm: Maven cache'i temizleyin
mvn clean install -U
rm -rf ~/.m2/repository
```

#### 3. Playwright Browser Sorunu
```bash
# Hata: "Browser not found"
# Çözüm: Browser'ları yeniden yükleyin
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --force"
```

#### 4. Port Çakışması
```bash
# Hata: "Port already in use"
# Çözüm: Farklı port kullanın
export API_PORT=8081
```

---

**Sonraki Bölüm**: [Kullanım Kılavuzu](04-kullanim-kilavuzu.md)

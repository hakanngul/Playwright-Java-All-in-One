package com.starlettech.clients;


import com.epam.reportportal.testng.ReportPortalTestNGListener;
import com.fasterxml.jackson.core.type.TypeReference;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import com.starlettech.core.PlaywrightManager;
import com.starlettech.utils.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Listeners;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * BaseApiClient
 * - Playwright APIRequestContext tabanlı genel HTTP istemcisi
 * - Alt sınıflar spesifik API'ler için buradan kalıtım alır

 * Özellikler:
 *  - Base URL
 *  - Varsayılan header'lar (+ extra header override)
 *  - Bearer/Basic auth helper
 *  - Timeout
 *  - Retry (basit, idempotent isteklerde önerilir)
 *  - JSON parse yardımcıları

 * Not: Playwright RequestOptions map kabul etmez; header'lar tek tek setlenir.
 */
@Listeners(ReportPortalTestNGListener.class)
public abstract class BaseApiClient {

    protected final Logger logger = LogManager.getLogger(this.getClass());

    private final String baseUrl;
    private final Map<String, String> defaultHeaders;
    private final long defaultTimeoutMs;
    private final int retries;
    private final Duration retryBackoff;

    protected APIRequestContext api;

    public static class Config {
        private String baseUrl;
        private final Map<String, String> defaultHeaders = new LinkedHashMap<>();
        private long timeoutMs = 30_000;
        private int retries = 0;
        private Duration retryBackoff = Duration.ofMillis(500);

        public Config baseUrl(String baseUrl) {
            this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
            return this;
        }

        public Config addHeader(String key, String value) {
            this.defaultHeaders.put(key, value);
            return this;
        }

        /** JSON için makul varsayılanlar */
        public Config withJsonDefaults() {
            this.defaultHeaders.putIfAbsent("Content-Type", "application/json; charset=UTF-8");
            this.defaultHeaders.putIfAbsent("Accept", "application/json");
            return this;
        }

        public Config bearer(String token) {
            if (token != null && !token.isBlank()) {
                this.defaultHeaders.put("Authorization", "Bearer " + token);
            }
            return this;
        }

        public Config basic(String username, String passwordBase64OrPlain) {
            if (username != null) {
                String basic = passwordBase64OrPlain;
                // Plain geldiyse base64’a çevir:
                if (!passwordBase64OrPlain.contains(":") && !passwordBase64OrPlain.endsWith("=")) {
                    // Kullanıcı plain yerine direkt base64 de verebilir; zorlamıyoruz.
                }
                // Basic header’ı doğrudan setleyelim (çağıran doğru değeri verebilir)
                this.defaultHeaders.put("Authorization", "Basic " + passwordBase64OrPlain);
            }
            return this;
        }

        public Config timeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Config retries(int retries) {
            this.retries = Math.max(0, retries);
            return this;
        }

        public Config retryBackoff(Duration backoff) {
            this.retryBackoff = backoff == null ? Duration.ofMillis(500) : backoff;
            return this;
        }
    }

    protected BaseApiClient(Config cfg) {
        this.baseUrl = Objects.requireNonNull(cfg.baseUrl, "Base URL is required");
        this.defaultHeaders = Collections.unmodifiableMap(new LinkedHashMap<>(cfg.defaultHeaders));
        this.defaultTimeoutMs = cfg.timeoutMs;
        this.retries = cfg.retries;
        this.retryBackoff = cfg.retryBackoff;
        initApiContext();
    }

    private void initApiContext() {
        try {
            Playwright pw = PlaywrightManager.getPlaywright();
            if (pw == null) {
                PlaywrightManager.initializePlaywright();
                pw = PlaywrightManager.getPlaywright();
            }
            this.api = pw.request().newContext(
                    new APIRequest.NewContextOptions()
                            .setBaseURL(baseUrl)
                            .setTimeout(defaultTimeoutMs)
            );
            logger.debug("API context initialized: baseUrl={}, timeout={}ms", baseUrl, defaultTimeoutMs);
        } catch (Exception e) {
            logger.error("API context init failed: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize API context", e);
        }
    }

    /* ===================== Public HTTP Helpers ===================== */

    protected APIResponse get(String path) {
        return executeWithRetry("GET", () -> api.get(path));
    }

    protected APIResponse get(String path, Map<String, String> query) {
        String url = buildUrlWithParams(path, query);
        return executeWithRetry("GET", () -> api.get(url));
    }

    protected APIResponse delete(String path) {
        return executeWithRetry("DELETE", () -> api.delete(path));
    }

    protected APIResponse post(String path, Object body) {
        RequestOptions opts = buildJsonRequestOptions(body, null, defaultTimeoutMs);
        return executeWithRetry("POST", () -> api.post(path, opts));
    }

    protected APIResponse put(String path, Object body) {
        RequestOptions opts = buildJsonRequestOptions(body, null, defaultTimeoutMs);
        return executeWithRetry("PUT", () -> api.put(path, opts));
    }

    protected APIResponse patch(String path, Object body) {
        RequestOptions opts = buildJsonRequestOptions(body, null, defaultTimeoutMs);
        return executeWithRetry("PATCH", () -> api.patch(path, opts));
    }

    /* Overload: ekstra header/timeout ihtiyacı olan çağrılar için */
    protected APIResponse post(String path, Object body, Map<String, String> extraHeaders, Long timeoutMs) {
        RequestOptions opts = buildJsonRequestOptions(body, extraHeaders, timeoutMs == null ? defaultTimeoutMs : timeoutMs);
        return executeWithRetry("POST", () -> api.post(path, opts));
    }

    /* ===================== RequestOptions Builder ===================== */

    protected RequestOptions buildJsonRequestOptions(Object body, Map<String, String> extraHeaders, long timeoutMs) {
        Map<String, String> headers = new LinkedHashMap<>(defaultHeaders);
        if (extraHeaders != null && !extraHeaders.isEmpty()) headers.putAll(extraHeaders);

        RequestOptions options = RequestOptions.create();
        headers.forEach(options::setHeader);

        if (body != null) {
            options.setData(JsonUtils.toJsonString(body));
        }
        if (timeoutMs > 0) {
            options.setTimeout(timeoutMs);
        }
        return options;
    }

    /* ===================== Retry Wrapper ===================== */

    private APIResponse executeWithRetry(String method, SupplierWithApiResponse call) {
        int attempt = 0;
        APIResponse last = null;
        RuntimeException lastEx = null;

        while (attempt <= retries) {
            try {
                APIResponse resp = call.get();
                logResponse(method, resp);
                return resp;
            } catch (RuntimeException ex) {
                lastEx = ex;
                logger.warn("{} attempt {}/{} failed: {}", method, attempt + 1, retries + 1, ex.getMessage());
                // Bekleme (exponential backoff basit)
                try { Thread.sleep(retryBackoff.toMillis() * Math.max(1, attempt + 1)); } catch (InterruptedException ignored) {}
            }
            attempt++;
        }

        logger.error("{} failed after {} attempts", method, retries + 1);
        if (last != null) {
            logger.error("Last response status: {}", last.status());
        }
        throw (lastEx != null ? lastEx : new RuntimeException(method + " failed"));
    }

    @FunctionalInterface
    private interface SupplierWithApiResponse {
        APIResponse get();
    }

    /* ===================== Parsing & Validation ===================== */

    protected <T> T parse(APIResponse response, Class<T> type) {
        try {
            return JsonUtils.fromJsonString(response.text(), type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response to " + type.getSimpleName(), e);
        }
    }

    protected <T> T parse(APIResponse response, TypeReference<T> typeRef) {
        try {
            return JsonUtils.fromJsonString(response.text(), typeRef);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response to given TypeReference", e);
        }
    }

    protected void assertStatus(APIResponse response, int expected) {
        int actual = response.status();
        if (actual != expected) {
            String msg = String.format("Expected %d but got %d. Body: %s", expected, actual, safeBody(response));
            logger.error(msg);
            throw new RuntimeException(msg);
        }
    }

    protected boolean is2xx(APIResponse response) {
        int s = response.status();
        return s >= 200 && s < 300;
    }

    private String safeBody(APIResponse resp) {
        try { return resp.text(); } catch (Exception ignored) { return "<unreadable>"; }
    }

    /* ===================== Utils ===================== */

    protected String buildUrlWithParams(String path, Map<String, String> params) {
        if (params == null || params.isEmpty()) return path;
        StringBuilder sb = new StringBuilder(path);
        sb.append(path.contains("?") ? "&" : "?");
        Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> e = it.next();
            sb.append(encode(e.getKey())).append("=")
                    .append(encode(e.getValue()));
            if (it.hasNext()) sb.append("&");
        }
        return sb.toString();
    }

    private String encode(String v) {
        return v == null ? "" : URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private void logResponse(String method, APIResponse response) {
        if (logger.isDebugEnabled()) {
            logger.debug("{} {} -> status={} content-type={}",
                    method,
                    // Playwright APIResponse URL erişimi (bazı sürümlerde .url() var, yoksa loga yazmayabiliriz)
                    tryGetUrl(response),
                    response.status(),
                    response.headers().getOrDefault("content-type", "-")
            );
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Response body: {}", safeBody(response));
        }
    }

    private String tryGetUrl(APIResponse response) {
        try { return response.url(); } catch (Throwable t) { return "-"; }
    }

    /* ===================== Lifecycle ===================== */

    public void cleanup() {
        try {
            if (api != null) {
                api.dispose();
                logger.debug("API context disposed");
            }
        } catch (Exception e) {
            logger.warn("Cleanup error: {}", e.getMessage());
        }
    }
}

package com.starlettech.validator;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.testng.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.starlettech.core.PlaywrightManager;
import com.starlettech.utils.ElementUtils;

/**
 * Assertions
 * -------------
 * - Minimal, bağımsız, AAA düzenine uygun ASSERT yardımcıları.
 * - Harici matcher yok, TestNG Assert kullanır.
 * - Playwright API+UI testleri için pratik, okunabilir kısayollar.
 *
 * Kullanım (AAA):
 *   // Arrange ...
 *   // Act
 *   APIResponse resp = apiClient.createPostResponse(body);
 *   // Assert
 *   Assertions.assertStatus(resp, 201);
 *   Assertions.assertContentType(resp, "application/json");
 *   Assertions.assertJsonEquals(resp, "title", "Hello");
 *   Assertions.assertJsonExists(resp, "id");
 *
 *   // UI
 *   Assertions.assertVisible("#login");
 *   Assertions.assertTextEquals("#welcome", "Welcome!");
 *
 * Notlar:
 *   - JSON path "a.b[0].c" desteklenir.
 *   - Süre ölçümünü testte yapıp assertResponseTimeLe(...) ile doğrulayın.
 */
public final class Assertions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Assertions() {} // static util

    // =========================================================
    // ================ API ASSERTIONS (Playwright) ============
    // =========================================================

    /** HTTP durum kodu */
    public static void assertStatus(APIResponse response, int expected) {
        assertStatus(response, expected, "HTTP status assertion failed");
    }

    public static void assertStatus(APIResponse response, int expected, String message) {
        int actual = response.status();
        Assert.assertEquals(actual, expected,
                message + detail(": expected=", expected, " actual=", actual, " body=", bodySummary(response, 300)));
    }

    /** 2xx */
    public static void assert2xx(APIResponse response) {
        int s = response.status();
        Assert.assertTrue(s >= 200 && s < 300,
                "Expected 2xx" + detail(" but was=", s, " body=", bodySummary(response, 300)));
    }

    /** İçerik tipi (başlangıç uyumu: application/json, text/html vs.) */
    public static void assertContentType(APIResponse response, String expectedStartsWith) {
        assertHeaderContains(response, "content-type", expectedStartsWith);
    }

    /** Header eşitlik */
    public static void assertHeaderEquals(APIResponse response, String name, String expected) {
        String actual = header(response, name);
        Assert.assertNotNull(actual, "Header missing: " + name);
        Assert.assertEquals(actual, expected,
                "Header mismatch" + detail(" '", name, "' expected='", expected, "' actual='", actual, "'"));
    }

    /** Header contains */
    public static void assertHeaderContains(APIResponse response, String name, String expectedSubstring) {
        String actual = header(response, name);
        Assert.assertNotNull(actual, "Header missing: " + name);
        Assert.assertTrue(actual.toLowerCase().contains(Objects.toString(expectedSubstring, "").toLowerCase()),
                "Header should contain substring" + detail(" '", name, "' expected*='", expectedSubstring, "' actual='", actual, "'"));
    }

    /** Response body regex */
    public static void assertBodyMatches(APIResponse response, String regex) {
        String body = bodyTextSafe(response);
        Assert.assertTrue(body != null && body.matches(regex),
                "Body should match regex" + detail(" regex='", regex, "' body=", bodySummary(response, 300)));
    }

    /** Süre (ms) üst sınır */
    public static void assertResponseTimeLe(long actualDurationMs, long maxMs) {
        Assert.assertTrue(actualDurationMs <= maxMs,
                "Response time exceeded" + detail(" maxMs=", maxMs, " actualMs=", actualDurationMs));
    }

    // -------------------- JSON assertions --------------------

    /** JSON parse edilebilir mi */
    public static void assertJsonParsable(APIResponse response) {
        Assert.assertNotNull(rootJson(response), "Response is not valid JSON" + detail(" body=", bodySummary(response, 300)));
    }

    /** path var mı */
    public static void assertJsonExists(APIResponse response, String path) {
        JsonNode n = jsonAt(response, path);
        Assert.assertNotNull(n, "JSON path not found" + detail(" '", path, "' body=", bodySummary(response, 300)));
    }

    /** path eşit mi (string karşılaştırma; non-text ise toString) */
    public static void assertJsonEquals(APIResponse response, String path, Object expected) {
        JsonNode n = jsonAt(response, path);
        Assert.assertNotNull(n, "JSON path not found" + detail(" '", path, "' body=", bodySummary(response, 300)));
        String actual = nodeAsString(n);
        String exp = String.valueOf(expected);
        Assert.assertEquals(actual, exp,
                "JSON value mismatch" + detail(" path='", path, "' expected='", exp, "' actual='", actual, "' body=", bodySummary(response, 300)));
    }

    /** path metin içeriyor mu */
    public static void assertJsonContains(APIResponse response, String path, String expectedSubstring) {
        JsonNode n = jsonAt(response, path);
        Assert.assertNotNull(n, "JSON path not found" + detail(" '", path, "'"));
        String actual = nodeAsString(n);
        Assert.assertTrue(actual.contains(expectedSubstring),
                "JSON text should contain" + detail(" path='", path, "' expected*='", expectedSubstring, "' actual='", actual, "'"));
    }

    /** path tipi (String/Integer/Long/Double/Float/Boolean/List/Map) */
    public static void assertJsonType(APIResponse response, String path, Class<?> expectedType) {
        JsonNode n = jsonAt(response, path);
        Assert.assertNotNull(n, "JSON path not found" + detail(" '", path, "'"));
        Assert.assertTrue(nodeMatchesType(n, expectedType),
                "JSON type mismatch" + detail(" path='", path, "' expected=", expectedType.getSimpleName(), " actual=", nodeType(n)));
    }

    /** Birden fazla alan zorunlu mu (kök objede) */
    public static void assertJsonFieldsPresent(APIResponse response, String... fields) {
        JsonNode root = rootJson(response);
        Assert.assertNotNull(root, "Response is not valid JSON");
        Assert.assertTrue(root.isObject(), "Root JSON must be object");
        for (String f : fields) {
            Assert.assertTrue(root.has(f),
                    "Missing required field" + detail(" '", f, "' available=", availableFields(root)));
        }
    }

    /** Kök JSON array mi */
    public static void assertJsonRootIsArray(APIResponse response) {
        JsonNode root = rootJson(response);
        Assert.assertNotNull(root, "Response is not valid JSON");
        Assert.assertTrue(root.isArray(), "Root JSON should be array");
    }

    /** Kök array boyutu */
    public static void assertJsonArraySize(APIResponse response, int expectedSize) {
        JsonNode root = rootJson(response);
        Assert.assertNotNull(root, "Response is not valid JSON");
        Assert.assertTrue(root.isArray(), "Root JSON should be array");
        Assert.assertEquals(root.size(), expectedSize,
                "Array size mismatch" + detail(" expected=", expectedSize, " actual=", root.size()));
    }

    // =========================================================
    // ===================== COMMON ASSERTS ====================
    // =========================================================

    public static void assertTrue(boolean condition, String message) {
        Assert.assertTrue(condition, message);
    }
    public static void assertFalse(boolean condition, String message) {
        Assert.assertFalse(condition, message);
    }
    public static void assertNull(Object obj, String message) {
        Assert.assertNull(obj, message);
    }
    public static void assertNotNull(Object obj, String message) {
        Assert.assertNotNull(obj, message);
    }
    public static void assertEquals(Object expected, Object actual, String message) {
        Assert.assertEquals(actual, expected, message);
    }
    public static void assertNotEquals(Object expected, Object actual, String message) {
        Assert.assertNotEquals(actual, expected, message);
    }
    public static void fail(String message) {
        Assert.fail(message);
    }
    public static void assertContains(String expected, String actual, String message) {
        Assert.assertTrue(actual.contains(expected), message);
    }
    public static void assertNotContains(String expected, String actual, String message) {
        Assert.assertFalse(actual.contains(expected), message);
    }
    public static void assertStartsWith(String expected, String actual, String message) {
        Assert.assertTrue(actual.startsWith(expected), message);
    }
    public static void assertEndsWith(String expected, String actual, String message) {
        Assert.assertTrue(actual.endsWith(expected), message);
    }
    public static void assertBetween(int min, int max, int actual, String message) {
        Assert.assertTrue(actual >= min && actual <= max, message);
    }
    public static void assertBetween(double min, double max, double actual, String message) {
        Assert.assertTrue(actual >= min && actual <= max, message);
    }
    public static void assertBetween(long min, long max, long actual, String message) {
        Assert.assertTrue(actual >= min && actual <= max, message);
    }
    public static void assertBetween(Date min, Date max, Date actual, String message) {
        Assert.assertTrue(actual.after(min) && actual.before(max), message);
    }
    public static void assertBetween(LocalDateTime min, LocalDateTime max, LocalDateTime actual, String message) {
        Assert.assertTrue(actual.isAfter(min) && actual.isBefore(max), message);
    }
    public static void assertBetween(LocalDate min, LocalDate max, LocalDate actual, String message) {
        Assert.assertTrue(actual.isAfter(min) && actual.isBefore(max), message);
    }
    public static void assertBetween(LocalTime min, LocalTime max, LocalTime actual, String message) {
        Assert.assertTrue(actual.isAfter(min) && actual.isBefore(max), message);
    }




    // =========================================================
    // ===================== UI ASSERTIONS =====================
    // =========================================================

    public static void assertVisible(String selector) {
        boolean visible = ElementUtils.isElementVisible(selector);
        Assert.assertTrue(visible, "Element should be visible" + detail(" selector=", selector));
    }

    public static void assertNotVisible(String selector) {
        boolean visible = ElementUtils.isElementVisible(selector);
        Assert.assertFalse(visible, "Element should NOT be visible" + detail(" selector=", selector));
    }

    public static void assertEnabled(String selector) {
        boolean enabled = ElementUtils.isElementEnabled(selector);
        Assert.assertTrue(enabled, "Element should be enabled" + detail(" selector=", selector));
    }

    public static void assertDisabled(String selector) {
        boolean enabled = ElementUtils.isElementEnabled(selector);
        Assert.assertFalse(enabled, "Element should be disabled" + detail(" selector=", selector));
    }

    public static void assertTextEquals(String selector, String expected) {
        String actual = ElementUtils.getElementText(selector);
        Assert.assertNotNull(actual, "Text is null" + detail(" selector=", selector));
        Assert.assertEquals(actual.trim(), expected.trim(),
                "Text mismatch" + detail(" selector=", selector, " expected='", expected, "' actual='", actual, "'"));
    }

    public static void assertTextContains(String selector, String expectedSubstring) {
        String actual = ElementUtils.getElementText(selector);
        Assert.assertNotNull(actual, "Text is null" + detail(" selector=", selector));
        Assert.assertTrue(actual.contains(expectedSubstring),
                "Text should contain" + detail(" selector=", selector, " expected*='", expectedSubstring, "' actual='", actual, "'"));
    }

    public static void assertAttributeEquals(String selector, String attributeName, String expected) {
        String actual = ElementUtils.getElementAttribute(selector, attributeName);
        Assert.assertEquals(actual, expected,
                "Attribute mismatch" + detail(" selector=", selector, " attr='", attributeName, "' expected='", expected, "' actual='", actual, "'"));
    }

    public static void assertCountIs(String selector, int expectedCount) {
        int actual = ElementUtils.getElementCount(selector);
        Assert.assertEquals(actual, expectedCount,
                "Element count mismatch" + detail(" selector=", selector, " expected=", expectedCount, " actual=", actual));
    }

    public static void assertPageTitleIs(String expected) {
        Page page = PlaywrightManager.getPage();
        Assert.assertNotNull(page, "Playwright Page is null");
        Assert.assertEquals(page.title(), expected,
                "Page title mismatch" + detail(" expected='", expected, "' actual='", page.title(), "'"));
    }

    public static void assertPageUrlContains(String expectedPart) {
        Page page = PlaywrightManager.getPage();
        Assert.assertNotNull(page, "Playwright Page is null");
        String url = page.url();
        Assert.assertTrue(url.contains(expectedPart),
                "URL should contain" + detail(" '", expectedPart, "' actual='", url, "'"));
    }

    // =========================================================
    // ===================== COMMON ASSERTS ====================
    // =========================================================

    public static void assertStringNotEmpty(String value) {
        Assert.assertNotNull(value, "String is null");
        Assert.assertFalse(value.trim().isEmpty(), "String is empty");
    }

    public static void assertStringMatches(String value, String regex) {
        Assert.assertNotNull(value, "String is null");
        Assert.assertTrue(value.matches(regex),
                "String does not match regex" + detail(" regex='", regex, "' value='", value, "'"));
    }

    public static void assertNumberInRange(Number value, Number minInclusive, Number maxInclusive) {
        Assert.assertNotNull(value, "Number is null");
        double v = value.doubleValue();
        Assert.assertTrue(v >= minInclusive.doubleValue() && v <= maxInclusive.doubleValue(),
                "Number out of range" + detail(" value=", v, " min=", minInclusive, " max=", maxInclusive));
    }

    public static void assertCollectionNotEmpty(Collection<?> c) {
        Assert.assertNotNull(c, "Collection is null");
        Assert.assertFalse(c.isEmpty(), "Collection is empty");
    }

    public static void assertCollectionSize(Collection<?> c, int expectedSize) {
        Assert.assertNotNull(c, "Collection is null");
        Assert.assertEquals(c.size(), expectedSize,
                "Collection size mismatch" + detail(" expected=", expectedSize, " actual=", c.size()));
    }

    public static void assertDateTimeFormat(String dateTimeString, String pattern) {
        Assert.assertNotNull(dateTimeString, "Date/time string is null");
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern(pattern);
            LocalDateTime.parse(dateTimeString, f);
        } catch (DateTimeParseException e) {
            Assert.fail("Date/time does not match pattern" + detail(" value='", dateTimeString, "' pattern='", pattern, "' error='", e.getMessage(), "'"));
        }
    }

    // =========================================================
    // ====================== INTERNALS ========================
    // =========================================================

    private static String header(APIResponse response, String name) {
        try {
            Map<String, String> hs = response.headers();
            if (hs == null) return null;
            String ln = name.toLowerCase();
            for (Map.Entry<String, String> e : hs.entrySet()) {
                if (e.getKey() != null && e.getKey().toLowerCase().equals(ln)) {
                    return e.getValue();
                }
            }
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static String bodyTextSafe(APIResponse response) {
        try {
            return response.text();
        } catch (Throwable t) {
            try {
                return new String(response.body(), StandardCharsets.UTF_8);
            } catch (Throwable ignored) {
                return "<unreadable body>";
            }
        }
    }

    private static String bodySummary(APIResponse response, int maxChars) {
        String s = bodyTextSafe(response);
        return s.length() > maxChars ? s.substring(0, maxChars) + "..." : s;
    }

    private static JsonNode rootJson(APIResponse response) {
        try {
            return MAPPER.readTree(bodyTextSafe(response));
        } catch (JsonProcessingException t) {
            return null;
        }
    }

    private static JsonNode jsonAt(APIResponse response, String path) {
        return getByPath(rootJson(response), path);
    }

    /** Basit JSON path: a.b[0].c */
    private static JsonNode getByPath(JsonNode root, String path) {
        if (root == null || path == null || path.isEmpty()) return null;
        String[] parts = path.split("\\.");
        JsonNode cur = root;
        for (String part : parts) {
            if (cur == null) return null;
            int idx = part.indexOf('[');
            if (idx < 0) {
                cur = cur.get(part);
            } else {
                String field = part.substring(0, idx);
                cur = cur.get(field);
                String rest = part.substring(idx);
                while (rest.startsWith("[")) {
                    int end = rest.indexOf(']');
                    if (end < 0) return null;
                    String idxStr = rest.substring(1, end);
                    int i = Integer.parseInt(idxStr);
                    if (!cur.isArray() || i < 0 || i >= cur.size()) return null;
                    cur = cur.get(i);
                    rest = rest.substring(end + 1);
                }
            }
        }
        return cur;
    }

    private static String nodeAsString(JsonNode n) {
        if (n == null || n.isNull()) return "null";
        return n.isTextual() ? n.asText() : n.toString();
    }

    private static boolean nodeMatchesType(JsonNode n, Class<?> type) {
        if (n == null) return false;
        if (type == String.class) return n.isTextual();
        if (type == Integer.class || type == int.class) return n.isInt() || n.canConvertToInt();
        if (type == Long.class || type == long.class) return n.isLong() || n.isInt();
        if (type == Double.class || type == double.class) return n.isDouble() || n.isFloat() || n.isInt() || n.isLong();
        if (type == Float.class || type == float.class) return n.isFloat() || n.isDouble() || n.isInt();
        if (type == Boolean.class || type == boolean.class) return n.isBoolean();
        if (type.getSimpleName().equals("List") || type == Iterable.class || type == Collection.class) return n.isArray();
        if (type == Map.class) return n.isObject();
        return false;
    }

    private static String nodeType(JsonNode n) {
        if (n == null) return "null";
        if (n.isTextual()) return "String";
        if (n.isInt()) return "Integer";
        if (n.isLong()) return "Long";
        if (n.isDouble()) return "Double";
        if (n.isFloat()) return "Float";
        if (n.isBoolean()) return "Boolean";
        if (n.isArray()) return "Array";
        if (n.isObject()) return "Object";
        return "Unknown";
    }

    private static String availableFields(JsonNode obj) {
        if (obj == null || !obj.isObject()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        Iterator<String> it = obj.fieldNames();
        while (it.hasNext()) {
            if (sb.length() > 1) sb.append(", ");
            sb.append(it.next());
        }
        sb.append("]");
        return sb.toString();
    }

    private static String detail(Object... kv) {
        StringBuilder sb = new StringBuilder();
        for (Object o : kv) sb.append(Objects.toString(o, ""));
        return sb.toString();
    }
}

package starlettech.tests.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.starlettech.annotations.ApiTest;
import com.starlettech.core.BaseApiTest;
import com.starlettech.enums.TestPriority;
import com.starlettech.validator.Assertions;

import starlettech.api.clients.TestApiClient;

/**
 * BaseApiClient kullanarak basit HTTP method testleri
 * JSONPlaceholder API ile GET, POST, PUT, PATCH, DELETE örnekleri
 */
@ApiTest
public class SimpleHttpMethodTests extends BaseApiTest {

    private TestApiClient apiClient;


    @BeforeClass
    public void setUp() {
        logger.info("Basit HTTP method testleri başlatılıyor");
        apiClient = new TestApiClient();
    }

    @AfterClass
    public void tearDown() {
        if (apiClient != null) {
            apiClient.cleanup();
        }
        logger.info("Basit HTTP method testleri tamamlandı");
    }

    @Test(groups = {"smoke", "api"}, priority = 1)
    @ApiTest(
            method = "GET",
            endpoint = "/posts",
            requiresAuth = false,
            description = "GET method testi - Tüm postları getir",
            author = "API Test Engineer",
            priority = TestPriority.HIGH,
            jiraId = "JIRA-123",
            tags = ""
    )
    public void testGetMethod() {
        logger.info("GET method testi başlatılıyor");

        // Tüm postları getir
        List<Map<String, Object>> posts = apiClient.getAllPosts();

        Assertions.assertCollectionNotEmpty(posts);
        Assertions.assertCollectionSize(posts, 100);

        // Doğrulamalar
        Assertions.assertNotNull(posts, "Posts listesi null olmamalı");
        Assertions.assertNotNull(posts, "Posts listesi null olmamalı");
        Assert.assertFalse(posts.isEmpty(), "Posts listesi boş olmamalı");
        Assertions.assertEquals(posts.size(), 100, "100 post olmalı");

        // İlk post'un yapısını kontrol et
        Map<String, Object> firstPost = posts.getFirst();
        Assertions.assertTrue(firstPost.containsKey("id"), "Post'ta id olmalı");
        Assertions.assertTrue(firstPost.containsKey("title"), "Post'ta title olmalı");
        Assertions.assertTrue(firstPost.containsKey("body"), "Post'ta body olmalı");
        Assertions.assertTrue(firstPost.containsKey("userId"), "Post'ta userId olmalı");

        logger.info("GET method testi başarılı - {} post getirildi", posts.size());
    }

    @Test(groups = {"smoke", "api"}, priority = 2)
    public void testPostMethod() {
        logger.info("POST method testi başlatılıyor");

        // Yeni post verisi hazırla
        Map<String, Object> newPost = new HashMap<>();
        newPost.put("title", "Test Post Başlığı");
        newPost.put("body", "Bu bir test post içeriğidir");
        newPost.put("userId", 1);

        // POST isteği gönder
        Map<String, Object> createdPost = apiClient.createPost(newPost);

        // Doğrulamalar
        Assertions.assertNotNull(createdPost, "Oluşturulan post null olmamalı");
        Assertions.assertTrue(createdPost.containsKey("id"), "Oluşturulan post'ta id olmalı");
        Assertions.assertEquals(createdPost.get("title"), "Test Post Başlığı", "Başlık eşleşmeli");
        Assertions.assertEquals(createdPost.get("body"), "Bu bir test post içeriğidir", "İçerik eşleşmeli");
        Assertions.assertEquals(createdPost.get("userId"), 1, "UserId eşleşmeli");

        logger.info("POST method testi başarılı - Post ID: {}", createdPost.get("id"));
    }

    @Test(groups = {"regression", "api"}, priority = 3)
    public void testPutMethod() {
        logger.info("PUT method testi başlatılıyor");

        // Güncellenecek post verisi hazırla
        Map<String, Object> updatedPost = new HashMap<>();
        updatedPost.put("id", 1);
        updatedPost.put("title", "Güncellenmiş Post Başlığı");
        updatedPost.put("body", "Bu güncellenmiş bir post içeriğidir");
        updatedPost.put("userId", 1);

        // PUT isteği gönder
        Map<String, Object> result = apiClient.updatePost(1, updatedPost);

        // Doğrulamalar
        Assertions.assertNotNull(result, "Güncellenen post null olmamalı");
        Assertions.assertEquals(result.get("id"), 1, "Post ID eşleşmeli");
        Assertions.assertEquals(result.get("title"), "Güncellenmiş Post Başlığı", "Başlık güncellenmiş olmalı");
        Assertions.assertEquals(result.get("body"), "Bu güncellenmiş bir post içeriğidir", "İçerik güncellenmiş olmalı");

        logger.info("PUT method testi başarılı - Post güncellendi");
    }

    @Test(groups = {"regression", "api"}, priority = 4)
    public void testPatchMethod() {
        logger.info("PATCH method testi başlatılıyor");

        // Kısmi güncelleme verisi hazırla
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "PATCH ile Güncellenmiş Başlık");

        // PATCH isteği gönder
        Map<String, Object> result = apiClient.patchPost(1, updates);

        // Doğrulamalar
        Assertions.assertNotNull(result, "Güncellenen post null olmamalı");
        Assertions.assertEquals(result.get("id"), 1, "Post ID eşleşmeli");
        Assertions.assertEquals(result.get("title"), "PATCH ile Güncellenmiş Başlık", "Başlık güncellenmiş olmalı");
        // Diğer alanlar değişmemiş olmalı
        Assertions.assertNotNull(result.get("body"), "Body alanı korunmalı");
        Assertions.assertNotNull(result.get("userId"), "UserId alanı korunmalı");

        logger.info("PATCH method testi başarılı - Post kısmen güncellendi");
    }

    @Test(groups = {"regression", "api"}, priority = 5)
    public void testDeleteMethod() {
        logger.info("DELETE method testi başlatılıyor");

        // DELETE isteği gönder
        boolean result = apiClient.deletePost(1);

        // Doğrulamalar
        Assertions.assertTrue(result, "Delete işlemi başarılı olmalı");

        logger.info("DELETE method testi başarılı - Post silindi");
    }
}

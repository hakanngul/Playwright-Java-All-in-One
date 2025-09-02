package starlettech.api.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.microsoft.playwright.APIResponse;
import com.starlettech.clients.BaseApiClient;

import java.util.List;
import java.util.Map;

public class TestApiClient extends BaseApiClient {
    public TestApiClient() {
        super(new Config()
                .baseUrl("https://jsonplaceholder.typicode.com")
                .withJsonDefaults()
                .timeoutMs(10000)
                .retries(1)
        );
    }

    // GET örneği - Tüm postları getir
    public List<Map<String, Object>> getAllPosts() {
        APIResponse response = get("/posts");
        assertStatus(response, 200);
        return parse(response, new TypeReference<>() {
        });
    }

    // GET örneği - Tek post getir
    public Map<String, Object> getPostById(int id) {
        APIResponse response = get("/posts/" + id);
        assertStatus(response, 200);
        return parse(response, new TypeReference<>() {
        });
    }

    // POST örneği - Yeni post oluştur
    public Map<String, Object> createPost(Map<String, Object> postData) {
        APIResponse response = post("/posts", postData);
        assertStatus(response, 201);
        return parse(response, new TypeReference<>() {
        });
    }

    // PUT örneği - Post'u tamamen güncelle
    public Map<String, Object> updatePost(int id, Map<String, Object> postData) {
        APIResponse response = put("/posts/" + id, postData);
        assertStatus(response, 200);
        return parse(response, new TypeReference<>() {
        });
    }

    // PATCH örneği - Post'u kısmen güncelle
    public Map<String, Object> patchPost(int id, Map<String, Object> updates) {
        APIResponse response = patch("/posts/" + id, updates);
        assertStatus(response, 200);
        return parse(response, new TypeReference<>() {
        });
    }

    // DELETE örneği - Post'u sil
    public boolean deletePost(int id) {
        APIResponse response = delete("/posts/" + id);
        assertStatus(response, 200);
        return true;
    }
}

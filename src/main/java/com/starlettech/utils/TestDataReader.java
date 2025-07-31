package com.starlettech.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starlettech.config.TestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for reading test data from various sources
 */
public class TestDataReader {
    private static final Logger logger = LogManager.getLogger(TestDataReader.class);
    private static TestDataReader instance;
    private final ObjectMapper objectMapper;
    private final TestConfig testConfig;

    private TestDataReader() {
        this.objectMapper = new ObjectMapper();
        this.testConfig = TestConfig.getInstance();
    }

    public static TestDataReader getInstance() {
        if (instance == null) {
            synchronized (TestDataReader.class) {
                if (instance == null) {
                    instance = new TestDataReader();
                }
            }
        }
        return instance;
    }

    /**
     * Read JSON data from file
     */
    public JsonNode readJsonData(String fileName) {
        try {
            String filePath = testConfig.getTestDataPath() + "/" + fileName;
            File file = new File(filePath);
            if (file.exists()) {
                JsonNode jsonNode = objectMapper.readTree(file);
                logger.info("Successfully read JSON data from: {}", filePath);
                return jsonNode;
            } else {
                // Try to read from resources
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("testdata/" + fileName);
                if (inputStream != null) {
                    JsonNode jsonNode = objectMapper.readTree(inputStream);
                    logger.info("Successfully read JSON data from resources: testdata/{}", fileName);
                    return jsonNode;
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read JSON data from {}: {}", fileName, e.getMessage());
        }
        return null;
    }

    /**
     * Read properties file
     */
    public Properties readProperties(String fileName) {
        Properties properties = new Properties();
        try {
            String filePath = testConfig.getTestDataPath() + "/" + fileName;
            File file = new File(filePath);
            if (file.exists()) {
                properties.load(new java.io.FileInputStream(file));
                logger.info("Successfully read properties from: {}", filePath);
            } else {
                // Try to read from resources
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("testdata/" + fileName);
                if (inputStream != null) {
                    properties.load(inputStream);
                    logger.info("Successfully read properties from resources: testdata/{}", fileName);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read properties from {}: {}", fileName, e.getMessage());
        }
        return properties;
    }

    /**
     * Get user data by username
     */
    public JsonNode getUserData(String username) {
        JsonNode usersData = readJsonData("users.json");
        if (usersData != null && usersData.has("validUsers")) {
            for (JsonNode user : usersData.get("validUsers")) {
                if (user.has("username") && user.get("username").asText().equals(username)) {
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Get API payload data
     */
    public JsonNode getApiPayload(String payloadName) {
        JsonNode payloadData = readJsonData("api-payloads.json");
        if (payloadData != null && payloadData.has(payloadName)) {
            return payloadData.get(payloadName);
        }
        return null;
    }

    /**
     * Get admin user data
     */
    public JsonNode getAdminUser() {
        JsonNode usersData = readJsonData("users.json");
        if (usersData != null && usersData.has("adminUser")) {
            return usersData.get("adminUser");
        }
        return null;
    }

    /**
     * Get invalid user data
     */
    public JsonNode getInvalidUser() {
        JsonNode usersData = readJsonData("users.json");
        if (usersData != null && usersData.has("invalidUsers") && usersData.get("invalidUsers").size() > 0) {
            return usersData.get("invalidUsers").get(0);
        }
        return null;
    }

    /**
     * Convert JsonNode to specific class
     */
    public <T> T convertToObject(JsonNode jsonNode, Class<T> clazz) {
        try {
            return objectMapper.treeToValue(jsonNode, clazz);
        } catch (Exception e) {
            logger.error("Failed to convert JsonNode to {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }
}

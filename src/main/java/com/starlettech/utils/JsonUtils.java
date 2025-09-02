package com.starlettech.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for JSON operations
 */
public class JsonUtils {
    private static final Logger logger = LogManager.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            // Ekstra alanlar yüzünden parse patlamasın:
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Convert object to JSON string
     */
    public static String toJsonString(Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            logger.debug("Converted object to JSON: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert object to JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert JSON string to object by Class<T>
     */
    public static <T> T fromJsonString(String json, Class<T> clazz) {
        try {
            T object = objectMapper.readValue(json, clazz);
            logger.debug("Converted JSON to object: {}", clazz.getSimpleName());
            return object;
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert JSON to object: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert JSON string to object by TypeReference<T>
     * Örn: fromJsonString(json, new TypeReference<List<PostDto>>() {})
     */
    public static <T> T fromJsonString(String json, TypeReference<T> typeRef) {
        try {
            T object = objectMapper.readValue(json, typeRef);
            logger.debug("Converted JSON to object via TypeReference");
            return object;
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert JSON to TypeReference: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parse JSON string to JsonNode
     */
    public static JsonNode parseJson(String json) {
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            logger.debug("Parsed JSON string to JsonNode");
            return jsonNode;
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert JsonNode to object by Class<T>
     */
    public static <T> T convertToObject(JsonNode jsonNode, Class<T> clazz) {
        try {
            T object = objectMapper.treeToValue(jsonNode, clazz);
            logger.debug("Converted JsonNode to object: {}", clazz.getSimpleName());
            return object;
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert JsonNode to object: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert JsonNode to object by TypeReference<T>
     * Örn: convertToObject(node, new TypeReference<List<PostDto>>() {})
     */
    public static <T> T convertToObject(JsonNode jsonNode, TypeReference<T> typeRef) {
        try {
            T object = objectMapper.convertValue(jsonNode, typeRef);
            logger.debug("Converted JsonNode to object via TypeReference");
            return object;
        } catch (IllegalArgumentException e) {
            logger.error("Failed to convert JsonNode to TypeReference: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get value from JsonNode by path (dot notation)
     */
    public static String getValueByPath(JsonNode jsonNode, String path) {
        try {
            String[] pathParts = path.split("\\.");
            JsonNode currentNode = jsonNode;

            for (String part : pathParts) {
                if (currentNode != null && currentNode.has(part)) {
                    currentNode = currentNode.get(part);
                } else {
                    logger.warn("Path not found in JSON: {}", path);
                    return null;
                }
            }
            return currentNode != null ? currentNode.asText() : null;
        } catch (Exception e) {
            logger.error("Failed to get value by path {}: {}", path, e.getMessage());
            return null;
        }
    }

    /**
     * Update JsonNode with new value (dot notation)
     */
    public static JsonNode updateJsonNode(JsonNode jsonNode, String path, String newValue) {
        try {
            String[] pathParts = path.split("\\.");
            JsonNode currentNode = jsonNode;

            // Navigate to parent node
            for (int i = 0; i < pathParts.length - 1; i++) {
                if (currentNode != null && currentNode.has(pathParts[i])) {
                    currentNode = currentNode.get(pathParts[i]);
                } else {
                    logger.warn("Path not found in JSON: {}", path);
                    return jsonNode;
                }
            }

            // Update the value
            if (currentNode instanceof ObjectNode) {
                ((ObjectNode) currentNode).put(pathParts[pathParts.length - 1], newValue);
                logger.debug("Updated JSON node at path: {}", path);
            }

            return jsonNode;
        } catch (Exception e) {
            logger.error("Failed to update JSON node at path {}: {}", path, e.getMessage());
            return jsonNode;
        }
    }

    /**
     * Pretty print JSON (Object veya JSON string)
     */
    public static String prettyPrint(Object object) {
        try {
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            logger.debug("Pretty printed JSON");
            return prettyJson;
        } catch (JsonProcessingException e) {
            logger.error("Failed to pretty print JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if string is valid JSON
     */
    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * Merge two JSON objects (shallow)
     */
    public static JsonNode mergeJson(JsonNode mainNode, JsonNode updateNode) {
        try {
            ObjectNode merged = mainNode.deepCopy();
            updateNode.fields().forEachRemaining(entry -> merged.set(entry.getKey(), entry.getValue()));
            logger.debug("Merged JSON objects");
            return merged;
        } catch (Exception e) {
            logger.error("Failed to merge JSON objects: {}", e.getMessage());
            return mainNode;
        }
    }
}

package com.shiroTest.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

import static com.shiroTest.config.JacksonConfig.setObjectMapper;

public class JsonUtil {


    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        setObjectMapper(objectMapper);
    }

    // 将对象序列化为JSON字符串
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting object to JSON string", e);
        }
    }

    // 将JSON字符串反序列化为对象
    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON string to object", e);
        }
    }

    // 将JSON字符串反序列化为JsonNode
    public static JsonNode toJsonNode(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON string to JsonNode", e);
        }
    }

    // 更新现有JsonNode中的字段
    public static JsonNode updateJsonNode(JsonNode originalNode, String key, String value) {
        if (originalNode.isObject()) {
            ((ObjectNode) originalNode).put(key, value);
        }
        return originalNode;
    }

    // 读取JSON文件并反序列化为对象
    public static <T> T fromJsonFile(String filePath, Class<T> clazz) {
        try {
            return objectMapper.readValue(new File(filePath), clazz);
        } catch (IOException e) {
            throw new RuntimeException("Error reading JSON file", e);
        }
    }

    // 将对象写入JSON文件
    public static void toJsonFile(String filePath, Object object) {
        try {
            objectMapper.writeValue(new File(filePath), object);
        } catch (IOException e) {
            throw new RuntimeException("Error writing JSON to file", e);
        }
    }
}

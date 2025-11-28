package com.tala.ai.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Mem0 memory service client
 * Running on Mac Mini (192.168.1.112)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Mem0Client {
    
    @Value("${external.mem0.url}")
    private String mem0Url;
    
    @Value("${external.mem0.api-key}")
    private String apiKey;
    
    private final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();
    
    /**
     * Add memory to Mem0
     */
    public String addMemory(String userId, String content, String metadata) throws IOException {
        String url = mem0Url + "/v1/memories";
        
        String requestBody = String.format("""
            {
              "user_id": "%s",
              "messages": [{"role": "user", "content": "%s"}],
              "metadata": %s
            }
            """, userId, escapeJson(content), metadata);
        
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
            .addHeader("Authorization", "Bearer " + apiKey)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Mem0 API error: " + response.code());
            }
            return response.body().string();
        }
    }
    
    /**
     * Search memories from Mem0
     */
    public String searchMemories(String userId, String query) throws IOException {
        String url = mem0Url + "/v1/memories/search";
        
        String requestBody = String.format("""
            {
              "user_id": "%s",
              "query": "%s"
            }
            """, userId, escapeJson(query));
        
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
            .addHeader("Authorization", "Bearer " + apiKey)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Mem0 API error: " + response.code());
            }
            return response.body().string();
        }
    }
    
    /**
     * Get all memories for a user
     */
    public String getUserMemories(String userId) throws IOException {
        String url = mem0Url + "/v1/memories?user_id=" + userId;
        
        Request request = new Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer " + apiKey)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Mem0 API error: " + response.code());
            }
            return response.body().string();
        }
    }
    
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}

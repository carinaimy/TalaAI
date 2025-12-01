package com.tala.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Client for origin-data-service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OriginDataServiceClient {
    
    @Value("${services.origin-data-service.url:http://localhost:8089}")
    private String originDataServiceUrl;
    
    private final ObjectMapper objectMapper;
    
    private final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();
    
    /**
     * Send chat event to origin-data-service
     */
    public String sendChatEvent(Object chatEventRequest) throws IOException {
        String url = originDataServiceUrl + "/api/v1/chat-events";
        
        String requestBody = objectMapper.writeValueAsString(chatEventRequest);
        
        log.debug("Sending chat event to origin-data-service: {}", url);
        
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                throw new IOException("Origin-data-service API error: " + response.code() + ", body: " + errorBody);
            }
            
            String responseBody = response.body().string();
            log.info("Chat event sent successfully to origin-data-service");
            return responseBody;
        }
    }
    
    /**
     * Health check for origin-data-service
     */
    public boolean isHealthy() {
        String url = originDataServiceUrl + "/api/v1/chat-events/health";
        
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (Exception e) {
            log.warn("Origin-data-service health check failed: {}", e.getMessage());
            return false;
        }
    }
}

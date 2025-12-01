package com.tala.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Gemini 2.5 Flash integration service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {
    
    private final ObjectMapper objectMapper;
    
    @Value("${gemini.api-key}")
    private String apiKey;
    
    @Value("${gemini.model:gemini-2.0-flash-exp}")
    private String model;
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final String GEMINI_STREAM_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:streamGenerateContent?alt=sse&key=%s";
    
    private final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build();
    
    /**
     * Generate content with Gemini (non-streaming)
     */
    public String generateContent(String prompt) throws IOException {
        String url = String.format(GEMINI_API_URL, model, apiKey);
        
        String requestBody = buildRequestBody(prompt);
        
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Gemini API error: " + response.code());
            }
            
            String responseBody = response.body().string();
            return extractTextFromResponse(responseBody);
        }
    }
    
    /**
     * Generate content with Gemini (streaming via SSE)
     */
    public void generateContentStream(String prompt, SseEmitter emitter) {
        String url = String.format(GEMINI_STREAM_URL, model, apiKey);
        
        String requestBody = buildRequestBody(prompt);
        
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
            .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Gemini streaming failed", e);
                emitter.completeWithError(e);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    emitter.completeWithError(new IOException("Gemini API error: " + response.code()));
                    return;
                }
                
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body().byteStream()))) {
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data)) {
                                emitter.complete();
                                break;
                            }
                            
                            try {
                                String text = extractTextFromStreamChunk(data);
                                if (text != null && !text.isEmpty()) {
                                    emitter.send(SseEmitter.event()
                                        .data(text)
                                        .name("message"));
                                }
                            } catch (Exception e) {
                                log.warn("Failed to parse stream chunk: {}", e.getMessage());
                            }
                        }
                    }
                    
                    emitter.complete();
                } catch (Exception e) {
                    log.error("Error processing stream", e);
                    emitter.completeWithError(e);
                }
            }
        });
    }
    
    /**
     * Generate content with system instruction and context
     */
    public String generateWithContext(String systemInstruction, String userPrompt, String context) throws IOException {
        String combinedPrompt = String.format(
            "System: %s\n\nContext: %s\n\nUser: %s",
            systemInstruction, context, userPrompt
        );
        return generateContent(combinedPrompt);
    }
    
    /**
     * Generate content with attachments (images, PDFs)
     * Note: For Gemini 2.5 Flash, we'll use inline data for images
     */
    public String generateContentWithAttachments(String prompt, List<String> attachmentUrls) throws IOException {
        String url = String.format(GEMINI_API_URL, model, apiKey);
        
        String requestBody = buildRequestBodyWithAttachments(prompt, attachmentUrls);
        
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Gemini API error: " + response.code());
            }
            
            String responseBody = response.body().string();
            return extractTextFromResponse(responseBody);
        }
    }
    
    private String buildRequestBodyWithAttachments(String prompt, List<String> attachmentUrls) {
        StringBuilder partsJson = new StringBuilder();
        partsJson.append("[{\"text\": \"").append(escapeJson(prompt)).append("\"}");
        
        // For now, we'll add attachment URLs as text references
        // In production, you'd fetch and encode images as base64
        if (attachmentUrls != null && !attachmentUrls.isEmpty()) {
            partsJson.append(",{\"text\": \"Attachment URLs: ");
            partsJson.append(String.join(", ", attachmentUrls));
            partsJson.append("\"}");
        }
        
        partsJson.append("]");
        
        return String.format("""
            {
              "contents": [{
                "parts": %s
              }],
              "generationConfig": {
                "temperature": 0.7,
                "topK": 40,
                "topP": 0.95,
                "maxOutputTokens": 4096
              }
            }
            """, partsJson.toString());
    }
    
    private String buildRequestBody(String prompt) {
        return String.format("""
            {
              "contents": [{
                "parts": [{
                  "text": "%s"
                }]
              }],
              "generationConfig": {
                "temperature": 0.7,
                "topK": 40,
                "topP": 0.95,
                "maxOutputTokens": 2048
              }
            }
            """, escapeJson(prompt));
    }
    
    private String extractTextFromResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode content = candidates.get(0).path("content");
            JsonNode parts = content.path("parts");
            
            if (parts.isArray() && parts.size() > 0) {
                return parts.get(0).path("text").asText();
            }
        }
        
        return "";
    }
    
    private String extractTextFromStreamChunk(String chunk) throws IOException {
        JsonNode root = objectMapper.readTree(chunk);
        JsonNode candidates = root.path("candidates");
        
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode content = candidates.get(0).path("content");
            JsonNode parts = content.path("parts");
            
            if (parts.isArray() && parts.size() > 0) {
                return parts.get(0).path("text").asText();
            }
        }
        
        return null;
    }
    
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}

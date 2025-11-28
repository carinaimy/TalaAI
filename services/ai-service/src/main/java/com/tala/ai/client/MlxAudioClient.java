package com.tala.ai.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * MLX Audio service client
 * Running on Mac Mini (192.168.1.112)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MlxAudioClient {
    
    @Value("${external.mlx-audio.url}")
    private String mlxAudioUrl;
    
    private final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build();
    
    /**
     * Text to speech
     */
    public byte[] textToSpeech(String text, String voice) throws IOException {
        String url = mlxAudioUrl + "/tts";
        
        String requestBody = String.format("""
            {
              "text": "%s",
              "voice": "%s"
            }
            """, escapeJson(text), voice);
        
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("MLX Audio API error: " + response.code());
            }
            return response.body().bytes();
        }
    }
    
    /**
     * Speech to text
     */
    public String speechToText(byte[] audioData) throws IOException {
        String url = mlxAudioUrl + "/stt";
        
        RequestBody requestBody = RequestBody.create(audioData, MediaType.parse("audio/wav"));
        
        Request request = new Request.Builder()
            .url(url)
            .post(requestBody)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("MLX Audio API error: " + response.code());
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

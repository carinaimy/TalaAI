package com.tala.ai.controller;

import com.tala.ai.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI streaming API with SSE support
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AIStreamController {
    
    private final GeminiService geminiService;
    
    /**
     * Generate content (non-streaming)
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateContent(
        @RequestBody Map<String, String> request
    ) {
        String prompt = request.get("prompt");
        log.info("POST /api/v1/ai/generate - prompt length: {}", prompt.length());
        
        try {
            String response = geminiService.generateContent(prompt);
            return ResponseEntity.ok(Map.of("content", response));
        } catch (IOException e) {
            log.error("Failed to generate content", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Generate content with streaming (SSE)
     */
    @PostMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateContentStream(
        @RequestBody Map<String, String> request
    ) {
        String prompt = request.get("prompt");
        log.info("POST /api/v1/ai/generate/stream - prompt length: {}", prompt.length());
        
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(5));
        
        emitter.onCompletion(() -> log.debug("SSE completed"));
        emitter.onTimeout(() -> {
            log.warn("SSE timeout");
            emitter.complete();
        });
        emitter.onError((e) -> {
            log.error("SSE error", e);
            emitter.completeWithError(e);
        });
        
        // Start streaming in background
        geminiService.generateContentStream(prompt, emitter);
        
        return emitter;
    }
    
    /**
     * Generate with context (non-streaming)
     */
    @PostMapping("/generate/context")
    public ResponseEntity<Map<String, String>> generateWithContext(
        @RequestBody Map<String, String> request
    ) {
        String systemInstruction = request.get("systemInstruction");
        String userPrompt = request.get("prompt");
        String context = request.get("context");
        
        log.info("POST /api/v1/ai/generate/context");
        
        try {
            String response = geminiService.generateWithContext(
                systemInstruction, userPrompt, context
            );
            return ResponseEntity.ok(Map.of("content", response));
        } catch (IOException e) {
            log.error("Failed to generate content with context", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
}

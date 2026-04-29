package com.ecommerce.api.controller;

import com.ecommerce.api.dto.ChatMessage;
import com.ecommerce.api.dto.ChatRequest;
import com.ecommerce.api.dto.ChatResponse;
import com.ecommerce.api.dto.ProductDTO;
import com.ecommerce.api.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.chat(request));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<ProductDTO>> getRecommendations(
            @RequestParam(defaultValue = "0") Long userId) {
        return ResponseEntity.ok(chatService.getRecommendations(userId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @RequestParam String sessionId) {
        return ResponseEntity.ok(chatService.getChatHistory(sessionId));
    }

    @DeleteMapping("/session")
    public ResponseEntity<Map<String, String>> clearSession(
            @RequestParam String sessionId) {
        chatService.clearSession(sessionId);
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }
}
package com.ecommerce.api.service;

import com.ecommerce.api.dto.ChatMessage;
import com.ecommerce.api.dto.ChatRequest;
import com.ecommerce.api.dto.ChatResponse;
import com.ecommerce.api.dto.ProductDTO;

import java.util.List;

public interface ChatService {
    ChatResponse chat(ChatRequest request);
    List<ProductDTO> getRecommendations(Long userId);
    List<ChatMessage> getChatHistory(String sessionId);
    void clearSession(String sessionId);
    void cleanupExpiredSessions();
}
package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.ChatMessage;
import com.ecommerce.api.dto.ChatRequest;
import com.ecommerce.api.dto.ChatResponse;
import com.ecommerce.api.dto.ProductDTO;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.service.ChatService;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private static final int MAX_MESSAGES_PER_SESSION = 50;
    private static final long SESSION_TTL_MINUTES = 30;

    private final ProductRepository productRepository;
    private final ChatClient chatClient;

    private final ConcurrentHashMap<String, ChatMemory> sessionMemoryMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> sessionLastAccessMap = new ConcurrentHashMap<>();

    public ChatServiceImpl(ProductRepository productRepository, ChatClient.Builder chatClientBuilder) {
        this.productRepository = productRepository;
        this.chatClient = chatClientBuilder.build();
    }

    @PostConstruct
    public void init() {
        sessionLastAccessMap.put("default", LocalDateTime.now());
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : "default";

        ChatMemory chatMemory = sessionMemoryMap.computeIfAbsent(sessionId, k -> {
            sessionLastAccessMap.put(sessionId, LocalDateTime.now());
            return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(MAX_MESSAGES_PER_SESSION)
                .build();
        });

        // MessageWindowChatMemory gestiona el tamaño de la ventana automáticamente.
        // Si aun así quieres limpiar la sesión manualmente:
        // chatMemory.clear(sessionId);

        sessionLastAccessMap.put(sessionId, LocalDateTime.now());

        List<Product> products = productRepository.findAll();
        String productContext = products.stream()
            .map(p -> String.format("ID: %d - %s: %s ($%.2f) [Stock: %d]",
                p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock()))
            .collect(Collectors.joining("\n"));

        String systemPrompt = String.format("""
            Eres un asistente de ventas amable y experto para un eCommerce.
            Tu objetivo es ayudar a los usuarios a encontrar productos y resolver dudas.
            
            PRODUCTOS DISPONIBLES:
            %s
            
            REGLAS:
            1. Usa el contexto de productos anterior para hacer recomendaciones.
            2. Si recomiendas un producto, menciona su nombre y descripción brevemente.
            3. Al final de tu respuesta, si recomendaste productos, incluye los IDs de la forma [ID:X, ID:Y].
            4. Sé conciso, amigable y profesional.
            """, productContext);

        String reply = chatClient.prompt()
            .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(sessionId)
                .build())
            .system(systemPrompt)
            .user(request.getMessage())
            .call()
            .content();

        // El MessageChatMemoryAdvisor ya guarda el mensaje del usuario y la respuesta en chatMemory automáticamente.

        List<ProductDTO> recommended = extractRecommendations(reply, products);

        return ChatResponse.builder()
            .reply(reply)
            .recommendedProducts(recommended)
            .build();
    }

    @Override
    public List<ProductDTO> getRecommendations(Long userId) {
        // Implementación básica de recomendaciones generales
        return productRepository.findAll().stream()
            .limit(5)
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessage> getChatHistory(String sessionId) {
        ChatMemory chatMemory = sessionMemoryMap.get(sessionId);
        if (chatMemory == null) {
            return List.of();
        }
        return chatMemory.get(sessionId).stream()
            .map((Message m) -> ChatMessage.builder()
                .role(m.getMessageType().name())
                .content(m.getText())
                .timestamp(LocalDateTime.now())
                .build())
            .collect(Collectors.toList());
    }

    @Override
    public void clearSession(String sessionId) {
        sessionMemoryMap.remove(sessionId);
        sessionLastAccessMap.remove(sessionId);
    }

    @Override
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(SESSION_TTL_MINUTES);
        sessionLastAccessMap.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));
        sessionMemoryMap.keySet().removeIf(key -> !sessionLastAccessMap.containsKey(key));
    }

    private List<ProductDTO> extractRecommendations(String reply, List<Product> products) {
        // Busca patrones como [ID:1, ID:2] o simplemente ID:1
        return products.stream()
            .filter(p -> reply.contains("[ID:" + p.getId() + "]") || reply.contains("ID:" + p.getId()))
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    private ProductDTO mapToDTO(Product p) {
        return ProductDTO.builder()
            .id(p.getId())
            .name(p.getName())
            .description(p.getDescription())
            .price(p.getPrice())
            .stock(p.getStock())
            .category(p.getCategory())
            .imageUrl(p.getImageUrl())
            .build();
    }
}

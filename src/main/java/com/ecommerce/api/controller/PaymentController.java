package com.ecommerce.api.controller;

import com.ecommerce.api.entity.Order;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Tag(name = "Pagos", description = "Endpoints para procesamiento de pagos con Stripe")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    @Operation(summary = "Crear sesión de pago", description = "Crea una sesión de checkout de Stripe y retorna la URL")
    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestParam Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            return ResponseEntity.ok(Map.of("url", "/orders"));
        }

        String checkoutUrl = paymentService.createCheckoutSession(order);
        return ResponseEntity.ok(Map.of("url", checkoutUrl));
    }

    @Operation(summary = "Webhook de Stripe", description = "Recibe eventos de Stripe para confirmar pagos")
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            paymentService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error procesando webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

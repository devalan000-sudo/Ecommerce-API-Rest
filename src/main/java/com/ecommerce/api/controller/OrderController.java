package com.ecommerce.api.controller;

import com.ecommerce.api.dto.OrderResponse;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Pedidos", description = "Endpoints para gestión de pedidos")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Obtener mis pedidos", description = "Retorna lista de pedidos del usuario autenticado")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getMyOrders(user));
    }

    @Operation(summary = "Crear pedido", description = "Crea un nuevo pedido a partir del carrito")
    @PostMapping
    public ResponseEntity<Void> createOrder(@AuthenticationPrincipal User user) {
        orderService.createOrder(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

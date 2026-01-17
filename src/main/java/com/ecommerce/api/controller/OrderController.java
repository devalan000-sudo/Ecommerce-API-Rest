package com.ecommerce.api.controller;

import com.ecommerce.api.dto.OrderResponse;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.service.OrderService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getMyOrders(user));
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@AuthenticationPrincipal User user) {
        orderService.createOrder(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

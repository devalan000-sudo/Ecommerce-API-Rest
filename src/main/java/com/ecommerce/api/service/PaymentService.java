package com.ecommerce.api.service;

import com.ecommerce.api.entity.Order;

public interface PaymentService {
    String createCheckoutSession(Order order);
    void handleWebhook(String payload, String sigHeader);
}

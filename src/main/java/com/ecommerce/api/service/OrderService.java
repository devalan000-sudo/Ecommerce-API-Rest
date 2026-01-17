package com.ecommerce.api.service;

import com.ecommerce.api.dto.OrderResponse;
import com.ecommerce.api.entity.User;

import java.util.List;

public interface OrderService {
    void createOrder(User user);

    List<OrderResponse> getMyOrders(User user);
}

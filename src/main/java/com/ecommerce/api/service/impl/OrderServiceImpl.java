package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.OrderResponse;
import com.ecommerce.api.entity.Order;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.mappers.OrderMapper;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderResponse createOrder() {
        return null;
    }

    @Override
    public List<OrderResponse> getMyOrders(User user) {
        List<Order> orders = orderRepository.findByUserOrderByDateDesc(user);
        return orderMapper.toOrderResponseList(orders);
    }
}

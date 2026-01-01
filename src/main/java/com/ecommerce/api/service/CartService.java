package com.ecommerce.api.service;

import com.ecommerce.api.dto.CartItemRequest;
import com.ecommerce.api.dto.CartItemResponse;
import com.ecommerce.api.entity.User;

import java.util.List;

public interface CartService {
    List<CartItemResponse> getCart(User user);
    List<CartItemResponse> addToCart(User user, CartItemRequest request);
    void checkout(User user);
    List<CartItemResponse> removeFromCart(User user,Long carrItemId);
    List<CartItemResponse> clearCart(User user);
}

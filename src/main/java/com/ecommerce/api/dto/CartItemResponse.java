package com.ecommerce.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {
    Long id;
    Long productId;
    String productName;
    String imageUrl;
    BigDecimal price;
    Integer quantity;
    BigDecimal subtotal;
}

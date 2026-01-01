package com.ecommerce.api.dto;

import lombok.Data;

@Data
public class CartItemResponse {
    Long id;
    Long productId;
    String productName;
    String imageUrl;
    Double price;
    Integer quantity;
    Double subtotal;
}

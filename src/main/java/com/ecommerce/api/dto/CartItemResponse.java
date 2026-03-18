package com.ecommerce.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    Long id;
    Long productId;
    String productName;
    String imageUrl;
    BigDecimal price;
    Integer quantity;
    BigDecimal subtotal;
}

package com.ecommerce.api.dto;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long  productId;
    private Integer quantity;
}

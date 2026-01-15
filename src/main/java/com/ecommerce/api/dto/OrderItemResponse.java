package com.ecommerce.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal price;
}

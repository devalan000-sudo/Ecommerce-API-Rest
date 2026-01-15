package com.ecommerce.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

@NotNull
@Data
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
}

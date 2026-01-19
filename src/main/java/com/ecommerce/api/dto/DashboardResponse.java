package com.ecommerce.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
public class DashboardResponse {
    private BigDecimal totalRenevue;
    public long totalOrders;
    private long totalProducts;
    private long lowStockCount;
    private List<ProductResponse> lowStockProducts;
}

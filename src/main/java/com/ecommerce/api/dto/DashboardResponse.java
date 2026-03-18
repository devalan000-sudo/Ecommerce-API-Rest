package com.ecommerce.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private BigDecimal totalRenevue;
    public long totalOrders;
    private long totalProducts;
    private long lowStockCount;
    private List<ProductResponse> lowStockProducts;
}

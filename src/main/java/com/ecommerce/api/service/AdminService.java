package com.ecommerce.api.service;

import com.ecommerce.api.dto.DashboardResponse;
import com.ecommerce.api.dto.ProductDTO;
import com.ecommerce.api.dto.ProductRequest;
import com.ecommerce.api.dto.ProductResponse;

import java.util.List;

public interface AdminService {
    DashboardResponse getDashboard();
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    List<ProductResponse> getAllProducts();
}

package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.DashboardResponse;
import com.ecommerce.api.dto.ProductDTO;
import com.ecommerce.api.dto.ProductRequest;
import com.ecommerce.api.dto.ProductResponse;
import com.ecommerce.api.entity.Order;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.mappers.ProductMapper;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminServiceImpl implements AdminService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public DashboardResponse getDashboard() {
        BigDecimal totalRenevue = orderRepository.findAll().stream()
                .map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();

        List<Product> lowStock = productRepository.findAll().stream()
                .filter(p -> p.getStock() < 5).toList();

        return DashboardResponse.builder()
                .totalRenevue(totalRenevue)
                .totalOrders(totalOrders)
                .totalProducts(totalProducts)
                .lowStockCount(lowStock.size())
                .lowStockProducts(productMapper.toProductResponseList(lowStock))
                .build();
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);
        log.info("Admin: Producto creado - {}", saved.getName());
        return productMapper.toProductResponse(saved);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setStock(request.getStock());
            product.setCategory(request.getCategory());
            product.setImageUrl(request.getImageUrl());

            Product updatedProduct = productRepository.save(product);
            log.info("Admin: Producto actualizado - {}", updatedProduct.getName());

        return productMapper.toProductResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException("No se puede eliminar: Producto no encontrado");
        }
        productRepository.getReferenceById(id);
        log.info("Admin: Producto eliminado con ID: {}", id);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        List<Product>  products = productRepository.findAll();
        return productMapper.toProductResponseList(products);
    }


}

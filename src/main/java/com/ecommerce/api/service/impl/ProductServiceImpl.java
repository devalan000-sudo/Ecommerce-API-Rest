package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.ProductDTO;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.exception.ResourseNotFoundException;
import com.ecommerce.api.mappers.ProductMapper;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {

        return productRepository.findAll().stream()
                .map(productMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String name){
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(productMapper::toDto).toList();
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = productMapper.toEntity(dto);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourseNotFoundException("Producto no encontrado"));
        productMapper.updateEntityFromDto(dto, product);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) throw  new ResourseNotFoundException("Producto no encontrado");
        productRepository.deleteById(id);
    }
}

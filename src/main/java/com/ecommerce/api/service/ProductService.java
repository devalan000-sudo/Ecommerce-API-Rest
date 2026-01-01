package com.ecommerce.api.service;

import com.ecommerce.api.dto.ProductDTO;

import java.util.List;

public interface ProductService {
   List<ProductDTO> getAllProducts();
   List<ProductDTO> searchProducts(String name);
   ProductDTO createProduct(ProductDTO productDTO);
   ProductDTO updateProduct(Long id, ProductDTO productDTO);
   void deleteProduct(Long id);

}

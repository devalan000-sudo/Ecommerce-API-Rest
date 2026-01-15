package com.ecommerce.api.controller;

import com.ecommerce.api.dto.ProductDTO;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/products")
@RestController
public class ProductController {

    private final ProductService productService;

    @GetMapping("public/product")
    public ResponseEntity<List<ProductDTO>> getAllProducts(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/public/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String name){
        return ResponseEntity.ok(productService.searchProducts(name));
    }

    @PostMapping("/create")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO, @AuthenticationPrincipal User user){
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updatedProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO, @AuthenticationPrincipal User user){
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long id, @AuthenticationPrincipal User user){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

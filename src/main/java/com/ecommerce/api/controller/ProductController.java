package com.ecommerce.api.controller;

import com.ecommerce.api.dto.ProductDTO;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.service.CloudinaryService;
import com.ecommerce.api.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Productos", description = "Endpoints para gestión de productos")
@RequiredArgsConstructor
@RequestMapping("/products")
@RestController
public class ProductController {

    private final ProductService productService;
    private final CloudinaryService cloudinaryService;

    @Operation(summary = "Obtener todos los productos", description = "Retorna lista de todos los productos disponibles")
    @GetMapping("public/product")
    public ResponseEntity<List<ProductDTO>> getAllProducts(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Operation(summary = "Buscar productos", description = "Busca productos por nombre")
    @GetMapping("/public/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String name){
        return ResponseEntity.ok(productService.searchProducts(name));
    }

    @Operation(summary = "Crear producto", description = "Crea un nuevo producto (solo Admin)")
    @PostMapping("/admin/create")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO, @AuthenticationPrincipal User user){
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @Operation(summary = "Actualizar producto", description = "Actualiza un producto existente por ID (solo Admin)")
    @PutMapping("/admin/{id}")
    public ResponseEntity<ProductDTO> updatedProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO, @AuthenticationPrincipal User user){
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @Operation(summary = "Eliminar producto", description = "Elimina un producto por ID (solo Admin)")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, @AuthenticationPrincipal User user){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Subir imagen de producto", description = "Sube una imagen a Cloudinary y retorna la URL (solo Admin)")
    @PostMapping("/admin/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir imagen: " + e.getMessage());
        }
    }
}

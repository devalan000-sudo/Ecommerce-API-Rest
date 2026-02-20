package com.ecommerce.api.controller;

import com.ecommerce.api.dto.DashboardResponse;
import com.ecommerce.api.dto.ProductRequest;
import com.ecommerce.api.dto.ProductResponse;
import com.ecommerce.api.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Administración", description = "Endpoints para administración del sistema (solo ADMIN)")
@RequiredArgsConstructor
@RequestMapping("/admin")
@RestController
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Obtener dashboard", description = "Retorna estadísticas del sistema")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(){
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @Operation(summary = "Listar todos los productos", description = "Retorna lista de todos los productos")
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        return ResponseEntity.ok(adminService.getAllProducts());
    }

    @Operation(summary = "Crear producto", description = "Crea un nuevo producto")
    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createProduct(productRequest));
    }

    @Operation(summary = "Actualizar producto", description = "Actualiza un producto existente por ID")
    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest){
        return ResponseEntity.ok(adminService.updateProduct(id, productRequest));
    }

    @Operation(summary = "Eliminar producto", description = "Elimina un producto por ID")
    @DeleteMapping("/products/{id}")
    public ResponseEntity<ProductResponse> deleteProduct(@PathVariable Long id){
        adminService.deleteProduct(id);
        return  ResponseEntity.noContent().build();
    }

}

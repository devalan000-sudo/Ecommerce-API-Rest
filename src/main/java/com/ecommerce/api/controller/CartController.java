package com.ecommerce.api.controller;

import com.ecommerce.api.dto.CartItemRequest;
import com.ecommerce.api.dto.CartItemResponse;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Carrito", description = "Endpoints para gestión del carrito de compras")
@RequiredArgsConstructor
@RequestMapping("/client/cart")
@RestController
public class CartController {

    private final CartService  cartService;

    @Operation(summary = "Obtener carrito", description = "Retorna todos los items del carrito del usuario")
    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart (@AuthenticationPrincipal User user){
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @Operation(summary = "Agregar al carrito", description = "Agrega un producto al carrito")
    @PostMapping("/add")
    public ResponseEntity<List<CartItemResponse>> addToCart(@AuthenticationPrincipal User user, @RequestBody CartItemRequest request){
        List<CartItemResponse> cart = cartService.addToCart(user,request);
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "Eliminar item del carrito", description = "Elimina un item específico del carrito")
    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<List<CartItemResponse>> removeFromCart (@AuthenticationPrincipal User user, @PathVariable Long itemId){
        List<CartItemResponse> cart = cartService.removeFromCart(user,itemId);
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "Limpiar carrito", description = "Elimina todos los items del carrito")
    @DeleteMapping("/clear")
    public ResponseEntity<List<CartItemResponse>> clearCart(@AuthenticationPrincipal User user){
        List<CartItemResponse> cart = cartService.clearCart(user);
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "Checkout", description = "Procesa la compra y crea una orden")
    @PostMapping("/checkout")
    public void chekout(@AuthenticationPrincipal User user){
        cartService.checkout(user);
    }
}

    @PostMapping("/add")
    public ResponseEntity<List<CartItemResponse>> addToCart(@AuthenticationPrincipal User user, @RequestBody CartItemRequest request){
        List<CartItemResponse> cart = cartService.addToCart(user,request);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<List<CartItemResponse>> removeFromCart (@AuthenticationPrincipal User user, @PathVariable Long itemId){
        List<CartItemResponse> cart = cartService.removeFromCart(user,itemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<List<CartItemResponse>> clearCart(@AuthenticationPrincipal User user){
        List<CartItemResponse> cart = cartService.clearCart(user);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/checkout")
    public void chekout(@AuthenticationPrincipal User user){
        cartService.checkout(user);
    }
}

package com.ecommerce.api.controller;

import com.ecommerce.api.dto.CartItemRequest;
import com.ecommerce.api.dto.CartItemResponse;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/client/cart")
@RestController
public class CartController {

    private final CartService  cartService;

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart (@AuthenticationPrincipal User user){
        return ResponseEntity.ok(cartService.getCart(user));
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

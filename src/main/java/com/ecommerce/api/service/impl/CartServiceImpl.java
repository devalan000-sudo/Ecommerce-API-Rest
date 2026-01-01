package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.CartItemRequest;
import com.ecommerce.api.dto.CartItemResponse;
import com.ecommerce.api.entity.CartItem;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.exception.ResourseNotFoundException;
import com.ecommerce.api.mappers.CartMapper;
import com.ecommerce.api.repository.CartItemRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;


    @Override
    @Transactional
    public List<CartItemResponse> getCart(User user) {
        List<CartItem> items = cartItemRepository.findByUser(user);
        return cartMapper.toResponseList(items);
    }


    @Override
    @Transactional
    public List<CartItemResponse> addToCart(User user, CartItemRequest request) {
        Product product = productRepository.findById(request.getProductId()).
                orElseThrow(() -> new ResourseNotFoundException("Producto no encotnrado"));
        if (product.getStock() < request.getQuantity()){
            throw new ResourseNotFoundException("No hay suficiente producto en el stock");
        }
        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElseGet(() -> CartItem.builder()
                        .user(user)
                        .product(product)
                        .quantity(0)
                        .build());
        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        cartItemRepository.save(cartItem);
        return getCart(user);
    }

    @Override
    @Transactional
    public void checkout(User user) {
        List<CartItem> items = cartItemRepository.findByUser(user);
        if (items.isEmpty()){
            throw new BusinessException("El carrito esta vacio");
        }
        for (CartItem item : items) {
            Product p = item.getProduct();
            if ((p.getStock() < item.getQuantity())){
                throw new BusinessException("No hay suficiente producto en el stock");
            }
            p.setStock(p.getStock() -  item.getQuantity());
        }
        cartItemRepository.deleteAll(items);

    }

    @Override
    @Transactional
    public List<CartItemResponse> removeFromCart(User user, Long carrItemId) {
        CartItem cartItem = cartItemRepository.findById(carrItemId)
                .orElseThrow(() -> new ResourseNotFoundException("Item no encontrado"));
        if (!cartItem.getUser().getId().equals(user.getId())){
            throw new BusinessException("No tienes permiso para eliminar este item");
        }
            cartItemRepository.delete(cartItem);
        return getCart(user);
    }

    @Override
    @Transactional
    public List<CartItemResponse> clearCart(User user) {
        cartItemRepository.deleteByUser(user);
        return List.of();
    }
}

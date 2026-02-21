package com.ecommerce.api.service;

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
import com.ecommerce.api.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .username("testuser")
                .build();
    }

    private Product createTestProduct() {
        return Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .stock(50)
                .build();
    }

    @Test
    void getCart_WhenCartIsEmpty_ReturnsEmptyList() {
        User user = createTestUser();
        
        when(cartItemRepository.findByUser(user)).thenReturn(Collections.emptyList());
        when(cartMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<CartItemResponse> result = cartService.getCart(user);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cartItemRepository).findByUser(user);
    }

    @Test
    void getCart_WhenCartHasItems_ReturnsItemList() {
        User user = createTestUser();
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .user(user)
                .product(createTestProduct())
                .quantity(2)
                .build();
        
        CartItemResponse response = new CartItemResponse();
        response.setId(1L);
        response.setQuantity(2);

        when(cartItemRepository.findByUser(user)).thenReturn(Arrays.asList(cartItem));
        when(cartMapper.toResponseList(anyList())).thenReturn(Arrays.asList(response));

        List<CartItemResponse> result = cartService.getCart(user);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cartItemRepository).findByUser(user);
    }

    @Test
    void addToCart_WithSufficientStock_AddsItemToCart() {
        User user = createTestUser();
        Product product = createTestProduct();
        
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem());
        when(cartItemRepository.findByUser(user)).thenReturn(Collections.emptyList());
        when(cartMapper.toResponseList(anyList())).thenReturn(Collections.emptyList());

        List<CartItemResponse> result = cartService.addToCart(user, request);

        assertNotNull(result);
        verify(productRepository).findById(1L);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addToCart_WithInsufficientStock_ThrowsException() {
        User user = createTestUser();
        Product product = Product.builder()
                .id(1L)
                .name("Limited Product")
                .price(BigDecimal.valueOf(100))
                .stock(5)
                .build();
        
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(10);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(ResourseNotFoundException.class, () -> 
                cartService.addToCart(user, request));
    }

    @Test
    void addToCart_WhenProductNotFound_ThrowsException() {
        User user = createTestUser();
        
        CartItemRequest request = new CartItemRequest();
        request.setProductId(999L);
        request.setQuantity(2);

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourseNotFoundException.class, () -> 
                cartService.addToCart(user, request));
    }

    @Test
    void removeFromCart_WhenItemExistsAndBelongsToUser_RemovesSuccessfully() {
        User user = createTestUser();
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .user(user)
                .product(createTestProduct())
                .quantity(2)
                .build();

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(cartItem);
        when(cartItemRepository.findByUser(user)).thenReturn(Collections.emptyList());
        when(cartMapper.toResponseList(anyList())).thenReturn(Collections.emptyList());

        List<CartItemResponse> result = cartService.removeFromCart(user, 1L);

        assertNotNull(result);
        verify(cartItemRepository).findById(1L);
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void removeFromCart_WhenItemNotBelongsToUser_ThrowsException() {
        User user = createTestUser();
        User otherUser = User.builder().id(2L).username("otheruser").build();
        
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .user(otherUser)
                .product(createTestProduct())
                .quantity(2)
                .build();

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        assertThrows(BusinessException.class, () -> 
                cartService.removeFromCart(user, 1L));
    }

    @Test
    void removeFromCart_WhenItemNotFound_ThrowsException() {
        User user = createTestUser();
        
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourseNotFoundException.class, () -> 
                cartService.removeFromCart(user, 999L));
    }

    @Test
    void clearCart_DeletesAllItemsSuccessfully() {
        User user = createTestUser();
        
        doNothing().when(cartItemRepository).deleteByUser(user);

        List<CartItemResponse> result = cartService.clearCart(user);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cartItemRepository).deleteByUser(user);
    }

    @Test
    void checkout_WithItemsInCart_CreatesOrderAndClearsCart() {
        User user = createTestUser();
        Product product = createTestProduct();
        
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .user(user)
                .product(product)
                .quantity(2)
                .build();

        when(cartItemRepository.findByUser(user)).thenReturn(Arrays.asList(cartItem));
        doNothing().when(cartItemRepository).deleteAll(anyList());

        assertDoesNotThrow(() -> cartService.checkout(user));
        
        verify(cartItemRepository).findByUser(user);
        verify(cartItemRepository).deleteAll(anyList());
    }

    @Test
    void checkout_WithEmptyCart_ThrowsException() {
        User user = createTestUser();
        
        when(cartItemRepository.findByUser(user)).thenReturn(Collections.emptyList());

        assertThrows(BusinessException.class, () -> cartService.checkout(user));
    }

    @Test
    void checkout_WithInsufficientStock_ThrowsException() {
        User user = createTestUser();
        Product product = Product.builder()
                .id(1L)
                .name("Limited Product")
                .price(BigDecimal.valueOf(100))
                .stock(1)
                .build();
        
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .user(user)
                .product(product)
                .quantity(5)
                .build();

        when(cartItemRepository.findByUser(user)).thenReturn(Arrays.asList(cartItem));

        assertThrows(BusinessException.class, () -> cartService.checkout(user));
    }
}

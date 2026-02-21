package com.ecommerce.api.service;

import com.ecommerce.api.dto.OrderResponse;
import com.ecommerce.api.entity.CartItem;
import com.ecommerce.api.entity.Order;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.mappers.OrderMapper;
import com.ecommerce.api.repository.CartItemRepository;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

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
    void createOrder_WithItemsInCart_CreatesOrderSuccessfully() {
        User user = createTestUser();
        Product product = createTestProduct();
        
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .user(user)
                .product(product)
                .quantity(2)
                .build();

        Order order = Order.builder()
                .id(1L)
                .user(user)
                .date(LocalDateTime.now())
                .total(BigDecimal.valueOf(200))
                .items(Collections.emptyList())
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .id(1L)
                .total(BigDecimal.valueOf(200))
                .build();

        when(cartItemRepository.findByUser(user)).thenReturn(Arrays.asList(cartItem));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        doNothing().when(cartItemRepository).deleteAll(anyList());
        when(orderMapper.toOrderResponseList(anyList())).thenReturn(Arrays.asList(orderResponse));

        assertDoesNotThrow(() -> orderService.createOrder(user));

        verify(cartItemRepository).findByUser(user);
        verify(productRepository).save(any(Product.class));
        verify(orderRepository).save(any(Order.class));
        verify(cartItemRepository).deleteAll(anyList());
    }

    @Test
    void createOrder_WithEmptyCart_ThrowsException() {
        User user = createTestUser();
        
        when(cartItemRepository.findByUser(user)).thenReturn(Collections.emptyList());

        assertThrows(BusinessException.class, () -> 
                orderService.createOrder(user));
        
        verify(cartItemRepository).findByUser(user);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_WithInsufficientStock_ThrowsException() {
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

        assertThrows(BusinessException.class, () -> 
                orderService.createOrder(user));
        
        verify(cartItemRepository).findByUser(user);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getMyOrders_WhenUserHasOrders_ReturnsOrderList() {
        User user = createTestUser();
        
        Order order1 = Order.builder()
                .id(1L)
                .user(user)
                .date(LocalDateTime.now())
                .total(BigDecimal.valueOf(200))
                .build();

        Order order2 = Order.builder()
                .id(2L)
                .user(user)
                .date(LocalDateTime.now())
                .total(BigDecimal.valueOf(150))
                .build();

        OrderResponse response1 = OrderResponse.builder().id(1L).total(BigDecimal.valueOf(200)).build();
        OrderResponse response2 = OrderResponse.builder().id(2L).total(BigDecimal.valueOf(150)).build();

        when(orderRepository.findByUserOrderByDateDesc(user))
                .thenReturn(Arrays.asList(order1, order2));
        when(orderMapper.toOrderResponseList(anyList()))
                .thenReturn(Arrays.asList(response1, response2));

        List<OrderResponse> result = orderService.getMyOrders(user);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository).findByUserOrderByDateDesc(user);
    }

    @Test
    void getMyOrders_WhenUserHasNoOrders_ReturnsEmptyList() {
        User user = createTestUser();
        
        when(orderRepository.findByUserOrderByDateDesc(user)).thenReturn(Collections.emptyList());
        when(orderMapper.toOrderResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<OrderResponse> result = orderService.getMyOrders(user);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findByUserOrderByDateDesc(user);
    }

    @Test
    void createOrder_DecrementsProductStock() {
        User user = createTestUser();
        Product product = createTestProduct();
        product.setStock(50);
        
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .user(user)
                .product(product)
                .quantity(3)
                .build();

        Order order = Order.builder()
                .id(1L)
                .user(user)
                .total(BigDecimal.valueOf(300))
                .build();

        when(cartItemRepository.findByUser(user)).thenReturn(Arrays.asList(cartItem));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        doNothing().when(cartItemRepository).deleteAll(anyList());

        orderService.createOrder(user);

        assertEquals(47, product.getStock());
        verify(productRepository).save(product);
    }

    @Test
    void createOrder_CalculatesTotalCorrectly() {
        User user = createTestUser();
        
        Product product1 = Product.builder()
                .id(1L)
                .name("Product 1")
                .price(BigDecimal.valueOf(100))
                .stock(50)
                .build();
        
        Product product2 = Product.builder()
                .id(2L)
                .name("Product 2")
                .price(BigDecimal.valueOf(50))
                .stock(50)
                .build();

        CartItem cartItem1 = CartItem.builder()
                .id(1L)
                .user(user)
                .product(product1)
                .quantity(2)
                .build();

        CartItem cartItem2 = CartItem.builder()
                .id(2L)
                .user(user)
                .product(product2)
                .quantity(3)
                .build();

        when(cartItemRepository.findByUser(user)).thenReturn(Arrays.asList(cartItem1, cartItem2));
        when(productRepository.save(any(Product.class))).thenReturn(product1);
        
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(orderCaptor.capture())).thenReturn(Order.builder().id(1L).build());
        
        doNothing().when(cartItemRepository).deleteAll(anyList());

        orderService.createOrder(user);

        Order capturedOrder = orderCaptor.getValue();
        assertEquals(BigDecimal.valueOf(350), capturedOrder.getTotal());
    }
}

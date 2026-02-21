package com.ecommerce.api.service;

import com.ecommerce.api.dto.ProductDTO;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.exception.ResourseNotFoundException;
import com.ecommerce.api.mappers.ProductMapper;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.service.impl.ProductServiceImpl;
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
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getAllProducts_WhenNoProducts_ReturnsEmptyList() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());
        when(productMapper.toDto(any(Product.class))).thenReturn(new ProductDTO());

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findAll();
    }

    @Test
    void getAllProducts_WhenProductsExist_ReturnsProductList() {
        Product product1 = Product.builder()
                .id(1L)
                .name("Laptop")
                .price(BigDecimal.valueOf(1000))
                .stock(10)
                .build();
        
        Product product2 = Product.builder()
                .id(2L)
                .name("Mouse")
                .price(BigDecimal.valueOf(25))
                .stock(50)
                .build();

        ProductDTO dto1 = ProductDTO.builder().id(1L).name("Laptop").build();
        ProductDTO dto2 = ProductDTO.builder().id(2L).name("Mouse").build();

        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));
        when(productMapper.toDto(product1)).thenReturn(dto1);
        when(productMapper.toDto(product2)).thenReturn(dto2);

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findAll();
        verify(productMapper, times(2)).toDto(any(Product.class));
    }

    @Test
    void searchProducts_WithValidName_ReturnsMatchingProducts() {
        String searchName = "Laptop";
        Product product = Product.builder()
                .id(1L)
                .name("Laptop Gaming")
                .price(BigDecimal.valueOf(1000))
                .build();
        
        ProductDTO dto = ProductDTO.builder().id(1L).name("Laptop Gaming").build();

        when(productRepository.findByNameContainingIgnoreCase(searchName))
                .thenReturn(Arrays.asList(product));
        when(productMapper.toDto(product)).thenReturn(dto);

        List<ProductDTO> result = productService.searchProducts(searchName);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().toLowerCase().contains(searchName.toLowerCase()));
        verify(productRepository).findByNameContainingIgnoreCase(searchName);
    }

    @Test
    void createProduct_WithValidData_ReturnsCreatedProduct() {
        ProductDTO requestDto = ProductDTO.builder()
                .name("New Product")
                .description("Description")
                .price(BigDecimal.valueOf(100))
                .stock(20)
                .category("Electronics")
                .build();

        Product productEntity = Product.builder()
                .name("New Product")
                .description("Description")
                .price(BigDecimal.valueOf(100))
                .stock(20)
                .category("Electronics")
                .build();

        Product savedProduct = Product.builder()
                .id(1L)
                .name("New Product")
                .description("Description")
                .price(BigDecimal.valueOf(100))
                .stock(20)
                .category("Electronics")
                .build();

        ProductDTO responseDto = ProductDTO.builder()
                .id(1L)
                .name("New Product")
                .description("Description")
                .price(BigDecimal.valueOf(100))
                .stock(20)
                .category("Electronics")
                .build();

        when(productMapper.toEntity(requestDto)).thenReturn(productEntity);
        when(productRepository.save(productEntity)).thenReturn(savedProduct);
        when(productMapper.toDto(savedProduct)).thenReturn(responseDto);

        ProductDTO result = productService.createProduct(requestDto);

        assertNotNull(result);
        assertEquals("New Product", result.getName());
        assertEquals(1L, result.getId());
        verify(productRepository).save(productEntity);
    }

    @Test
    void updateProduct_WhenProductExists_ReturnsUpdatedProduct() {
        Long productId = 1L;
        ProductDTO requestDto = ProductDTO.builder()
                .name("Updated Name")
                .description("Updated Description")
                .price(BigDecimal.valueOf(200))
                .stock(15)
                .category("Updated Category")
                .build();

        Product existingProduct = Product.builder()
                .id(productId)
                .name("Old Name")
                .description("Old Description")
                .price(BigDecimal.valueOf(100))
                .stock(10)
                .category("Old Category")
                .build();

        Product updatedProduct = Product.builder()
                .id(productId)
                .name("Updated Name")
                .description("Updated Description")
                .price(BigDecimal.valueOf(200))
                .stock(15)
                .category("Updated Category")
                .build();

        ProductDTO responseDto = ProductDTO.builder()
                .id(productId)
                .name("Updated Name")
                .description("Updated Description")
                .price(BigDecimal.valueOf(200))
                .stock(15)
                .category("Updated Category")
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        doNothing().when(productMapper).updateEntityFromDto(requestDto, existingProduct);
        when(productRepository.save(existingProduct)).thenReturn(updatedProduct);
        when(productMapper.toDto(updatedProduct)).thenReturn(responseDto);

        ProductDTO result = productService.updateProduct(productId, requestDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals(BigDecimal.valueOf(200), result.getPrice());
        verify(productRepository).findById(productId);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProduct_WhenProductNotExists_ThrowsException() {
        Long productId = 999L;
        ProductDTO requestDto = ProductDTO.builder()
                .name("Updated Name")
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourseNotFoundException.class, () -> 
                productService.updateProduct(productId, requestDto));
        
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_WhenProductExists_DeletesSuccessfully() {
        Long productId = 1L;

        when(productRepository.existsById(productId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        assertDoesNotThrow(() -> productService.deleteProduct(productId));
        
        verify(productRepository).existsById(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    void deleteProduct_WhenProductNotExists_ThrowsException() {
        Long productId = 999L;

        when(productRepository.existsById(productId)).thenReturn(false);

        assertThrows(ResourseNotFoundException.class, () -> 
                productService.deleteProduct(productId));
        
        verify(productRepository).existsById(productId);
        verify(productRepository, never()).deleteById(any());
    }
}

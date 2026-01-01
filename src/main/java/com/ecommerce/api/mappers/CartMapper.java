package com.ecommerce.api.mappers;

import com.ecommerce.api.dto.CartItemResponse;
import com.ecommerce.api.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.price", target = "Price")
    @Mapping(source = "product.imageUrl", target = "imageUrl")
    @Mapping(target = "subtotal", expression = "java(cartItem.getProduct().getPrice().multiply(java.math.BigDecimal.valueOf(cartItem.getQuantity())))")
    CartItemResponse toResponse(CartItem cartItem);

    List<CartItemResponse> toResponseList(List<CartItem> cartItems);
}

package com.ecommerce.api.mappers;

import com.ecommerce.api.dto.ProductDTO;
import com.ecommerce.api.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDTO toDto (Product product);
    Product toEntity(ProductDTO dto);

    void updateEntityFromDto(ProductDTO dto, @MappingTarget Product product);
}

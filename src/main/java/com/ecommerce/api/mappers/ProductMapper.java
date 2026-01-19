package com.ecommerce.api.mappers;

import com.ecommerce.api.dto.ProductDTO;
import com.ecommerce.api.dto.ProductRequest;
import com.ecommerce.api.dto.ProductResponse;
import com.ecommerce.api.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    //Mappers Admin
    ProductResponse toProductResponse(Product product);
    List<ProductResponse> toProductResponseList(List<Product> products);
    Product toEntity(ProductRequest productRequest);

    //Mappers User
    ProductDTO toDto (Product product);
    Product toEntity(ProductDTO dto);

    void updateEntityFromDto(ProductDTO dto, @MappingTarget Product product);
}

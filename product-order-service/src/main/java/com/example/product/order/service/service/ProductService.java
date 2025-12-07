package com.example.product.order.service.service;

import com.example.product.order.service.dto.ProductCreateRequest;
import com.example.product.order.service.dto.ProductResponse;
import com.example.product.order.service.dto.ProductUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;



public interface ProductService {
    ProductResponse createProduct(ProductCreateRequest request);

    ProductResponse getProduct(UUID id);

    Page<ProductResponse> listProducts(String status,
                                       BigDecimal minPrice,
                                       BigDecimal maxPrice,
                                       int page,
                                       int size);


    ProductResponse updateProduct(UUID id, @Valid ProductUpdateRequest req);

    ProductResponse updateStock(UUID id, int newStockId);

    void softDeleteProduct(UUID id);


}



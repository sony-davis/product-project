package com.example.product.order.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ProductUpdateRequest {

    private String name;
    private String description;

    private BigDecimal price;

    private Integer stockQty;
    private String status;

}


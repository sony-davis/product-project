package com.example.product.order.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class OrderItemResponse {
    private UUID id;
    private UUID productId;
    private Integer quantity;
    private BigDecimal priceAtOrder;
}


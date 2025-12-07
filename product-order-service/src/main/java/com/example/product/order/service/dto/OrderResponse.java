package com.example.product.order.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class OrderResponse {
    private UUID id;
    private String customerName;
    private Timestamp orderDate;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;

}


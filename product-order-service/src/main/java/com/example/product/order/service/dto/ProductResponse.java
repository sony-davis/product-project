package com.example.product.order.service.dto;

import com.example.product.order.service.entity.Product;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQty;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}


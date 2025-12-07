package com.example.product.order.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


public class ProductStockUpdateRequest {

    @NotNull
    @Min(value = 0, message = "stockQty must be >= 0")
    private Integer stockQty;
}


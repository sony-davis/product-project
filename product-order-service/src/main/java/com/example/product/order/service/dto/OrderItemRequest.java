package com.example.product.order.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class OrderItemRequest {

    @NotNull
    private UUID productId;

    @NotNull
    @Min(value = 0)
    private Integer quantity;

}

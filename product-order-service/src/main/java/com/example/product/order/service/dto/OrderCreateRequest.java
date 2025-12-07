package com.example.product.order.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;


import java.util.List;

@Setter
@Getter
public class OrderCreateRequest {

    @NotBlank
    private String customerName;
    @NotEmpty
    private List<OrderItemRequest> items;

}
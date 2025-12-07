package com.example.product.order.service.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OrderUpdateRequest {

    private String customerName;
    private List<OrderItemResponse> items;
    private String status;

}

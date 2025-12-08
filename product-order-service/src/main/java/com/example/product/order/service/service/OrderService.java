package com.example.product.order.service.service;

import com.example.product.order.service.dto.OrderCreateRequest;
import com.example.product.order.service.dto.OrderResponse;
import com.example.product.order.service.dto.OrderUpdateRequest;
import com.example.product.order.service.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.UUID;



public interface OrderService {
    OrderResponse createOrder(OrderCreateRequest request);

    OrderResponse getOrder(UUID id);


    Page<OrderResponse> listOrders(String status,String customerNameContains,LocalDate fromDate,LocalDate toDate,int page, int size);

    OrderResponse updateOrder(UUID id, @Valid OrderUpdateRequest req);

    OrderResponse updateStatus(UUID id, String status);

    void deleteOrder(UUID id);

}




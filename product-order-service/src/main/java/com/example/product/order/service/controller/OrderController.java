package com.example.product.order.service.controller;

import com.example.product.order.service.dto.*;
import com.example.product.order.service.service.OrderService;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;


@RestController
@RequestMapping("/api/orders")

public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/createOrder")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request)  {
        OrderResponse created = service.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getOrder(id));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size

    ) {
        Page<OrderResponse> result = service.listOrders(status, page, size);
        return ResponseEntity.ok(result);
    }

    @PutMapping("updateOrder/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable UUID id,
                                                  @Valid @RequestBody OrderUpdateRequest req) {
        return ResponseEntity.ok(service.updateOrder(id, req));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable UUID id,
                                                     @Valid @RequestBody OrderStatusUpdateRequest req) {
            return ResponseEntity.ok(service.updateStatus(id, req.getStatus()));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        service.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }



}
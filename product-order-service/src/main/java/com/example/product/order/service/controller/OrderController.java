package com.example.product.order.service.controller;

import com.example.product.order.service.dto.*;
import com.example.product.order.service.service.OrderService;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.UUID;


@RestController
@RequestMapping("/api/orders")

public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Creates a new order and saves it to the order table
     */
    @PostMapping("/create")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request)  {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }
    /**
     * getting an order from order table
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
    /**
     * Retrieves an order by its ID.
     */

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerNameContains,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size

    ) {
        Page<OrderResponse> result = orderService.filterOrders(status,customerNameContains,fromDate,toDate, page, size);
        return ResponseEntity.ok(result);
    }
    /**
     * Updates an existing order by ID and saves it to the order table
     */

    @PutMapping("updateOrder/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable UUID id,
                                                  @Valid @RequestBody OrderUpdateRequest req) {
        return ResponseEntity.ok(orderService.updateOrder(id, req));
    }
    /**
     * updates the status of an order and saves it to the order table
     */

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable UUID id,
                                                     @Valid @RequestBody OrderStatusUpdateRequest req) {
            return ResponseEntity.ok(orderService.updateStatus(id, req.getStatus()));
    }
    /**
     * deletes an order by ID
     */
    @DeleteMapping("deleteOrder/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }


}
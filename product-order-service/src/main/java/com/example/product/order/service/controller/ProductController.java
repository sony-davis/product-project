package com.example.product.order.service.controller;

import com.example.product.order.service.dto.ProductCreateRequest;
import com.example.product.order.service.dto.ProductResponse;
import com.example.product.order.service.dto.ProductStockUpdateRequest;
import com.example.product.order.service.dto.ProductUpdateRequest;
import com.example.product.order.service.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")

public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest req) {
        ProductResponse created = service.createProduct(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getProduct(id));
    }



    @GetMapping
    public ResponseEntity<Page<ProductResponse>> listProducts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ProductResponse> result = service.listProducts(status, minPrice, maxPrice, page, size);
        return ResponseEntity.ok(result);
    }



    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody ProductUpdateRequest req) {
        return ResponseEntity.ok(service.updateProduct(id, req));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(@PathVariable UUID id,
                                                       @Valid @RequestBody ProductStockUpdateRequest req) {
        return ResponseEntity.ok(service.updateStock(id, req.getStockQty()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        service.softDeleteProduct(id);
        return ResponseEntity.noContent().build();
    }


}
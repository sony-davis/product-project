package com.example.product.order.service.controller;

import com.example.product.order.service.dto.ProductCreateRequest;
import com.example.product.order.service.dto.ProductResponse;
import com.example.product.order.service.dto.ProductStockUpdateRequest;
import com.example.product.order.service.dto.ProductUpdateRequest;
import com.example.product.order.service.service.ProductService;
import com.example.product.order.service.serviceImpl.ProductServiceImpl;
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

    private final ProductService productService;

    public ProductController(ProductService productService) {

        this.productService = productService;
    }


/**
 * Creates a new product and saves it to the product table
*/

 @PostMapping("/create")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(req));
    }

    /**
     * getting a product from product table
     */

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    /**
     * Retrieves a product by its ID.
     */

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> listProducts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String nameContains,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ProductResponse> result = productService.filterProducts(status, minPrice, maxPrice, nameContains,page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * Updates an existing product by ID and saves it to the product table
     */

    @PutMapping("updateProduct/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody ProductUpdateRequest req) {
        return ResponseEntity.ok(productService.updateProduct(id, req));
    }

    /**
     * updates the stock of a product and saves it to the product table
     */

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(@PathVariable UUID id,
                                                       @Valid @RequestBody ProductStockUpdateRequest req) {
        return ResponseEntity.ok(productService.updateStock(id, req.getStockQty()));
    }

    /**
     * Soft deletes a product by ID (marks as inactive instead of removing from DB)
     */

    @DeleteMapping("deleteProduct/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        productService.softDeleteProduct(id);
        return ResponseEntity.noContent().build();
    }


}
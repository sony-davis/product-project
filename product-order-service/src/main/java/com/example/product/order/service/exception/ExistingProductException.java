package com.example.product.order.service.exception;

public class ExistingProductException extends RuntimeException {
    public ExistingProductException(String message) {
        super(message);
    }
}

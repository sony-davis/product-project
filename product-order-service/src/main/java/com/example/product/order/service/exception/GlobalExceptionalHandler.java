package com.example.product.order.service.exception;

import com.example.product.order.service.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice

public class GlobalExceptionalHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductException(ProductNotFoundException ex, HttpServletRequest req) {
        ErrorResponse err = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderException(OrderNotFoundException ex, HttpServletRequest req) {
        ErrorResponse err = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(ExistingProductException.class)
    public ResponseEntity<ErrorResponse> handleExistingProduct(ExistingProductException ex, HttpServletRequest req) {
        ErrorResponse err = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        ErrorResponse err = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ErrorResponse> handleOutOfStock(OutOfStockException ex, HttpServletRequest req) {
        ErrorResponse err = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

}

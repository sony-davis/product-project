package com.example.product.order.service.serviceImpl;

import com.example.product.order.service.dto.ProductCreateRequest;
import com.example.product.order.service.dto.ProductResponse;
import com.example.product.order.service.dto.ProductUpdateRequest;
import com.example.product.order.service.entity.Product;
import com.example.product.order.service.exception.BadRequestException;
import com.example.product.order.service.exception.ExistingProductException;
import com.example.product.order.service.exception.ProductNotFoundException;
import com.example.product.order.service.repository.ProductRepository;
import com.example.product.order.service.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {

        this.productRepository = productRepository;
    }

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
    }

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {


        Product p = new Product();
        p.setName(request.getName().trim());
        p.setDescription(request.getDescription());
        p.setPrice(request.getPrice());
        p.setStockQty(request.getStockQty());
        p.setStatus("ACTIVE");
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        Product saved = productRepository.save(p);
        return toResponse(saved);
    }

    @Override
    public ProductResponse getProduct(UUID id) {
        Product p = findProductOrThrow(id);

        return toResponse(p);
    }

    @Override
    public Page<ProductResponse> listProducts(String status,
                                              BigDecimal minPrice,
                                              BigDecimal maxPrice,
                                              String nameContains,
                                              int page,
                                              int size) {


        String statusParam = (StringUtils.hasText(status)) ? status.trim() : null;


        int pageNumber = Math.max(0, page);
        int pageSize = Math.max(1, size);

        PageRequest pageable = PageRequest.of(pageNumber, pageSize);

        Page<Product> productPage = productRepository.findByFilters(statusParam, minPrice, maxPrice, nameContains,pageable);

        return productPage.map(this::toResponse);
    }


    @Override
    public ProductResponse updateProduct(UUID id, ProductUpdateRequest request) {

        Product p = findProductOrThrow(id);

        if (request.getName() != null &&!request.getName().equalsIgnoreCase(p.getName())) {
            if (productRepository.existsByNameIgnoreCase(request.getName().trim())) {
                throw new ExistingProductException("Product Already Exists");
            }
            p.setName(request.getName().trim());
        }

        if (request.getDescription() != null)
            p.setDescription(request.getDescription());

        if (request.getPrice() != null) {
            if (request.getPrice().signum() <= 0) {
                throw new BadRequestException("Price must be greater than 0");
            }
            p.setPrice(request.getPrice());
        }
        if (request.getStockQty() != null) {
            if (request.getStockQty() < 0) {
                throw new BadRequestException("Quantity must be a positive number");
            }
            p.setStockQty(request.getStockQty());
        }
        if (request.getStatus() != null)
            p.setStatus(request.getStatus());


        p.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        Product saved = productRepository.save(p);
        return toResponse(saved);
    }


    @Override
    public ProductResponse updateStock(UUID id, int newStockId) {
        Product p = findProductOrThrow(id);

            p.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
            Product saved = productRepository.save(p);
            return toResponse(saved);
        }

    @Override
    public void softDeleteProduct(UUID id){
        Product p = findProductOrThrow(id);

        p.setStatus("INACTIVE");
        p.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        productRepository.save(p);

    }

    private ProductResponse toResponse(Product p) {
        ProductResponse r = new ProductResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setDescription(p.getDescription());
        r.setPrice(p.getPrice());
        r.setStockQty(p.getStockQty());
        r.setStatus(p.getStatus());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
    }
}
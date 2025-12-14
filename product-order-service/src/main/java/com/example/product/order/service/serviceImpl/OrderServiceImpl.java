package com.example.product.order.service.serviceImpl;

import com.example.product.order.service.dto.*;
import com.example.product.order.service.entity.Order;
import com.example.product.order.service.entity.OrderItem;
import com.example.product.order.service.entity.Product;
import com.example.product.order.service.exception.BadRequestException;
import com.example.product.order.service.exception.OrderNotFoundException;
import com.example.product.order.service.exception.OutOfStockException;
import com.example.product.order.service.exception.ProductNotFoundException;
import com.example.product.order.service.repository.OrderRepository;
import com.example.product.order.service.repository.ProductRepository;
import com.example.product.order.service.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;

    public OrderServiceImpl(OrderRepository orderRepo, ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    private Order findOrderOrThrow(UUID id) {
       return orderRepo.findById(id)
                .orElseThrow(() -> new OrderNotFoundException( "Order not found"));
    }


    @Override
    public OrderResponse createOrder(OrderCreateRequest request) {


        BigDecimal total = BigDecimal.ZERO;
        Order order = new Order();
        order.setCustomerName(request.getCustomerName().trim());
        order.setOrderDate(Timestamp.from(Instant.now()));
        order.setStatus("CREATED");


        for (OrderItemRequest ir : request.getItems()) {
            if (ir.getProductId() == null) {
                throw new BadRequestException("Product ID is required");
            }
            if (ir.getQuantity() == null || ir.getQuantity() <= 0) {
                throw new BadRequestException("Quantity is required");
            }

            UUID productId = ir.getProductId();
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product not found"));

            if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
                throw new BadRequestException("Product status must be ACTIVE");
            }

            BigDecimal priceAtOrder = product.getPrice();
            BigDecimal pr = priceAtOrder.multiply(BigDecimal.valueOf(ir.getQuantity()));
            total = total.add(pr);

            OrderItem oi = new OrderItem();
            oi.setProductId(productId);
            oi.setQuantity(ir.getQuantity());
            oi.setPriceAtOrder(priceAtOrder);

            order.addItem(oi);
        }

        order.setTotalAmount(total);
        orderRepo.save(order);
        return mapToResponse(order);
    }

    @Override

    public OrderResponse getOrderById(UUID id) {
        Order order = findOrderOrThrow(id);
        return mapToResponse(order);
    }

    @Override
    public Page<OrderResponse> filterOrders(String status,String customerNameContains,LocalDate fromDate,LocalDate toDate,
                                          int page, int size) {

        List<Order> allOrders = orderRepo.findAll();

        List<OrderResponse> filtered = allOrders.stream()
                .filter(o -> status == null || o.getStatus() == null || o.getStatus().equalsIgnoreCase(status))

                .filter(o -> {
                    if (!StringUtils.hasText(customerNameContains)) return true;
                    if (o.getCustomerName() == null)
                        return false;
                    return o.getCustomerName()
                            .toLowerCase()
                            .contains(customerNameContains.toLowerCase());
                })

                .filter(o -> {
                    if (fromDate == null) return true;
                    if (o.getOrderDate() == null) return false;
                    LocalDate orderLocalDate = o.getOrderDate().toLocalDateTime().toLocalDate();
                    return !orderLocalDate.isBefore(fromDate);
                })
                .filter(o -> {
                    if (toDate == null) return true;
                    if (o.getOrderDate() == null) return false;
                    LocalDate orderLocalDate = o.getOrderDate().toLocalDateTime().toLocalDate();
                    return !orderLocalDate.isAfter(toDate);
                })
                .map(this::mapToResponse)
                .toList();

        int pageNumber = Math.max(0, page);
        int pageSize = Math.max(1, size);

        int fromIndex = Math.min(pageNumber * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());

        List<OrderResponse> pageContent = (fromIndex >= toIndex) ? Collections.emptyList() : filtered.subList(fromIndex, toIndex);

        return new PageImpl<>(pageContent, PageRequest.of(pageNumber, pageSize), filtered.size());
    }


    @Override
    public OrderResponse updateOrder(UUID id, OrderUpdateRequest request) {
        Order o = findOrderOrThrow(id);

        if (request.getCustomerName() != null) {
            if (!"CREATED".equalsIgnoreCase(o.getStatus())) {
                throw new BadRequestException("Customer name must be updated only if the status is CREATED");
            }

            String name = request.getCustomerName().trim();
            if (name.isEmpty()) {
                throw new BadRequestException("Customer name Cannot be Empty");
            }
            o.setCustomerName(name);
        }

        if (request.getStatus() != null)
            o.setStatus(request.getStatus());

        o.setOrderDate(Timestamp.valueOf(LocalDateTime.now()));
        Order saved = orderRepo.save(o);
        return mapToResponse(saved);

    }

    @Override
    public OrderResponse updateStatus(UUID id, String newStatus) {
        Order order = findOrderOrThrow(id);

        String currentStatus = order.getStatus();
        newStatus = newStatus.toUpperCase();

        if ("CREATED".equalsIgnoreCase(currentStatus)) {
            if (!newStatus.equals("CONFIRMED") && !newStatus.equals("CANCELLED")) {
                throw new BadRequestException("Invalid transition");
            }
        } else {
            throw new BadRequestException("Cannot change status when order is in status: " + currentStatus);
        }


        if (newStatus.equals("CONFIRMED")) {
            for (OrderItem item : order.getItems()) {

                Product p = productRepo.findById(item.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException("Product not found"));

                if (p.getStockQty() < item.getQuantity()) {
                    throw new OutOfStockException("Insufficient stock for product");
                }
            }

            for (OrderItem item : order.getItems()) {
                Product p = productRepo.findById(item.getProductId()).get();
                p.setStockQty(p.getStockQty() - item.getQuantity());
                productRepo.save(p);
            }
        }


        order.setStatus(newStatus);
        order.setOrderDate(Timestamp.valueOf(LocalDateTime.now()));

        Order saved = orderRepo.save(order);
        return mapToResponse(saved);
    }

    @Override
    public void deleteOrder(UUID id) {
        Order order = findOrderOrThrow(id);

        String status = order.getStatus().toUpperCase();

        if (!status.equals("CREATED") && !status.equals("CANCELLED")) {
            throw new BadRequestException("Cannot delete order");
        }
        orderRepo.delete(order);
    }


    private OrderResponse mapToResponse(Order order) {
        OrderResponse resp = new OrderResponse();
        resp.setId(order.getId());
        resp.setCustomerName(order.getCustomerName());
        resp.setOrderDate(order.getOrderDate());
        resp.setStatus(order.getStatus());
        resp.setTotalAmount(order.getTotalAmount());

        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> {
            OrderItemResponse r = new OrderItemResponse();
            r.setId(item.getId());
            r.setProductId(item.getProductId());
            r.setQuantity(item.getQuantity());
            r.setPriceAtOrder(item.getPriceAtOrder());
            return r;
        }).collect(Collectors.toList());

        resp.setItems(itemResponses);
        return resp;
    }

}
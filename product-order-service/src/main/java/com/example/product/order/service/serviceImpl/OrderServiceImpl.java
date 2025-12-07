package com.example.product.order.service.serviceImpl;

import com.example.product.order.service.dto.*;
import com.example.product.order.service.entity.Order;
import com.example.product.order.service.entity.OrderItem;
import com.example.product.order.service.entity.Product;
import com.example.product.order.service.repository.OrderItemRepository;
import com.example.product.order.service.repository.OrderRepository;
import com.example.product.order.service.repository.ProductRepository;
import com.example.product.order.service.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final ProductRepository productRepo;

    public OrderServiceImpl(OrderRepository orderRepo,
                            OrderItemRepository itemRepo,
                            ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.productRepo = productRepo;
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
                throw new ResponseStatusException(BAD_REQUEST);
            }
            if (ir.getQuantity() == null || ir.getQuantity() <= 0) {
                throw new ResponseStatusException(BAD_REQUEST);
            }

            UUID productId = ir.getProductId();
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

            if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
                throw new ResponseStatusException(BAD_REQUEST);
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

    public OrderResponse getOrder(UUID id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "ORDER_NOT_FOUND"));
        List<OrderItem> items = itemRepo.findByOrderId(id);
        return mapToResponse(order);
    }

    @Override
    public Page<OrderResponse> listOrders(String status,
                                          int page, int size) {


        String statusParam = null;
        if (StringUtils.hasText(status)) {
            statusParam = status.trim();
        }


//        LocalDateTime fromDateParam = null;
//        LocalDateTime toDateParam = null;




        int pageNumber = Math.max(0, page);
        int pageSize = Math.max(1, size);

        PageRequest pageable = PageRequest.of(pageNumber, pageSize);

        Page<Order> orderPage = orderRepo.findByFilters(
                statusParam,
                pageable);

        return orderPage.map(this::mapToResponse);
    }


    @Override
    public OrderResponse updateOrder(UUID id, OrderUpdateRequest request) {
        Order o = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        if (request.getCustomerName() != null) {
            if (!"CREATED".equalsIgnoreCase(o.getStatus())) {
                throw new ResponseStatusException(BAD_REQUEST);
            }

            String name = request.getCustomerName().trim();
            if (name.isEmpty()) {
                throw new ResponseStatusException(BAD_REQUEST);
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
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "ORDER_NOT_FOUND"));

        String currentStatus = order.getStatus();
        newStatus = newStatus.toUpperCase();

        if ("CREATED".equalsIgnoreCase(currentStatus)) {
            if (!newStatus.equals("CONFIRMED") && !newStatus.equals("CANCELLED")) {
                throw new ResponseStatusException(BAD_REQUEST, "Invalid transition");
            }
        } else {
            throw new ResponseStatusException(
                    BAD_REQUEST, "Cannot change status when order is in status: " + currentStatus);
        }


        if (newStatus.equals("CONFIRMED")) {
            for (OrderItem item : order.getItems()) {

                Product p = productRepo.findById(item.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "PRODUCT_NOT_FOUND"));

                if (p.getStockQty() < item.getQuantity()) {
                    throw new ResponseStatusException(
                            BAD_REQUEST, "Insufficient stock for product");
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
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "ORDER_NOT_FOUND"));

        String status = order.getStatus().toUpperCase();

        if (!status.equals("CREATED") && !status.equals("CANCELLED")) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Cannot delete order");
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
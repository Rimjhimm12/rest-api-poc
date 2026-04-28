package com.learn.restapi.controller;

import com.learn.restapi.exception.ResourceNotFoundException;
import com.learn.restapi.model.Order;
import com.learn.restapi.model.Product;
import com.learn.restapi.service.OrderService;
import com.learn.restapi.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Place and manage customer orders — requires authentication")
@SecurityRequirement(name = "basicAuth")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;

    public OrderController(OrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "List all orders")
    public ResponseEntity<List<Order>> listOrders() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by ID")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return ResponseEntity.ok(order);
    }

    @PostMapping
    @Operation(summary = "Place a new order")
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody Order order) {
        Product product = productService.findById(order.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", order.getProductId()));

        if (product.getStock() < order.getQuantity()) {
            throw new IllegalArgumentException(
                    "Not enough stock. Available: " + product.getStock() + ", Requested: " + order.getQuantity());
        }

        Order placed = orderService.create(order, product);
        return ResponseEntity.status(HttpStatus.CREATED).body(placed);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status (ADMIN only)")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String statusStr = body.get("status");
        if (statusStr == null) {
            throw new IllegalArgumentException("'status' field is required");
        }

        Order.Status newStatus;
        try {
            newStatus = Order.Status.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Valid values: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED");
        }

        Order updated = orderService.updateStatus(id, newStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return ResponseEntity.ok(updated);
    }
}

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

    // -------------------------------------------------------------------------
    // PATTERN 5 — Enum path variable
    //
    // /api/orders/status/{status}
    //
    // Spring automatically converts the string in the URL (e.g. "PENDING") to
    // the Java enum Order.Status. If the value doesn't match any enum constant
    // the request is rejected with 400 — handled by GlobalExceptionHandler.
    //
    // "status" is a literal prefix → Spring resolves this BEFORE /{id}.
    // -------------------------------------------------------------------------
    @GetMapping("/status/{status}")
    @Operation(summary = "List orders filtered by status (PATTERN 5: enum path variable)")
    public ResponseEntity<List<Order>> getByStatus(@PathVariable Order.Status status) {
        List<Order> result = orderService.findByStatus(status);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.size()))
                .body(result);
    }

    // -------------------------------------------------------------------------
    // PATTERN 6 — Nested resource + descriptive variable name
    //
    // /api/orders/{orderId}/summary
    //
    // Named {orderId} (not {id}) to make intent clear when multiple resources
    // appear in the same URL. Returns a lightweight summary map rather than
    // the full Order object — useful when the caller only needs a few fields.
    // -------------------------------------------------------------------------
    @GetMapping("/{orderId}/summary")
    @Operation(summary = "Get a brief order summary (PATTERN 6: nested resource + named path variable)")
    public ResponseEntity<Map<String, Object>> getOrderSummary(@PathVariable Long orderId) {
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return ResponseEntity.ok(Map.of(
                "orderId",    order.getId(),
                "productId",  order.getProductId(),
                "quantity",   order.getQuantity(),
                "totalPrice", order.getTotalPrice(),
                "status",     order.getStatus()
        ));
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

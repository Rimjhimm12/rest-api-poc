package com.learn.restapi.service;

import com.learn.restapi.model.Order;
import com.learn.restapi.model.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final List<Order> orders = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<Order> findAll() {
        return orders;
    }

    public Optional<Order> findById(Long id) {
        return orders.stream().filter(o -> o.getId().equals(id)).findFirst();
    }

    public Order create(Order order, Product product) {
        double total = product.getPrice() * order.getQuantity();
        Order saved = new Order(idCounter.getAndIncrement(), order.getProductId(), order.getQuantity(), total);
        orders.add(saved);
        return saved;
    }

    public Optional<Order> updateStatus(Long id, Order.Status status) {
        return findById(id).map(order -> {
            order.setStatus(status);
            return order;
        });
    }
}

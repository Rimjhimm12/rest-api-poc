package com.learn.restapi.service;

import com.learn.restapi.model.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public ProductService() {
        products.add(new Product(idCounter.getAndIncrement(), "iPhone 15 Pro",
                "Apple smartphone with A17 chip", 1099.99, "Electronics", 50));
        products.add(new Product(idCounter.getAndIncrement(), "Samsung 55\" 4K TV",
                "Crystal clear 4K UHD Smart TV", 799.99, "Electronics", 20));
        products.add(new Product(idCounter.getAndIncrement(), "Nike Air Max 270",
                "Lightweight running shoes with air cushioning", 89.99, "Footwear", 120));
        products.add(new Product(idCounter.getAndIncrement(), "The Pragmatic Programmer",
                "Classic software engineering book by Andy Hunt", 42.99, "Books", 200));
        products.add(new Product(idCounter.getAndIncrement(), "Breville Coffee Maker",
                "12-cup programmable coffee maker with thermal carafe", 79.99, "Kitchen", 75));
        products.add(new Product(idCounter.getAndIncrement(), "Sony WH-1000XM5",
                "Industry-leading noise cancelling headphones", 349.99, "Electronics", 30));
        products.add(new Product(idCounter.getAndIncrement(), "Levi's 501 Jeans",
                "Classic straight fit denim jeans", 59.99, "Clothing", 300));
    }

    public List<Product> findAll(String category, Double minPrice, Double maxPrice) {
        return products.stream()
                .filter(p -> category == null || p.getCategory().equalsIgnoreCase(category))
                .filter(p -> minPrice == null || p.getPrice() >= minPrice)
                .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    public Optional<Product> findById(Long id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public Product create(Product product) {
        product.setId(idCounter.getAndIncrement());
        products.add(product);
        return product;
    }

    public Optional<Product> update(Long id, Product updated) {
        return findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setPrice(updated.getPrice());
            existing.setCategory(updated.getCategory());
            existing.setStock(updated.getStock());
            return existing;
        });
    }

    public boolean delete(Long id) {
        return products.removeIf(p -> p.getId().equals(id));
    }

    public int count() {
        return products.size();
    }
}

package com.learn.restapi.controller;

import com.learn.restapi.exception.ResourceNotFoundException;
import com.learn.restapi.model.Product;
import com.learn.restapi.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Browse and manage e-commerce products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // -------------------------------------------------------------------------
    // CONCEPT 1 & 2: Simple GET — works in browser AND Postman
    // CONCEPT 7: Query parameters — filter by category and price range
    // CONCEPT 4 (response side): X-Total-Count header in the response
    // -------------------------------------------------------------------------
    @GetMapping
    @Operation(summary = "List all products",
               description = "Returns all products. Optionally filter by category, minPrice, maxPrice.")
    public ResponseEntity<List<Product>> listProducts(
            @Parameter(description = "Filter by category (e.g. Electronics, Footwear)")
            @RequestParam(required = false) String category,

            @Parameter(description = "Minimum price filter")
            @RequestParam(required = false) Double minPrice,

            @Parameter(description = "Maximum price filter")
            @RequestParam(required = false) Double maxPrice,

            // CONCEPT 4 (request side): read a custom header from the caller
            @RequestHeader(value = "X-Request-ID", required = false) String requestId
    ) {
        List<Product> result = productService.findAll(category, minPrice, maxPrice);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.size()));

        // Echo back the caller's request ID if they sent one
        if (requestId != null) {
            headers.add("X-Request-ID", requestId);
            headers.add("main", "gandu");
        }

        return ResponseEntity.ok().headers(headers).body(result);
    }

    // -------------------------------------------------------------------------
    // CONCEPT 1 & 2: GET by ID — demonstrates path variables
    // CONCEPT 6: Returns 404 with a structured error body for unknown IDs
    // -------------------------------------------------------------------------
    @GetMapping("/{id}")
    @Operation(summary = "Get a single product by ID")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return ResponseEntity.ok(product);
    }

    // -------------------------------------------------------------------------
    // CONCEPT 3: POST with JSON body
    // CONCEPT 4: Requires Content-Type: application/json header
    // CONCEPT 6: Returns 400 if body is invalid (validation annotations on Product)
    // CONCEPT 7 (auth): Requires ADMIN role via Basic Auth
    // -------------------------------------------------------------------------
    @PostMapping
    @Operation(summary = "Create a new product (ADMIN only)",
               security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product created = productService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // -------------------------------------------------------------------------
    // PUT — update an existing product
    // -------------------------------------------------------------------------
    @PutMapping("/{id}")
    @Operation(summary = "Update a product (ADMIN only)",
               security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {
        Product updated = productService.update(id, product)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return ResponseEntity.ok(updated);
    }

    // -------------------------------------------------------------------------
    // DELETE — returns 204 No Content on success, 404 if not found
    // -------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product (ADMIN only)",
               security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!productService.delete(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        return ResponseEntity.noContent().build();
    }
}

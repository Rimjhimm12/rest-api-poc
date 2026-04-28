package com.learn.restapi.examples;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

/**
 * CONCEPT 9 — Calling a REST API from Java code.
 *
 * Run this class directly (right-click → Run in IntelliJ) while the server is up.
 * It uses only the Java standard library — no external dependencies needed.
 */
public class ApiClientExample {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        System.out.println("=== CONCEPT 9: Calling REST API from Java ===\n");

        getProducts();
        getProductById(1L);
        getProductsWithQueryParams("Electronics", 100.0, 500.0);
        createProduct();
        placeOrder();
    }

    // --- GET all products (no auth needed) ---
    static void getProducts() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/products"))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("GET /api/products");
        System.out.println("Status: " + response.statusCode());
        System.out.println("X-Total-Count header: " + response.headers().firstValue("X-Total-Count").orElse("(not present)"));
        System.out.println("Body (first 300 chars): " + response.body().substring(0, Math.min(300, response.body().length())));
        System.out.println();
    }

    // --- GET single product by ID ---
    static void getProductById(Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/products/" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("GET /api/products/" + id);
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        System.out.println();
    }

    // --- GET with query parameters ---
    static void getProductsWithQueryParams(String category, double minPrice, double maxPrice) throws Exception {
        String url = BASE_URL + "/api/products?category=" + category + "&minPrice=" + minPrice + "&maxPrice=" + maxPrice;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("GET /api/products?category=" + category + "&minPrice=" + minPrice + "&maxPrice=" + maxPrice);
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        System.out.println();
    }

    // --- POST with JSON body + Basic Auth (admin only) ---
    static void createProduct() throws Exception {
        String json = """
                {
                  "name": "Sony PlayStation 5",
                  "description": "Next-gen gaming console",
                  "price": 499.99,
                  "category": "Electronics",
                  "stock": 15
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/products"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", basicAuth("admin", "password123"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());



        System.out.println("POST /api/products (with auth)");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        System.out.println();
    }

    // --- POST an order (any authenticated user) ---
    static void placeOrder() throws Exception {
        String json = """
                {
                  "productId": 3,
                  "quantity": 2
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/orders"))
                .header("Content-Type", "application/json")
                .header("Authorization", basicAuth("user", "user123"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("POST /api/orders (as regular user)");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
        System.out.println();
    }

    // Encodes username:password as Base64 for HTTP Basic Auth
    private static String basicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}

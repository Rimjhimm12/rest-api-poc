  package com.learn.restapi.restassured;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * REST-assured version of ApiClientExample.java.
 *
 * PREREQUISITE: Start the server first → mvn spring-boot:run
 * Then run this class: mvn test -Dtest=ECommerceApiTest
 *
 * Each test maps directly to a concept from the GUIDE.md and mirrors
 * what ApiClientExample.java did with raw HttpClient — but with built-in
 * assertions and automatic request/response logging on failure.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ECommerceApiTest {

    // -------------------------------------------------------------------------
    // Setup — equivalent to: private static final String BASE_URL = "http://localhost:8080";
    // -------------------------------------------------------------------------
    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/api";

        // Log the full request + response whenever a test fails.
        // In HttpClient you had System.out.println — this does it automatically.
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    // =========================================================================
    // SECTION 1 — CONCEPT 1 & 2: GET requests (happy path)
    // =========================================================================

    /**
     * HttpClient equivalent:
     *   client.send(GET "/api/products", ...)
     *   assert response.statusCode() == 200
     */
    @Test
    @Order(1)
    void getAllProducts_returns200AndNonEmptyList() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/products")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", not(empty()))          // array is not empty
            .body("size()", greaterThan(0));  // at least 1 product
    }

    @Test
    @Order(2)
    void getProductById_returns200WithCorrectProduct() {
        // HttpClient equivalent:
        //   client.send(GET "/api/products/1", ...)
        //   assert response.statusCode() == 200

        given()
            .accept(ContentType.JSON)
        .when()
            .get("/products/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("name", notNullValue())
            .body("price", greaterThan(0f));
    }

    // =========================================================================
    // SECTION 2 — CONCEPT 7: Query parameters
    // =========================================================================

    /**
     * HttpClient equivalent:
     *   URI.create(BASE_URL + "/api/products?category=Electronics&minPrice=100&maxPrice=500")
     */
    @Test
    @Order(3)
    void getProducts_filterByCategory_returnsOnlyMatchingProducts() {
        given()
            .accept(ContentType.JSON)
            .queryParam("category", "Electronics")
        .when()
            .get("/products")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            // every returned product must belong to Electronics
            .body("category", everyItem(equalToIgnoringCase("Electronics")));
    }

    @Test
    @Order(4)
    void getProducts_filterByPriceRange_returnsOnlyProductsInRange() {
        float minPrice = 50f;
        float maxPrice = 200f;

        given()
            .accept(ContentType.JSON)
            .queryParam("minPrice", minPrice)
            .queryParam("maxPrice", maxPrice)
        .when()
            .get("/products")
        .then()
            .statusCode(200)
            .body("price", everyItem(greaterThanOrEqualTo(minPrice)))
            .body("price", everyItem(lessThanOrEqualTo(maxPrice)));
    }

    @Test
    @Order(5)
    void getProducts_filterByUnknownCategory_returnsEmptyList() {
        given()
            .queryParam("category", "Dinosaurs")
        .when()
            .get("/products")
        .then()
            .statusCode(200)           // 200, not 404 — the endpoint exists
            .body("$", empty());       // but the result list is empty
    }

    // =========================================================================
    // SECTION 3 — CONCEPT 4: Headers (request + response)
    // =========================================================================

    @Test
    @Order(6)
    void getProducts_responseContainsTotalCountHeader() {
        // HttpClient equivalent:
        //   response.headers().firstValue("X-Total-Count").orElse("(not present)")

        given()
        .when()
            .get("/products")
        .then()
            .statusCode(200)
            .header("X-Total-Count", notNullValue());
    }

    @Test
    @Order(7)
    void getProducts_requestIdHeaderIsEchoedBack() {
        // Send a custom header → verify the server echoes it in the response.
        // HttpClient equivalent:
        //   request.header("X-Request-ID", "test-run-001")
        //   response.headers().firstValue("X-Request-ID")

        given()
            .header("X-Request-ID", "test-run-001")
        .when()
            .get("/products")
        .then()
            .statusCode(200)
            .header("X-Request-ID", equalTo("test-run-001"));
    }

    // =========================================================================
    // SECTION 4 — CONCEPT 3: POST with JSON body (happy path)
    // =========================================================================

    /**
     * HttpClient equivalent:
     *   request.header("Content-Type", "application/json")
     *          .header("Authorization", basicAuth("admin", "password123"))
     *          .POST(BodyPublishers.ofString(json))
     *
     * REST-assured does auth and content-type in one line each.
     */
    @Test
    @Order(8)
    void createProduct_asAdmin_returns201WithCreatedProduct() {
        String body = """
                {
                  "name": "Sony PlayStation 5",
                  "description": "Next-gen gaming console with DualSense controller",
                  "price": 499.99,
                  "category": "Electronics",
                  "stock": 25
                }
                """;

        given()
            .contentType(ContentType.JSON)   // sets Content-Type: application/json
            .accept(ContentType.JSON)
            .auth().basic("admin", "password123")
            .body(body)
        .when()
            .post("/products")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Sony PlayStation 5"))
            .body("price", equalTo(499.99f))
            .body("category", equalTo("Electronics"));
    }

    @Test
    @Order(9)
    void placeOrder_asUser_returns201WithCalculatedTotal() {
        // HttpClient equivalent:
        //   request.header("Authorization", basicAuth("user", "user123"))
        //          .POST(BodyPublishers.ofString("""{ "productId": 3, "quantity": 2 }"""))

        String body = """
                {
                  "productId": 3,
                  "quantity": 2
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .auth().basic("user", "user123")
            .body(body)
        .when()
            .post("/orders")
        .then()
            .statusCode(201)
            .body("productId", equalTo(3))
            .body("quantity", equalTo(2))
            .body("totalPrice", greaterThan(0f))
            .body("status", equalTo("PENDING"));
    }

    // =========================================================================
    // SECTION 5 — CONCEPT 9: Authentication — 401 vs 403 scenarios
    // =========================================================================

    @Test
    @Order(10)
    void createProduct_withNoAuth_returns401() {
        // No .auth() call = no Authorization header = 401 Unauthorized
        given()
            .contentType(ContentType.JSON)
            .body("{ \"name\": \"Test\", \"description\": \"x\", \"price\": 10.0, \"category\": \"X\", \"stock\": 1 }")
        .when()
            .post("/products")
        .then()
            .statusCode(401);
    }

    @Test
    @Order(11)
    void createProduct_asUser_returns403() {
        // Logged in as USER (not ADMIN) → authenticated but not authorized
        given()
            .contentType(ContentType.JSON)
            .auth().basic("user", "user123")
            .body("{ \"name\": \"Test\", \"description\": \"x\", \"price\": 10.0, \"category\": \"X\", \"stock\": 1 }")
        .when()
            .post("/products")
        .then()
            .statusCode(403);
    }

    @Test
    @Order(12)
    void deleteProduct_withNoAuth_returns401() {
        given()
        .when()
            .delete("/products/1")
        .then()
            .statusCode(401);
    }

    @Test
    @Order(13)
    void deleteProduct_asUser_returns403() {
        given()
            .auth().basic("user", "user123")
        .when()
            .delete("/products/1")
        .then()
            .statusCode(403);
    }

    @Test
    @Order(14)
    void getOrders_withNoAuth_returns401() {
        given()
        .when()
            .get("/orders")
        .then()
            .statusCode(401);
    }

    // =========================================================================
    // SECTION 6 — CONCEPT 6 & 8: Error scenarios (negative testing)
    // =========================================================================

    @Test
    @Order(15)
    void getProductById_withUnknownId_returns404WithErrorBody() {
        // Equivalent to: GET /api/products/9999 in Postman → inspect structured error

        given()
            .accept(ContentType.JSON)
        .when()
            .get("/products/9999")
        .then()
            .statusCode(404)
            .body("status", equalTo(404))
            .body("error", equalTo("Not Found"))
            .body("message", containsString("9999"))
            .body("timestamp", notNullValue());
    }

    @Test
    @Order(16)
    void createProduct_withMissingContentTypeHeader_returns415() {
        // This is the bug that caused your 500 in Postman.
        // After the fix, missing Content-Type now correctly returns 415.

        given()
            .auth().basic("admin", "password123")
            // intentionally NOT setting .contentType(ContentType.JSON)
            .body("{ \"name\": \"Test\", \"price\": 99.99 }")
        .when()
            .post("/products")
        .then()
            .statusCode(415)
            .body("error", equalTo("Unsupported Media Type"))
            .body("message", containsString("Content-Type"));
    }

    @Test
    @Order(17)
    void createProduct_withMalformedJson_returns400() {
        given()
            .contentType(ContentType.JSON)
            .auth().basic("admin", "password123")
            .body("{ \"name\": \"Broken, \"price\": 99 }")  // missing closing quote
        .when()
            .post("/products")
        .then()
            .statusCode(400)
            .body("error", equalTo("Bad Request"))
            .body("message", containsString("Malformed JSON"));
    }

    @Test
    @Order(18)
    void createProduct_withInvalidFields_returns400WithFieldErrors() {
        // Sends a body that fails @NotBlank / @Positive validation.
        // Maps to: GlobalExceptionHandler.handleValidation()

        String invalidBody = """
                {
                  "name": "",
                  "description": "",
                  "price": -50,
                  "category": "",
                  "stock": -1
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .auth().basic("admin", "password123")
            .body(invalidBody)
        .when()
            .post("/products")
        .then()
            .statusCode(400)
            .body("error", equalTo("Validation Failed"))
            .body("fieldErrors.name", notNullValue())
            .body("fieldErrors.price", notNullValue())
            .body("fieldErrors.category", notNullValue());
    }

    @Test
    @Order(19)
    void placeOrder_withInsufficientStock_returns400() {
        String body = """
                {
                  "productId": 1,
                  "quantity": 99999
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .auth().basic("user", "user123")
            .body(body)
        .when()
            .post("/orders")
        .then()
            .statusCode(400)
            .body("message", containsString("Not enough stock"));
    }

    @Test
    @Order(20)
    void placeOrder_withNonExistentProduct_returns404() {
        String body = """
                {
                  "productId": 9999,
                  "quantity": 1
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .auth().basic("user", "user123")
            .body(body)
        .when()
            .post("/orders")
        .then()
            .statusCode(404)
            .body("message", containsString("9999"));
    }

    // =========================================================================
    // SECTION 7 — PUT / PATCH / DELETE happy paths
    // =========================================================================

    @Test
    @Order(21)
    void updateProduct_asAdmin_returns200WithUpdatedData() {
        String updatedBody = """
                {
                  "name": "iPhone 15 Pro Max",
                  "description": "Updated Apple flagship smartphone",
                  "price": 1199.99,
                  "category": "Electronics",
                  "stock": 40
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .auth().basic("admin", "password123")
            .body(updatedBody)
        .when()
            .put("/products/1")
        .then()
            .statusCode(200)
            .body("name", equalTo("iPhone 15 Pro Max"))
            .body("price", equalTo(1199.99f));
    }

    @Test
    @Order(22)
    void updateOrderStatus_returns200WithNewStatus() {
        // First create an order so we have one to update
        String orderBody = "{ \"productId\": 2, \"quantity\": 1 }";

        int orderId = given()
                .contentType(ContentType.JSON)
                .auth().basic("user", "user123")
                .body(orderBody)
            .when()
                .post("/orders")
            .then()
                .statusCode(201)
                .extract().path("id");  // extract the created order's ID

        // Now update its status
        given()
            .contentType(ContentType.JSON)
            .auth().basic("user", "user123")
            .body("{ \"status\": \"CONFIRMED\" }")
        .when()
            .patch("/orders/" + orderId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("CONFIRMED"));
    }

    @Test
    @Order(23)
    void updateOrderStatus_withInvalidStatus_returns400() {
        // Create an order first
        int orderId = given()
                .contentType(ContentType.JSON)
                .auth().basic("user", "user123")
                .body("{ \"productId\": 2, \"quantity\": 1 }")
            .when()
                .post("/orders")
            .then()
                .statusCode(201)
                .extract().path("id");

        given()
            .contentType(ContentType.JSON)
            .auth().basic("user", "user123")
            .body("{ \"status\": \"LAUNCHED_TO_MOON\" }")
        .when()
            .patch("/orders/" + orderId + "/status")
        .then()
            .statusCode(400)
            .body("message", containsString("Invalid status"));
    }

    @Test
    @Order(24)
    void deleteProduct_asAdmin_returns204() {
        // Create a product to delete so we don't affect other tests
        String newProductBody = """
                {
                  "name": "Temp Product",
                  "description": "Will be deleted",
                  "price": 1.00,
                  "category": "Test",
                  "stock": 1
                }
                """;

        int newId = given()
                .contentType(ContentType.JSON)
                .auth().basic("admin", "password123")
                .body(newProductBody)
            .when()
                .post("/products")
            .then()
                .statusCode(201)
                .extract().path("id");

        // Delete it
        given()
            .auth().basic("admin", "password123")
        .when()
            .delete("/products/" + newId)
        .then()
            .statusCode(204);

        // Confirm it's gone
        given()
        .when()
            .get("/products/" + newId)
        .then()
            .statusCode(404);
    }

    // =========================================================================
    // SECTION 8 — Full end-to-end flow test
    //   (mirrors running all methods in ApiClientExample.java's main())
    // =========================================================================

    @Test
    @Order(25)
    void fullFlow_createProduct_placeOrder_confirmOrder() {
        // Step 1: Admin creates a new product
        int productId = given()
                .contentType(ContentType.JSON)
                .auth().basic("admin", "password123")
                .body("""
                        {
                          "name": "Nintendo Switch OLED",
                          "description": "Handheld gaming console with OLED screen",
                          "price": 349.99,
                          "category": "Electronics",
                          "stock": 10
                        }
                        """)
            .when()
                .post("/products")
            .then()
                .statusCode(201)
                .extract().path("id");

        // Step 2: User can see the new product
        given()
        .when()
            .get("/products/" + productId)
        .then()
            .statusCode(200)
            .body("name", equalTo("Nintendo Switch OLED"));

        // Step 3: User places an order for it
        int orderId = given()
                .contentType(ContentType.JSON)
                .auth().basic("user", "user123")
                .body("{ \"productId\": " + productId + ", \"quantity\": 2 }")
            .when()
                .post("/orders")
            .then()
                .statusCode(201)
                .body("status", equalTo("PENDING"))
                .body("totalPrice", equalTo(699.98f))  // 349.99 * 2
                .extract().path("id");

        // Step 4: Admin ships the order
        given()
            .contentType(ContentType.JSON)
            .auth().basic("admin", "password123")
            .body("{ \"status\": \"SHIPPED\" }")
        .when()
            .patch("/orders/" + orderId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("SHIPPED"));
    }

    // =========================================================================
    // SECTION 9 — Extracting values from responses
    //   (useful for chaining API calls — like what main() did in ApiClientExample)
    // =========================================================================

    @Test
    @Order(26)
    void extractAndUseResponseValues() {
        // REST-assured lets you pull values out of the response for use in assertions or next calls.
        // HttpClient equivalent: manually parsing the JSON string.

        Response response = given()
                .accept(ContentType.JSON)
            .when()
                .get("/products/1")
            .then()
                .statusCode(200)
                .extract().response();

        // Extract individual fields
        String name     = response.jsonPath().getString("name");
        float  price    = response.jsonPath().getFloat("price");
        String category = response.jsonPath().getString("category");
        int    stock    = response.jsonPath().getInt("stock");

        System.out.println("Extracted → name: " + name + " | price: " + price
                + " | category: " + category + " | stock: " + stock);

        // Assert on extracted values using plain Java
        Assertions.assertNotNull(name);
        Assertions.assertTrue(price > 0);
        Assertions.assertNotNull(category);
        Assertions.assertTrue(stock >= 0);
    }
}

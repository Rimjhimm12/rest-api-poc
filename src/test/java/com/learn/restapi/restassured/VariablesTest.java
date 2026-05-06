package com.learn.restapi.restassured;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.*;

import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * VARIABLES IN API TESTING — REST-assured edition.
 *
 * PREREQUISITE: Start the server → mvn spring-boot:run
 * Run all:      mvn test -Dtest=VariablesTest
 * Run one:      mvn test -Dtest=VariablesTest#type2_credentialVariables
 *
 * Covers 5 types of variables used in real API test suites:
 *
 *   TYPE 1 — Configuration variables  (base URL, port, environment)
 *   TYPE 2 — Credential variables     (usernames, passwords)
 *   TYPE 3 — Extracted variables      (IDs pulled from responses, passed to next request)
 *   TYPE 4 — Request spec variables   (reusable pre-built request templates)
 *   TYPE 5 — Response spec variables  (reusable assertion templates)
 *
 * Postman equivalent for each type is noted in comments throughout.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VariablesTest {

    // =========================================================================
    // TYPE 1 — CONFIGURATION VARIABLES
    //
    // Store environment config as named constants so you change them in ONE
    // place when switching from dev → staging → production.
    //
    // Postman equivalent: Environment Variables
    //   Name: baseUrl      Value: http://localhost:8080
    //   Name: basePath     Value: /api
    //   Used in URL bar:   {{baseUrl}}{{basePath}}/products
    // =========================================================================
    private static final String BASE_URL  = "http://localhost";
    private static final int    PORT      = 8080;
    private static final String BASE_PATH = "/api";

    // =========================================================================
    // TYPE 2 — CREDENTIAL VARIABLES
    //
    // Store usernames and passwords as constants. In real projects these come
    // from environment variables or a secrets manager — never hardcoded in a
    // shared repo. Here they are hardcoded for learning clarity.
    //
    // Postman equivalent: Environment Variables
    //   Name: adminUsername   Value: admin
    //   Name: adminPassword   Value: password123
    //   Used in Auth tab:     Username: {{adminUsername}}
    // =========================================================================
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "password123";
    private static final String USER_USER  = "user";
    private static final String USER_PASS  = "user123";

    // =========================================================================
    // TYPE 3 — EXTRACTED VARIABLES
    //
    // Values pulled OUT of a response and stored for use in a LATER request.
    // This is how you chain API calls: create something → capture its ID →
    // use that ID to GET / UPDATE / DELETE it.
    //
    // Postman equivalent (in the Tests script of a POST request):
    //   var jsonData = pm.response.json();
    //   pm.environment.set("createdProductId", jsonData.id);
    //
    // Then in the next request URL: /api/products/{{createdProductId}}
    //
    // static fields so they survive across @Test methods in this class.
    // =========================================================================
    private static int createdProductId;
    private static int createdOrderId;

    // =========================================================================
    // TYPE 4 — REQUEST SPEC VARIABLES  (RequestSpecBuilder)
    //
    // A RequestSpecification is a reusable object that holds common request
    // settings — base URL, content type, auth headers — so you don't repeat
    // them on every single test.
    //
    // Postman equivalent: Collection-level Authorization + Headers
    //   (set once on the Collection → inherited by every request in it)
    // =========================================================================
    private static RequestSpecification adminSpec;
    private static RequestSpecification userSpec;
    private static RequestSpecification publicSpec;

    // =========================================================================
    // TYPE 5 — RESPONSE SPEC VARIABLES  (ResponseSpecBuilder)
    //
    // A ResponseSpecification holds common assertions that every response
    // should satisfy — status code, content type, presence of certain fields.
    //
    // Postman equivalent: Collection-level Tests script
    //   pm.test("Content-Type is JSON", function() {
    //       pm.response.to.have.header("Content-Type", "application/json");
    //   });
    // =========================================================================
    private static ResponseSpecification okJsonSpec;
    private static ResponseSpecification createdJsonSpec;

    // -------------------------------------------------------------------------
    // @BeforeAll: wire everything up once before any test runs
    // -------------------------------------------------------------------------
    @BeforeAll
    static void setup() {
        // Apply TYPE 1 variables globally
        RestAssured.baseURI  = BASE_URL;
        RestAssured.port     = PORT;
        RestAssured.basePath = BASE_PATH;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Build TYPE 4 specs using TYPE 2 credential variables
        adminSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", basicAuthHeader(ADMIN_USER, ADMIN_PASS))
                .build();

        userSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", basicAuthHeader(USER_USER, USER_PASS))
                .build();

        publicSpec = new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .build();

        // Build TYPE 5 response specs
        okJsonSpec = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .build();

        createdJsonSpec = new ResponseSpecBuilder()
                .expectStatusCode(201)
                .expectContentType(ContentType.JSON)
                .build();
    }

    // =========================================================================
    // TEST 1 — TYPE 1: configuration variables
    //
    // Shows that RestAssured.baseURI / port / basePath (set in @BeforeAll)
    // means every request automatically uses the right server.
    // Change BASE_URL / PORT here to point at staging or production.
    // =========================================================================
    @Test
    @Order(1)
    void type1_configurationVariables_baseUrlAppliedAutomatically() {
        // We only write "/products" — REST-assured prepends BASE_URL + PORT + BASE_PATH
        // Actual request goes to: http://localhost:8080/api/products
        given()
        .when()
            .get("/products")           // not http://localhost:8080/api/products
        .then()
            .statusCode(200);

        System.out.println("Base URL in use: " + RestAssured.baseURI + ":" + RestAssured.port + RestAssured.basePath);
    }

    // =========================================================================
    // TEST 2 — TYPE 2: credential variables
    //
    // Shows the difference between hardcoded credentials (bad) and named
    // credential variables (good). Both produce the same Authorization header.
    // =========================================================================
    @Test
    @Order(2)
    void type2_credentialVariables_namedVsHardcoded() {

        // BAD — hardcoded: scattered across tests, painful to update
        given()
            .header("Authorization", "Basic YWRtaW46cGFzc3dvcmQxMjM=")
            .contentType(ContentType.JSON)
            .body("{ \"name\": \"Test\", \"description\": \"x\", \"price\": 1.0, \"category\": \"X\", \"stock\": 1 }")
        .when()
            .post("/products")
        .then()
            .statusCode(201);

        // GOOD — named variables: change ADMIN_PASS in one place, all tests update
        given()
            .header("Authorization", basicAuthHeader(ADMIN_USER, ADMIN_PASS))
            .contentType(ContentType.JSON)
            .body("{ \"name\": \"Test2\", \"description\": \"x\", \"price\": 1.0, \"category\": \"X\", \"stock\": 1 }")
        .when()
            .post("/products")
        .then()
            .statusCode(201);

        System.out.println("Both requests used the same credential — second one via named variables.");
    }

    // =========================================================================
    // TEST 3 — TYPE 3: extracted variables (part 1 — capture)
    //
    // Create a product and save the server-generated ID into a static variable.
    // Postman equivalent (Tests tab of this request):
    //   pm.environment.set("createdProductId", pm.response.json().id);
    // =========================================================================
    @Test
    @Order(3)
    void type3_extractedVariables_captureIdFromCreateResponse() {
        String body = """
                {
                  "name": "Variables Demo Laptop",
                  "description": "Created to demonstrate variable extraction",
                  "price": 1299.99,
                  "category": "Electronics",
                  "stock": 10
                }
                """;

        // .extract().path("id") pulls the "id" field from the JSON response
        // and stores it in our static variable for use in later tests.
        createdProductId = given()
                .spec(adminSpec)
                .body(body)
            .when()
                .post("/products")
            .then()
                .statusCode(201)
                .body("name", equalTo("Variables Demo Laptop"))
                .extract().path("id");

        System.out.println("Captured productId = " + createdProductId);
        assertTrue(createdProductId > 0, "Extracted ID should be a positive integer");
    }

    // =========================================================================
    // TEST 4 — TYPE 3: extracted variables (part 2 — reuse)
    //
    // Use the ID captured in test 3 to GET, then UPDATE the same product.
    // Postman equivalent: using {{createdProductId}} in the request URL.
    // =========================================================================
    @Test
    @Order(4)
    void type3_extractedVariables_reuseIdInSubsequentRequests() {
        // GET — use the extracted ID to fetch the specific product we created
        given()
            .spec(publicSpec)
        .when()
            .get("/products/" + createdProductId)   // ← extracted variable used here
        .then()
            .statusCode(200)
            .body("id",   equalTo(createdProductId))
            .body("name", equalTo("Variables Demo Laptop"));

        // PUT — update the same product using the same ID
        String updatedBody = """
                {
                  "name": "Variables Demo Laptop — Updated",
                  "description": "Updated via extracted variable",
                  "price": 1199.99,
                  "category": "Electronics",
                  "stock": 8
                }
                """;

        given()
            .spec(adminSpec)
            .body(updatedBody)
        .when()
            .put("/products/" + createdProductId)   // ← same extracted variable
        .then()
            .statusCode(200)
            .body("name",  equalTo("Variables Demo Laptop — Updated"))
            .body("price", equalTo(1199.99f));

        System.out.println("GET and PUT both used extracted productId = " + createdProductId);
    }

    // =========================================================================
    // TEST 5 — TYPE 4: request spec variables
    //
    // Use adminSpec / userSpec / publicSpec instead of repeating
    // contentType + accept + auth headers on every request.
    // =========================================================================
    @Test
    @Order(5)
    void type4_requestSpecVariables_reusableAuthAndHeaders() {
        // publicSpec → no auth, accepts JSON
        given()
            .spec(publicSpec)
        .when()
            .get("/products")
        .then()
            .statusCode(200);

        // userSpec → authenticated as USER, content-type + accept preset
        String orderBody = "{ \"productId\": " + createdProductId + ", \"quantity\": 1 }";

        createdOrderId = given()
                .spec(userSpec)
                .body(orderBody)
            .when()
                .post("/orders")
            .then()
                .statusCode(201)
                .extract().path("id");

        System.out.println("Order created using userSpec. Captured orderId = " + createdOrderId);

        // adminSpec → authenticated as ADMIN — can do anything
        given()
            .spec(adminSpec)
        .when()
            .get("/orders")
        .then()
            .statusCode(200);
    }

    // =========================================================================
    // TEST 6 — TYPE 5: response spec variables
    //
    // Reuse the same set of assertions across multiple requests.
    // okJsonSpec asserts: 200 + application/json content type.
    // createdJsonSpec asserts: 201 + application/json content type.
    // =========================================================================
    @Test
    @Order(6)
    void type5_responseSpecVariables_reusableAssertions() {
        // okJsonSpec — every GET endpoint must satisfy these
        given().spec(publicSpec).when().get("/products").then().spec(okJsonSpec);
        given().spec(publicSpec).when().get("/products/" + createdProductId).then().spec(okJsonSpec);
        given().spec(userSpec).when().get("/orders").then().spec(okJsonSpec);
        given().spec(userSpec).when().get("/orders/" + createdOrderId).then().spec(okJsonSpec);

        // createdJsonSpec — every POST that creates a resource must satisfy these
        String newProductBody = """
                {
                  "name": "Response Spec Demo",
                  "description": "Testing response spec",
                  "price": 49.99,
                  "category": "Test",
                  "stock": 5
                }
                """;

        given().spec(adminSpec).body(newProductBody).when().post("/products").then().spec(createdJsonSpec);

        System.out.println("All endpoints satisfied their response specs.");
    }

    // =========================================================================
    // TEST 7 — Full chain using ALL 5 types together
    //
    // This is how a real test scenario looks in production:
    // specs handle boilerplate, extracted variables chain requests together.
    //
    // Scenario: Admin creates a product → User orders it → Admin ships the order
    // =========================================================================
    @Test
    @Order(7)
    void allTypes_fullChainedScenario() {
        // STEP 1: Create product (uses TYPE 4 adminSpec, captures TYPE 3 variable)
        int productId = given()
                .spec(adminSpec)                        // TYPE 4 — request spec
                .body("""
                        {
                          "name": "Chained Request Demo",
                          "description": "Product for chain demo",
                          "price": 299.99,
                          "category": "Electronics",
                          "stock": 5
                        }
                        """)
            .when()
                .post("/products")
            .then()
                .spec(createdJsonSpec)                  // TYPE 5 — response spec
                .body("name", equalTo("Chained Request Demo"))
                .extract().path("id");                  // TYPE 3 — extract

        System.out.println("Step 1 — Created productId: " + productId);

        // STEP 2: Verify the product exists (reuses TYPE 3 variable)
        given()
            .spec(publicSpec)
        .when()
            .get("/products/" + productId)              // TYPE 3 — reuse
        .then()
            .spec(okJsonSpec)
            .body("stock", equalTo(5));

        // STEP 3: Place an order (TYPE 4 userSpec, captures another TYPE 3 variable)
        int orderId = given()
                .spec(userSpec)
                .body("{ \"productId\": " + productId + ", \"quantity\": 2 }")
            .when()
                .post("/orders")
            .then()
                .spec(createdJsonSpec)
                .body("totalPrice", equalTo(599.98f))   // 299.99 × 2
                .body("status",     equalTo("PENDING"))
                .extract().path("id");                  // TYPE 3 — extract

        System.out.println("Step 2 — Created orderId: " + orderId);

        // STEP 4: Ship the order (reuses TYPE 3 orderId variable)
        given()
            .spec(adminSpec)
            .body("{ \"status\": \"SHIPPED\" }")
        .when()
            .patch("/orders/" + orderId + "/status")    // TYPE 3 — reuse
        .then()
            .spec(okJsonSpec)
            .body("status", equalTo("SHIPPED"));

        System.out.println("Step 3 — Order " + orderId + " shipped.");
        System.out.println("Full chain complete: product " + productId + " → order " + orderId + " → SHIPPED");
    }

    // =========================================================================
    // TEST 8 — Extracting multiple values from one response
    //
    // Shows how to pull several fields at once from a single response,
    // rather than making multiple calls.
    // =========================================================================
    @Test
    @Order(8)
    void type3_extractMultipleValuesFromOneResponse() {
        Response response = given()
                .spec(publicSpec)
            .when()
                .get("/products/" + createdProductId)
            .then()
                .spec(okJsonSpec)
                .extract().response();

        // Extract multiple fields into separate variables
        int    id          = response.jsonPath().getInt("id");
        String name        = response.jsonPath().getString("name");
        float  price       = response.jsonPath().getFloat("price");
        String category    = response.jsonPath().getString("category");
        int    stock       = response.jsonPath().getInt("stock");

        System.out.println("Extracted multiple variables from one response:");
        System.out.printf("  id=%d | name=%s | price=%.2f | category=%s | stock=%d%n",
                id, name, price, category, stock);

        // Use them in assertions or pass them to the next request
        assertEquals(createdProductId, id);
        assertNotNull(name);
        assertTrue(price > 0);
    }

    // =========================================================================
    // Cleanup — delete the product created by type3 tests
    // =========================================================================
    @AfterAll
    static void cleanup() {
        if (createdProductId > 0) {
            given()
                .spec(adminSpec)
            .when()
                .delete("/products/" + createdProductId)
            .then()
                .statusCode(anyOf(is(204), is(404)));
        }
    }

    // -------------------------------------------------------------------------
    // Helper — builds the Authorization header value for Basic Auth.
    // Kept here so the tests above stay readable without noise.
    // -------------------------------------------------------------------------
    private static String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}

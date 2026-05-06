package com.learn.restapi.restassured;

import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TROUBLESHOOTING GUIDE — REST-assured debugging techniques.
 *
 * PREREQUISITE: Start the server first → mvn spring-boot:run
 *
 * Run this class:        mvn test -Dtest=TroubleshootingTest
 * Run one technique:     mvn test -Dtest=TroubleshootingTest#technique_01_logEverythingForOneRequest
 *
 * Read each test top-to-bottom. Each one demonstrates ONE troubleshooting
 * technique with a comment explaining when you would actually reach for it.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TroubleshootingTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/api";
    }

    // =========================================================================
    // TECHNIQUE 1 — Log the full request + response for one specific call
    //
    // When to use: a single test is failing and you want to see exactly what
    // is going out and coming back, without touching every other test.
    //
    // Add  .log().all()  after given() to print the request.
    // Add  .log().all()  after then()  to print the response.
    // =========================================================================
    @Test
    @Order(1)
    void technique_01_logEverythingForOneRequest() {
        given()
            .log().all()                    // prints: method, URI, headers, body
        .when()
            .get("/products/1")
        .then()
            .log().all()                    // prints: status, headers, body
            .statusCode(200);
    }

    // =========================================================================
    // TECHNIQUE 2 — Log only specific parts (less noise than log().all())
    //
    // When to use: you know roughly what the problem is and want focused output.
    //   .log().headers()  → only request/response headers
    //   .log().body()     → only the body
    //   .log().status()   → only the status line
    //   .log().params()   → only query/form params
    //   .log().uri()      → only the URL that was called
    // =========================================================================
    @Test
    @Order(2)
    void technique_02_logOnlyWhatYouNeed() {
        System.out.println("--- Request URI ---");
        given()
            .log().uri()
            .queryParam("category", "Electronics")
        .when()
            .get("/products")
        .then()
            .log().status()                 // just: HTTP/1.1 200 OK
            .log().headers()                // just: Content-Type, X-Total-Count, etc.
            .statusCode(200);

        System.out.println("\n--- Response body only ---");
        given()
        .when()
            .get("/products/1")
        .then()
            .log().body()                   // just the JSON body, nothing else
            .statusCode(200);
    }

    // =========================================================================
    // TECHNIQUE 3 — Log ONLY when the assertion fails (best for large test suites)
    //
    // When to use: default setup for all tests. Silent on success, noisy on
    // failure — so you don't drown in output when everything is green.
    //
    // This is already in ECommerceApiTest via:
    //   RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    //
    // Per-test equivalent shown below.
    // =========================================================================
    @Test
    @Order(3)
    void technique_03_logOnlyOnFailure() {
        given()
            .log().ifValidationFails()      // request logged only if .then() assertions fail
        .when()
            .get("/products")
        .then()
            .log().ifValidationFails()      // response logged only if assertions fail
            .statusCode(200)
            .body("$", not(empty()));
        // If this passes → no output at all (clean).
        // If this fails  → full request + response printed automatically.
    }

    // =========================================================================
    // TECHNIQUE 4 — Log when the server returns an error status (4xx / 5xx)
    //
    // When to use: you expect 200 but get 4xx/5xx and want to see the error
    // body without printing anything for successful calls.
    // =========================================================================
    @Test
    @Order(4)
    void technique_04_logOnlyOnErrorStatusCode() {
        given()
            .log().ifValidationFails(LogDetail.ALL)
        .when()
            .get("/products")
        .then()
            .log().ifError()                // prints response only if status >= 400
            .statusCode(200);
    }

    // =========================================================================
    // TECHNIQUE 5 — Extract the full Response object and inspect it manually
    //
    // When to use: you are not sure what fields the response has, or you want
    // to print parts of it with System.out.println for quick investigation —
    // same as you did with HttpClient.
    // =========================================================================
    @Test
    @Order(5)
    void technique_05_extractResponseAndInspectManually() {
        Response response = given()
                .accept(ContentType.JSON)
            .when()
                .get("/products/1")
            .then()
                .statusCode(200)
                .extract().response();      // get the whole response object

        // Now treat it like a plain object — inspect anything you want
        System.out.println("Status code  : " + response.statusCode());
        System.out.println("Content-Type : " + response.contentType());
        System.out.println("Full body    :\n" + response.body().prettyPrint());

        // Extract individual fields from the JSON
        int    id       = response.jsonPath().getInt("id");
        String name     = response.jsonPath().getString("name");
        float  price    = response.jsonPath().getFloat("price");
        String category = response.jsonPath().getString("category");

        System.out.println("\nExtracted fields:");
        System.out.println("  id       = " + id);
        System.out.println("  name     = " + name);
        System.out.println("  price    = " + price);
        System.out.println("  category = " + category);

        // Use plain JUnit assertions instead of Hamcrest if you prefer
        assertNotNull(name, "name should not be null");
        assertTrue(price > 0, "price should be positive, got: " + price);
    }

    // =========================================================================
    // TECHNIQUE 6 — Extract and inspect a list response
    //
    // When to use: the endpoint returns an array and you want to iterate
    // over items, print them, or check specific elements by index.
    // =========================================================================
    @Test
    @Order(6)
    void technique_06_extractAndInspectListResponse() {
        Response response = given()
                .queryParam("category", "Electronics")
            .when()
                .get("/products")
            .then()
                .statusCode(200)
                .extract().response();

        // Extract as a Java List — each element is a Map<String, Object>
        List<String> names  = response.jsonPath().getList("name");
        List<Float>  prices = response.jsonPath().getList("price");

        System.out.println("Electronics products returned: " + names.size());
        for (int i = 0; i < names.size(); i++) {
            System.out.printf("  [%d] %-30s $%.2f%n", i, names.get(i), prices.get(i));
        }

        // Access a specific item by index
        String firstProductName = response.jsonPath().getString("[0].name");
        System.out.println("First item: " + firstProductName);

        assertFalse(names.isEmpty(), "Expected at least one Electronics product");
    }

    // =========================================================================
    // TECHNIQUE 7 — Global filters: always log every request and response
    //
    // When to use: early in development when you want to see everything,
    // or when you are completely lost and need full visibility.
    //
    // WARNING: very verbose — disable once you find the problem.
    //
    // To apply globally (affects all tests in the run), add this to @BeforeAll:
    //   RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    //
    // This test shows the per-request equivalent.
    // =========================================================================
    @Test
    @Order(7)
    void technique_07_globalAlwaysOnLogging_perRequestDemo() {
        // Per-request version of always-on logging
        given()
            .filter(new RequestLoggingFilter())   // log this request
            .filter(new ResponseLoggingFilter())  // log this response
        .when()
            .get("/products")
        .then()
            .statusCode(200);

        // To enable globally for ALL tests in a class, put this in @BeforeAll:
        //   RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        // To turn it off after finding the bug:
        //   RestAssured.reset();   ← resets all global config including filters
    }

    // =========================================================================
    // TECHNIQUE 8 — Validate error response structure field by field
    //
    // When to use: a negative test is failing and you are not sure whether the
    // status code is wrong, the error field is wrong, or the message is wrong.
    // Split the assertion into one per line to pinpoint the exact failure.
    // =========================================================================
    @Test
    @Order(8)
    void technique_08_assertFieldByFieldToFindExactFailure() {
        Response response = given()
                .accept(ContentType.JSON)
            .when()
                .get("/products/9999")
            .then()
                .extract().response();

        System.out.println("Response body:\n" + response.body().prettyPrint());

        // Assert each field separately so the failure message tells you exactly
        // which field is wrong — instead of a single blob assertion failing.
        assertEquals(404,        response.statusCode(),              "Wrong status code");
        assertEquals("Not Found", response.jsonPath().getString("error"), "Wrong error field");
        assertTrue(response.jsonPath().getString("message").contains("9999"),
                "Message should mention the missing ID. Got: " + response.jsonPath().getString("message"));
        assertNotNull(response.jsonPath().getString("timestamp"),    "Timestamp missing");
    }

    // =========================================================================
    // TECHNIQUE 9 — Confirm what the server actually received
    //
    // When to use: you are getting an unexpected error (e.g., 400 or 415) and
    // you are not sure if the request body or headers are being sent correctly.
    // Log the request and compare it to what you expect in Postman.
    // =========================================================================
    @Test
    @Order(9)
    void technique_09_verifyExactRequestBeingSent() {
        String body = """
                {
                  "name": "Debug Product",
                  "description": "Checking what goes on the wire",
                  "price": 9.99,
                  "category": "Test",
                  "stock": 1
                }
                """;

        given()
            .log().all()                            // print EVERYTHING sent to the server
            .contentType(ContentType.JSON)          // Content-Type: application/json
            .accept(ContentType.JSON)               // Accept: application/json
            .auth().basic("admin", "password123")   // Authorization: Basic ...
            .body(body)
        .when()
            .post("/products")
        .then()
            .log().all()                            // print EVERYTHING received
            .statusCode(201);

        // Compare the logged output to your Postman request.
        // If headers or body look different, that is your bug.
    }

    // =========================================================================
    // TECHNIQUE 10 — Intentional failure to see what REST-assured prints
    //
    // When to use: run this once to understand what a failure looks like BEFORE
    // you encounter a real one. The output shows you exactly what to read.
    //
    // This test is @Disabled so it does not break your build.
    // Remove @Disabled to run it deliberately.
    // =========================================================================
    @Test
    @Order(10)
    @Disabled("Intentionally failing test — enable to see what failure output looks like")
    void technique_10_intentionalFailure_readTheOutput() {
        given()
            .log().all()
        .when()
            .get("/products/1")
        .then()
            .log().all()
            .statusCode(999)                        // deliberately wrong — will fail
            .body("name", equalTo("WRONG NAME"));   // deliberately wrong — will fail

        // After running, read the output:
        //
        //  1 expectation failed.
        //  Expected status code <999> but was <200>.
        //
        //  Request method: GET
        //  Request URI:    http://localhost:8080/api/products/1
        //  Headers:        Accept=*/*
        //
        //  HTTP/1.1 200 OK
        //  Content-Type: application/json
        //  ...
        //  {
        //    "id": 1,
        //    "name": "iPhone 15 Pro",
        //    ...
        //  }
    }

    // =========================================================================
    // TECHNIQUE 11 — Check connectivity before blaming the test
    //
    // When to use: all tests are failing with "Connection refused". This
    // verifies the server is up before you spend time debugging test code.
    // =========================================================================
    @Test
    @Order(11)
    void technique_11_verifyServerIsReachable() {
        int status = given()
                .when()
                    .get("/products")
                .then()
                    .extract().statusCode();

        // If this line fails with "Connection refused", the server is not running.
        // Start it with: mvn spring-boot:run
        assertNotEquals(0, status,
                "Got status 0 — server is not reachable. Run: mvn spring-boot:run");

        System.out.println("Server is reachable. Status: " + status);
    }

    // =========================================================================
    // TECHNIQUE 12 — Print the raw response body as a plain string
    //
    // When to use: JSON path extraction is failing and you want to see the
    // raw text exactly as it came back — no parsing, no formatting.
    // Useful to spot encoding issues or unexpected HTML error pages.
    // =========================================================================
    @Test
    @Order(12)
    void technique_12_printRawResponseBody() {
        String rawBody = given()
                .when()
                    .get("/products")
                .then()
                    .statusCode(200)
                    .extract().body().asString();

        System.out.println("Raw body (first 500 chars):");
        System.out.println(rawBody.substring(0, Math.min(500, rawBody.length())));

        // If you see an HTML page instead of JSON here, the server returned
        // an error page — check your URL and auth headers.
        assertTrue(rawBody.trim().startsWith("[") || rawBody.trim().startsWith("{"),
                "Expected JSON but got something else. Raw: " + rawBody.substring(0, 100));
    }
}

# REST API Learning Guide
### Built on an E-Commerce Spring Boot Project

> **Who this is for:** You know Java and Selenium. You understand that Selenium sends browser actions (clicks, form fills) to a browser. REST APIs work the same way — instead of browser actions, you send HTTP requests to a server. Everything you learn here maps directly to API testing in your QA career.

---

## Table of Contents

1. [What is a REST API?](#1-what-is-a-rest-api)
2. [Start the Server](#2-start-the-server)
3. [Send a GET Request in a Browser](#3-send-a-get-request-in-a-browser)
4. [Send a GET Request in Postman](#4-send-a-get-request-in-postman)
5. [Send a POST Request with a JSON Body](#5-send-a-post-request-with-a-json-body)
6. [Add Headers](#6-add-headers)
7. [Add Query Parameters](#7-add-query-parameters)
8. [Try Invalid Input and Inspect Errors](#8-try-invalid-input-and-inspect-errors)
9. [Use Authentication](#9-use-authentication)
10. [Read API Documentation (Swagger)](#10-read-api-documentation-swagger)
11. [Call an API from Java Code](#11-call-an-api-from-java-code)
12. [Run Automated Tests with REST-assured](#12-run-automated-tests-with-rest-assured)
13. [Variables in API Testing](#13-variables-in-api-testing)
14. [Data-Driven API Testing in Postman](#14-data-driven-api-testing-in-postman)
15. [File Upload Operations](#15-file-upload-operations)
16. [Build a Tiny REST API (Your Exercise)](#16-build-a-tiny-rest-api-your-exercise)

---

## 1. What is a REST API?

### The Analogy

Think of a waiter at a restaurant:
- **You (the client)** sit at the table and place an order.
- **The waiter (HTTP)** carries your request to the kitchen.
- **The kitchen (the server)** prepares your food.
- **The waiter brings back** your food (the response).

A REST API works exactly the same way. You (Postman, browser, your Java code) send an HTTP **request**. The server (our Spring Boot app) processes it and sends back an HTTP **response**.

### The Four Most Important HTTP Methods

| Method | What it does | Real-world analogy |
|--------|-------------|-------------------|
| `GET` | Fetch data — read-only | Checking your order status |
| `POST` | Create new data | Placing a new order |
| `PUT` | Replace existing data | Changing your order entirely |
| `DELETE` | Remove data | Cancelling your order |

There is also `PATCH` — update only some fields (like changing just the order status).

### HTTP Status Codes — The Language of Responses

The server always replies with a **status code** that tells you what happened.

| Range | Meaning | Common examples |
|-------|---------|----------------|
| 2xx | ✅ Success | `200 OK`, `201 Created`, `204 No Content` |
| 3xx | ↩️ Redirect | `301 Moved Permanently` |
| 4xx | ❌ Your fault | `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found` |
| 5xx | 💥 Server's fault | `500 Internal Server Error` |

> **Testing tip:** In Selenium you check that the right page loaded. In API testing you check that the right status code came back. Same idea, different layer.

### Our E-Commerce API — What We Have

```
GET    /api/products              — list all products (public)
GET    /api/products/{id}         — get one product  (public)
POST   /api/products              — create a product (ADMIN only)
PUT    /api/products/{id}         — update a product (ADMIN only)
DELETE /api/products/{id}         — delete a product (ADMIN only)

GET    /api/orders                — list orders      (any logged-in user)
GET    /api/orders/{id}           — get one order    (any logged-in user)
POST   /api/orders                — place an order   (any logged-in user)
PATCH  /api/orders/{id}/status    — update status    (any logged-in user)
```

---

## 2. Start the Server

### Prerequisites

- Java 17+ installed (`java -version`)
- Maven installed (`mvn -version`) — or use `./mvnw` if no Maven

### Start

Open a terminal in the project root and run:

```bash
mvn spring-boot:run
```

You will see output ending with something like:
```
Started RestApiPocApplication in 2.4 seconds
```

The server is now listening on **http://localhost:8080**

### To stop it
Press `Ctrl + C` in the terminal.

> **Leave the server running** for all the exercises below.

---

## 3. Send a GET Request in a Browser

The browser is the simplest REST client for GET requests (it can't do POST, PUT, DELETE — but GET works fine).

### Step 1 — Open your browser and go to:
```
http://localhost:8080/api/products
```

### What you see
The server returns a JSON array of all products. It looks like:

```json
[
  {
    "id": 1,
    "name": "iPhone 15 Pro",
    "description": "Apple smartphone with A17 chip",
    "price": 1099.99,
    "category": "Electronics",
    "stock": 50
  },
  ...
]
```

### What just happened?
1. Your browser sent `GET /api/products HTTP/1.1` to `localhost:8080`
2. Spring Boot's `ProductController.listProducts()` method ran
3. It returned a `List<Product>` which Spring converted to JSON
4. Your browser displayed it

### Try these too
- `http://localhost:8080/api/products/1` — get product with id 1
- `http://localhost:8080/api/products/99` — get a product that doesn't exist → should show a 404 error JSON

> **Observe:** In Selenium, `driver.get(url)` navigates the browser. Here, the browser itself IS the client making a GET request to the API.

---

## 4. Send a GET Request in Postman

Postman gives you full control over every part of the HTTP request — something a browser cannot do.

### First time setup
1. Download Postman from postman.com (free)
2. Create a free account (or skip)
3. Click **+ New Request** or the **+** tab

### Send a GET request

1. Set the method dropdown to **GET**
2. Enter URL: `http://localhost:8080/api/products`
3. Click **Send**

### Inspect the response

Look at the bottom panel:
- **Body** tab — the JSON response (pretty-printed)
- **Status** — `200 OK` (green)
- **Time** — how long the server took to respond
- **Size** — size of the response in bytes
- **Headers** tab — response headers from the server (notice `X-Total-Count`!)

### What Postman shows you that a browser doesn't

| Information | Browser | Postman |
|------------|---------|---------|
| Response body | ✅ | ✅ |
| Status code | ❌ (hidden) | ✅ 200 OK |
| Response headers | ❌ (need DevTools) | ✅ |
| Response time | ❌ | ✅ |
| Send POST/PUT/DELETE | ❌ | ✅ |
| Set request headers | ❌ | ✅ |
| Basic Auth | ❌ | ✅ |

> **Try:** Change the URL to `/api/products/3` and send again. Notice the status stays 200 but the body is now a single object, not an array.

> **Try:** Change the URL to `/api/products/999` and send. Status becomes **404 Not Found** and the body shows our structured error:
> ```json
> {
>   "status": 404,
>   "error": "Not Found",
>   "message": "Product not found with id: 999"
> }
> ```

---

## 5. Send a POST Request with a JSON Body

GET requests carry no body. POST requests carry a **body** (also called a payload) — this is how you send data to the server.

### In Postman

1. Open a new request tab
2. Set method to **POST**
3. URL: `http://localhost:8080/api/products`
4. Click the **Body** tab (below the URL bar)
5. Select **raw** and set the dropdown on the right to **JSON**
6. Paste this:

```json
{
  "name": "Sony PlayStation 5",
  "description": "Next-gen gaming console with DualSense controller",
  "price": 499.99,
  "category": "Electronics",
  "stock": 25
}
```

7. Click **Send**

> Wait — you should get **401 Unauthorized**! This endpoint requires you to be logged in as ADMIN. We will fix this in Concept 9. For now, note the error.

### What is JSON?

JSON (JavaScript Object Notation) is just a text format for structured data. Think of it as a Java `Map<String, Object>` written in a universal format.

```json
{
  "name": "value",          ← String
  "price": 99.99,           ← Number (no quotes)
  "inStock": true,          ← Boolean
  "tags": ["sale", "new"],  ← Array
  "dimensions": {           ← Nested object
    "width": 10,
    "height": 5
  }
}
```

### The Content-Type header

When you selected **raw → JSON** in Postman, it automatically added:
```
Content-Type: application/json
```

This tells the server: "My body is JSON, please parse it that way." Without this header, Spring Boot won't know how to read the body.

---

## 6. Add Headers

Headers are metadata attached to an HTTP request or response. Think of them as sticky notes attached to your request.

### Common request headers

| Header | Purpose | Example |
|--------|---------|---------|
| `Content-Type` | Format of the body you're sending | `application/json` |
| `Accept` | Format you want back | `application/json` |
| `Authorization` | Your credentials | `Basic YWRtaW46cGFzc3dvcmQxMjM=` |
| `X-Request-ID` | Custom ID to track a request | `abc-123-xyz` |

### Try sending a custom header in Postman

1. GET request to `http://localhost:8080/api/products`
2. Click the **Headers** tab
3. Add a row: `X-Request-ID` = `my-test-run-001`
4. Send

Now look at the **Response Headers**. You'll see our server echoed it back:
```
X-Request-ID: my-test-run-001
X-Total-Count: 7
```

### Look at the code that does this

Open [ProductController.java](src/main/java/com/learn/restapi/controller/ProductController.java):

```java
@GetMapping
public ResponseEntity<List<Product>> listProducts(
    @RequestParam(required = false) String category,
    @RequestHeader(value = "X-Request-ID", required = false) String requestId
) {
    List<Product> result = productService.findAll(...);

    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(result.size()));

    if (requestId != null) {
        headers.add("X-Request-ID", requestId);  // echo it back
    }

    return ResponseEntity.ok().headers(headers).body(result);
}
```

`@RequestHeader` reads a header from the request. `HttpHeaders` adds headers to the response. Simple as that.

### Common response headers

| Header | Meaning |
|--------|---------|
| `Content-Type` | Format of the response body |
| `X-Total-Count` | Custom: total number of records (useful for pagination) |
| `Location` | URL of newly created resource (used with 201 Created) |

---

## 7. Add Query Parameters

Query parameters are key=value pairs appended to the URL after `?`. They're used to filter, sort, or paginate results — without changing the endpoint.

### URL anatomy

```
http://localhost:8080/api/products?category=Electronics&minPrice=100&maxPrice=500
│                   │              │          │          │        │   │        │
└── Protocol+Host   └── Path       └── Param1 └── Value1 └─Param2    └─Value3  Value3
```

Everything after `?` is the **query string**.

### Exercises in Postman

**Filter by category:**
```
GET http://localhost:8080/api/products?category=Electronics
```
Expected: Only Electronics products (iPhone, TV, headphones)

**Filter by price range:**
```
GET http://localhost:8080/api/products?minPrice=50&maxPrice=100
```
Expected: Products between $50 and $100

**Combine filters:**
```
GET http://localhost:8080/api/products?category=Electronics&maxPrice=500
```
Expected: Electronics under $500

**No match:**
```
GET http://localhost:8080/api/products?category=Toys
```
Expected: `[]` — empty array with status 200 (not a 404, because the endpoint exists, just no data matched)

### Adding params in Postman the easy way

In Postman, click the **Params** tab next to the URL bar. Add key-value rows there — Postman builds the URL automatically. This is cleaner than typing `?key=value` by hand.

### In the code

```java
@GetMapping
public ResponseEntity<List<Product>> listProducts(
    @RequestParam(required = false) String category,
    @RequestParam(required = false) Double minPrice,
    @RequestParam(required = false) Double maxPrice
) {
    ...
}
```

`@RequestParam` reads from the query string. `required = false` means the parameter is optional.

---

## 8. Try Invalid Input and Inspect Errors

Good APIs don't crash when given bad input — they return clear error messages. This is the heart of negative testing, which you already know from Selenium.

### Error scenario 1 — Resource not found (404)

```
GET http://localhost:8080/api/products/9999
```

Response:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 9999",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Error scenario 2 — Malformed JSON body (400)

In Postman, set method to POST, URL to `/api/products`, body to **raw → JSON**, and paste broken JSON:

```
{ "name": "Broken, "price": 99 }
```
(Missing closing quote on "Broken")

Response:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Malformed JSON body — check your request syntax"
}
```

### Error scenario 3 — Validation failure (400)

POST to `/api/products` with Authorization: Basic admin/password123 (see Concept 9), and send:

```json
{
  "name": "",
  "price": -50,
  "category": ""
}
```

Response:
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields are invalid",
  "fieldErrors": {
    "name": "Name is required",
    "price": "Price must be a positive number",
    "description": "Description is required",
    "category": "Category is required"
  }
}
```

Notice each field tells you exactly what's wrong. This is what `@Valid` and validation annotations like `@NotBlank`, `@Positive` do in Spring.

### Error scenario 4 — Unauthorized (401)

```
POST http://localhost:8080/api/products
(no Authorization header)
```

Response:
```json
401 Unauthorized
```

### Error scenario 5 — Out of stock (400)

First, POST an order for product ID 1 (iPhone) with quantity 10000:
```json
{
  "productId": 1,
  "quantity": 10000
}
```
(With user credentials)

Response:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Not enough stock. Available: 50, Requested: 10000"
}
```

### The code behind error handling

Open [GlobalExceptionHandler.java](src/main/java/com/learn/restapi/exception/GlobalExceptionHandler.java). This single class handles all errors:

```java
@RestControllerAdvice  // intercepts exceptions from all controllers
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(...) { ... }

    // etc.
}
```

In testing, you would assert:
- Status code is the expected one
- Error message contains the right text
- `fieldErrors` map contains the right fields for validation errors

---

## 9. Use Authentication

Most real APIs don't let just anyone call every endpoint. Authentication proves **who you are**, authorization proves **what you're allowed to do**.

### HTTP Basic Authentication

The simplest form: you send `username:password` encoded as Base64 in the `Authorization` header.

```
Authorization: Basic YWRtaW46cGFzc3dvcmQxMjM=
                      ↑ base64("admin:password123")
```

### Our credentials

| Username | Password | Role | Can do |
|----------|---------|------|--------|
| `admin` | `password123` | ADMIN | Everything |
| `user` | `user123` | USER | View products, place/view orders |
| *(none)* | — | — | View products only |

### Using Basic Auth in Postman

1. Open any request that needs auth (e.g., POST /api/products)
2. Click the **Authorization** tab
3. Set **Type** to **Basic Auth**
4. Enter Username: `admin`, Password: `password123`
5. Postman automatically creates the `Authorization` header for you

### Exercise — Create a Product as ADMIN

1. POST `http://localhost:8080/api/products`
2. Auth: admin / password123
3. Body (JSON):
```json
{
  "name": "Sony PlayStation 5",
  "description": "Next-gen gaming console with DualSense controller",
  "price": 499.99,
  "category": "Electronics",
  "stock": 25
}
```
4. Send → Status: **201 Created**

### Exercise — Place an Order as USER

1. POST `http://localhost:8080/api/orders`
2. Auth: user / user123
3. Body:
```json
{
  "productId": 1,
  "quantity": 2
}
```
4. Send → Status: **201 Created** with calculated total price

### Exercise — Try ADMIN-only endpoint as USER

1. DELETE `http://localhost:8080/api/products/1`
2. Auth: user / user123
3. Send → Status: **403 Forbidden** — user is authenticated but not authorized

### Exercise — Access without auth

1. DELETE `http://localhost:8080/api/products/1`
2. No auth
3. Send → Status: **401 Unauthorized** — not even logged in

### 401 vs 403 — Know the difference

| Code | Meaning | Analogy |
|------|---------|---------|
| 401 Unauthorized | You haven't logged in at all | No ID badge |
| 403 Forbidden | You're logged in but not allowed | Badge doesn't open this door |

### How it works in the code

See [SecurityConfig.java](src/main/java/com/learn/restapi/security/SecurityConfig.java):

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
    .requestMatchers("/api/orders/**").authenticated()
)
.httpBasic(Customizer.withDefaults());
```

Each line is a rule. Spring Security checks them top to bottom and applies the first match.

---

## 10. Read API Documentation (Swagger)

Real APIs come with documentation so consumers know what endpoints exist, what parameters they take, and what responses to expect. Swagger UI auto-generates this from the code.

### Open Swagger UI

With the server running, go to:
```
http://localhost:8080/swagger-ui.html
```

### What you'll see

- All endpoints grouped by controller tag (Products, Orders)
- For each endpoint: method, URL, description, parameters, request body, response codes
- An **Execute** button — you can try the API directly from the docs page

### Exercise — Use Swagger to call an endpoint

1. Click on **GET /api/products** to expand it
2. Click **Try it out**
3. Fill in `category: Electronics`
4. Click **Execute**
5. Scroll down to see the curl command, the request URL, and the response

### Exercise — Authorize in Swagger

1. Click the **Authorize** button (padlock icon, top right)
2. Enter `admin` / `password123`
3. Click **Authorize**
4. Now you can call protected endpoints directly from Swagger

### The raw API spec (OpenAPI/JSON)

Swagger UI reads from:
```
http://localhost:8080/v3/api-docs
```

This is the machine-readable version of the docs — the same format tools like Postman and REST-assured can import to auto-generate request templates.

### Generating API docs from code

In our code, annotations like these generate the docs automatically:

```java
@Tag(name = "Products", description = "Browse and manage e-commerce products")

@Operation(summary = "List all products",
           description = "Returns all products. Optionally filter by category.")

@Parameter(description = "Filter by category (e.g. Electronics, Footwear)")
```

The library `springdoc-openapi` scans all controllers and builds the spec automatically.

---

## 11. Call an API from Java Code

In a real project (or in REST-assured tests), you call APIs from code. Here's how to do it with Java's built-in `HttpClient` (Java 11+) — no libraries needed.

Open [ApiClientExample.java](src/main/java/com/learn/restapi/examples/ApiClientExample.java).

### Run it

Make sure the server is running, then right-click `ApiClientExample.java` in IntelliJ and click **Run**. You'll see all five examples output their results.

### Breaking it down

**GET request:**
```java
HttpClient client = HttpClient.newHttpClient();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/api/products"))
    .header("Accept", "application/json")
    .GET()
    .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

System.out.println("Status: " + response.statusCode());  // 200
System.out.println("Body: " + response.body());           // JSON string
```

**POST with auth:**
```java
String json = """
    { "name": "PS5", "price": 499.99, ... }
    """;

// Basic auth = "Basic " + base64(username:password)
String credentials = Base64.getEncoder().encodeToString("admin:password123".getBytes());

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/api/products"))
    .header("Content-Type", "application/json")
    .header("Authorization", "Basic " + credentials)
    .POST(HttpRequest.BodyPublishers.ofString(json))
    .build();
```

**Read a response header:**
```java
response.headers().firstValue("X-Total-Count").orElse("not present");
```

### In real QA projects you'd use REST-assured

REST-assured is the Selenium of API testing. It wraps HTTP calls in a fluent DSL and adds built-in assertions so you don't have to parse the response manually.

This project already has a full REST-assured test suite — see **Section 12** for how to run it and understand what each test does.

---

## 12. Run Automated Tests with REST-assured

### What is REST-assured?

REST-assured is the standard Java library for API test automation — the equivalent of Selenium for REST APIs.

| Selenium (you know this) | REST-assured (same idea) |
|--------------------------|--------------------------|
| Opens a browser | Opens an HTTP connection |
| Navigates to a URL | Sends a request to an endpoint |
| Finds elements, clicks buttons | Sends headers, body, auth |
| `assertEquals(expected, actual)` | `.then().statusCode(200).body(...)` |
| WebDriver → Browser | REST-assured → HTTP Server |

Instead of verbose `HttpClient` code with manual `System.out.println`, REST-assured gives you a clean `given → when → then` pattern that reads like English.

**HttpClient (section 11):**
```java
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/api/products"))
    .header("Authorization", "Basic " + base64("admin:password123"))
    .header("Content-Type", "application/json")
    .POST(HttpRequest.BodyPublishers.ofString(json))
    .build();
HttpResponse<String> response = client.send(request, ...);
assert response.statusCode() == 201;
```

**REST-assured (same request, same assertion):**
```java
given()
    .auth().basic("admin", "password123")
    .contentType(ContentType.JSON)
    .body(json)
.when()
    .post("/api/products")
.then()
    .statusCode(201);
```

---

### The Test File

Open [ECommerceApiTest.java](src/test/java/com/learn/restapi/restassured/ECommerceApiTest.java).

It has **26 tests** grouped into 9 sections. Every test maps to a concept from this guide:

| Test section | Guide concept | What it tests |
|-------------|--------------|--------------|
| Section 1 — GET happy path | Concepts 1 & 2 | Status 200, non-empty list, single product |
| Section 2 — Query params | Concept 7 | Filter by category, price range, no match |
| Section 3 — Headers | Concept 4 | `X-Total-Count` in response, `X-Request-ID` echo |
| Section 4 — POST happy path | Concept 3 | Create product (201), place order (201) |
| Section 5 — Authentication | Concept 9 | 401 (no auth), 403 (wrong role), for multiple endpoints |
| Section 6 — Error scenarios | Concept 8 | 404, 415 (missing Content-Type), 400 validation, 400 bad JSON, 400 out of stock |
| Section 7 — PUT/PATCH/DELETE | All methods | Update, status change, delete + verify gone |
| Section 8 — Full flow | End-to-end | Create → view → order → ship in one test |
| Section 9 — Value extraction | Chaining calls | Pull an ID from response 1, use it in request 2 |

---

### Prerequisites

The server must be running before you run the tests (REST-assured connects to a real running server, just like Postman does).

**Terminal 1 — start the server:**
```bash
mvn spring-boot:run
```
Wait until you see `Started RestApiPocApplication`.

**Terminal 2 — run the tests** (keep terminal 1 open):

---

### How to Run — All Tests

```bash
mvn test -Dtest=ECommerceApiTest
```

**Expected output (all passing):**
```
[INFO] Running com.learn.restapi.restassured.ECommerceApiTest
[INFO] Tests run: 26, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.4 s
[INFO] BUILD SUCCESS
```

---

### How to Run — A Single Test

Use the method name after a `#`:

```bash
mvn test -Dtest=ECommerceApiTest#getAllProducts_returns200AndNonEmptyList
```

Useful when you are debugging one failing test and don't want to wait for all 26.

---

### How to Run from IntelliJ

**Run all tests in the class:**
1. Open [ECommerceApiTest.java](src/test/java/com/learn/restapi/restassured/ECommerceApiTest.java)
2. Click the green ▶ icon next to the class name
3. Select **Run 'ECommerceApiTest'**

**Run a single test:**
1. Click the green ▶ icon next to any individual `@Test` method
2. Select **Run 'methodName'**

**Re-run failed tests only:**
After a run, click **Rerun Failed Tests** in the Run panel (the icon that looks like ↺ with a red dot).

> Make sure the server is already running in a separate terminal or via IntelliJ's Run config before you run the tests.

---

### How to Read the Output

**All tests pass:**
```
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**A test fails:**
```
Tests run: 26, Failures: 1, Errors: 0, Skipped: 0 <<< FAILURE!
ECommerceApiTest.getProductById_returns200WithCorrectProduct -- FAILURE!

1 expectation failed.
Expected status code <200> but was <404>.

Request method:  GET
Request URI:     http://localhost:8080/api/products/1
...
Response status: 404
Response body:
{
  "status" : 404,
  "error" : "Not Found",
  ...
}
```

REST-assured automatically prints the full request and response when a test fails — you don't have to add any `System.out.println`. This is controlled by:

```java
@BeforeAll
static void setup() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
}
```

To always print requests/responses (useful while learning), replace that line with:

```java
RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
```

You'll need to add these imports:
```java
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
```

---

### Understanding the given / when / then Pattern

Every test in the file follows this exact structure:

```
given()         ← BUILD the request (headers, auth, body, params)
    ...
.when()         ← SEND the request (which endpoint, which method)
    ...
.then()         ← ASSERT the response (status, headers, body fields)
    ...
```

This maps directly to your Selenium mental model:

```
// Selenium
driver.get("url");               // WHEN — navigate
WebElement el = driver.find(by); // WHEN — interact
assertEquals("expected", el.getText()); // THEN — assert

// REST-assured
given().auth()...                // GIVEN — prepare
.when().post("/endpoint")        // WHEN — send
.then().statusCode(201)          // THEN — assert
```

---

### Walk Through One Test

Open the test `fullFlow_createProduct_placeOrder_confirmOrder` (Section 8 in the test file). It's the most complete example and shows everything in one place:

```java
// STEP 1: Admin creates a product — extract the generated ID from the response
int productId = given()
        .contentType(ContentType.JSON)          // header: Content-Type: application/json
        .auth().basic("admin", "password123")   // header: Authorization: Basic ...
        .body("""
                {
                  "name": "Nintendo Switch OLED",
                  "price": 349.99,
                  ...
                }
                """)
    .when()
        .post("/products")                      // POST /api/products
    .then()
        .statusCode(201)                        // assert 201 Created
        .extract().path("id");                  // pull the "id" field out of the response JSON

// STEP 2: Anyone can see the product (public endpoint, no auth)
given()
.when()
    .get("/products/" + productId)
.then()
    .statusCode(200)
    .body("name", equalTo("Nintendo Switch OLED"));

// STEP 3: User places an order
int orderId = given()
        .contentType(ContentType.JSON)
        .auth().basic("user", "user123")
        .body("{ \"productId\": " + productId + ", \"quantity\": 2 }")
    .when()
        .post("/orders")
    .then()
        .statusCode(201)
        .body("totalPrice", equalTo(699.98f))   // 349.99 × 2 — price was computed server-side
        .extract().path("id");

// STEP 4: Admin ships the order
given()
    .contentType(ContentType.JSON)
    .auth().basic("admin", "password123")
    .body("{ \"status\": \"SHIPPED\" }")
.when()
    .patch("/orders/" + orderId + "/status")
.then()
    .statusCode(200)
    .body("status", equalTo("SHIPPED"));
```

---

### Extracting Values from a Response

The key technique for chaining calls is `.extract()`. Without it you can only assert. With it you can pull a value out and use it in the next request.

```java
// Extract a single field
int id = given()...when()...post("/products").then()
    .statusCode(201)
    .extract().path("id");          // int

String name = ...extract().path("name");  // String

// Extract the whole response object to inspect anything
Response response = given()...when()...get("/products/1")
    .then().statusCode(200)
    .extract().response();

String category = response.jsonPath().getString("category");
float price     = response.jsonPath().getFloat("price");
```

---

### Common REST-assured Matchers

These come from the `org.hamcrest.Matchers` static import:

```java
.statusCode(200)                         // exact status code
.body("name", equalTo("iPhone"))         // exact string match
.body("price", equalTo(99.99f))          // exact float — note the 'f'
.body("price", greaterThan(0f))          // numeric comparison
.body("$", not(empty()))                 // array is not empty
.body("size()", equalTo(3))              // array has exactly 3 items
.body("category", everyItem(equalTo("Electronics")))  // every item matches
.body("message", containsString("404")) // partial string match
.body("fieldErrors.name", notNullValue()) // field exists
.header("X-Total-Count", notNullValue()) // response header exists
.header("Content-Type", containsString("application/json"))
```

---

### Adding Your Own Test

Once you complete the exercise in Section 16 (Coupon feature), add a test for it:

```java
@Test
@Order(27)
void createCoupon_asAdmin_returns201() {
    given()
        .contentType(ContentType.JSON)
        .auth().basic("admin", "password123")
        .body("""
                {
                  "code": "SAVE20",
                  "discountPercent": 20,
                  "active": true
                }
                """)
    .when()
        .post("/coupons")
    .then()
        .statusCode(201)
        .body("code", equalTo("SAVE20"))
        .body("discountPercent", equalTo(20))
        .body("active", equalTo(true));
}
```

---

## 13. Variables in API Testing

Variables let you write tests and requests that work across different environments, avoid copy-pasting the same values everywhere, and chain API calls together (use the output of one request as the input of the next).

Think of them the same way you think of variables in Java — instead of hardcoding `"http://localhost:8080"` in 50 test methods, you store it once and reference it by name.

---

### The 5 Types of Variables

| Type | What it stores | Changes per... | Example |
|------|---------------|----------------|---------|
| **Configuration** | Base URL, port, environment name | Environment (dev/staging/prod) | `http://localhost:8080` |
| **Credential** | Usernames, passwords, tokens | Environment or user role | `admin` / `password123` |
| **Extracted** | Values pulled from a response | Each test run | The `id` from a `POST` response |
| **Request Spec** | Pre-built request template (headers, auth, content-type) | Role | Admin spec, User spec |
| **Response Spec** | Pre-built assertion template | Status / content contract | "must be 200 + JSON" |

---

### Variables in Postman

#### Step 1 — Create an Environment

An **Environment** in Postman is a named set of variables that you can switch between (Dev, Staging, Prod).

1. Click the **Environments** icon (the eye icon, top-right of Postman)
2. Click **+ Create Environment**
3. Name it `Local Dev`
4. Add these variables:

| Variable | Initial Value | Current Value |
|----------|--------------|---------------|
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` |
| `adminUsername` | `admin` | `admin` |
| `adminPassword` | `password123` | `password123` |
| `userUsername` | `user` | `user` |
| `userPassword` | `user123` | `user123` |

5. Click **Save**
6. Select `Local Dev` from the environment dropdown (top-right corner)

#### Step 2 — Use Variables in Requests

In the URL bar, use `{{variableName}}` anywhere:

```
{{baseUrl}}/api/products
{{baseUrl}}/api/products/{{createdProductId}}
```

In the **Auth** tab → Basic Auth:
```
Username: {{adminUsername}}
Password: {{adminPassword}}
```

In the **Headers** tab:
```
X-Environment: {{POSTMAN_ENVIRONMENT_NAME}}
```

Postman replaces `{{variableName}}` with the current value before sending. The URL bar shows the resolved value in orange when you hover over it.

#### Step 3 — Extract a Value from a Response into a Variable

This is how you chain requests: take the `id` from a `POST /products` response and use it in the next `GET /products/{{createdProductId}}` request.

In the **Scripts → Post-response** tab of your `POST /products` request, add:

```javascript
// Parse the response JSON
var body = pm.response.json();

// Store the returned id into an environment variable
pm.environment.set("createdProductId", body.id);

console.log("Captured product ID: " + body.id);
```

Now in your next request URL:
```
{{baseUrl}}/api/products/{{createdProductId}}
```

Postman fills in the value automatically using what the previous request set.

#### Step 4 — Variable Scope (which value wins?)

Postman has four scopes. When the same variable name exists in multiple scopes, the **narrowest** scope wins:

```
Global  ←  widest, shared across all workspaces
  └── Collection  ←  shared across all requests in one collection
        └── Environment  ←  switched per environment (Dev/Staging/Prod)
              └── Local  ←  narrowest, set during a single request's script
```

**In practice:**
- Use **Environment** variables for anything that changes between Dev/Staging/Prod (base URL, credentials)
- Use **Collection** variables for values shared across a collection but not environment-specific
- Use **Local** (script) variables for values extracted from responses and passed between requests

#### Step 5 — Exercise: Chain Three Postman Requests

1. **Request 1** — `POST {{baseUrl}}/api/products` with admin auth and a JSON body
   - In **Scripts → Post-response**: `pm.environment.set("productId", pm.response.json().id)`

2. **Request 2** — `POST {{baseUrl}}/api/orders` with user auth and body:
   ```json
   { "productId": {{productId}}, "quantity": 1 }
   ```
   - In **Scripts → Post-response**: `pm.environment.set("orderId", pm.response.json().id)`

3. **Request 3** — `PATCH {{baseUrl}}/api/orders/{{orderId}}/status` with body:
   ```json
   { "status": "CONFIRMED" }
   ```

Run them in order. Each request feeds the next. This is a **Postman Collection Runner** workflow.

---

### Variables in REST-assured

The test file [VariablesTest.java](src/test/java/com/learn/restapi/restassured/VariablesTest.java) demonstrates all five types with runnable examples.

#### TYPE 1 — Configuration Variables

```java
// Defined once at the top of the class
private static final String BASE_URL  = "http://localhost";
private static final int    PORT      = 8080;
private static final String BASE_PATH = "/api";

// Applied globally in @BeforeAll — every request inherits these
RestAssured.baseURI  = BASE_URL;
RestAssured.port     = PORT;
RestAssured.basePath = BASE_PATH;
```

To point at staging: change `BASE_URL` and `PORT` in one place.

#### TYPE 2 — Credential Variables

```java
private static final String ADMIN_USER = "admin";
private static final String ADMIN_PASS = "password123";

// Used wherever admin auth is needed
.header("Authorization", basicAuthHeader(ADMIN_USER, ADMIN_PASS))
```

In real projects, read these from system properties or environment variables:
```java
private static final String ADMIN_PASS = System.getenv("ADMIN_PASSWORD");
```

#### TYPE 3 — Extracted Variables (the most important one)

This is the REST-assured equivalent of `pm.environment.set(...)`.

```java
// Declare a static field to hold the value across test methods
private static int createdProductId;

// In one @Test — capture the ID from the POST response
createdProductId = given()
        .spec(adminSpec)
        .body(productJson)
    .when()
        .post("/products")
    .then()
        .statusCode(201)
        .extract().path("id");   // ← pulls "id" field from the JSON response

// In the NEXT @Test — use the captured ID
given()
    .when()
        .get("/products/" + createdProductId)   // ← same variable
    .then()
        .statusCode(200);
```

| Postman | REST-assured |
|---------|-------------|
| `pm.environment.set("productId", body.id)` | `createdProductId = ...extract().path("id")` |
| `{{productId}}` in URL | `"/products/" + createdProductId` |

#### TYPE 4 — Request Spec Variables (RequestSpecBuilder)

A `RequestSpecification` pre-packages headers, content-type, and auth so you don't repeat them on every test.

```java
// Built once in @BeforeAll
private static RequestSpecification adminSpec;

adminSpec = new RequestSpecBuilder()
        .setContentType(ContentType.JSON)
        .setAccept(ContentType.JSON)
        .addHeader("Authorization", basicAuthHeader(ADMIN_USER, ADMIN_PASS))
        .build();

// Used in every test that needs admin auth — three lines become one
given()
    .spec(adminSpec)   // ← replaces: contentType + accept + auth header
    .body(...)
.when()
    .post("/products")
```

| Without spec | With spec |
|-------------|-----------|
| `.contentType(JSON)` | `.spec(adminSpec)` |
| `.accept(JSON)` | |
| `.auth().basic("admin","password123")` | |

#### TYPE 5 — Response Spec Variables (ResponseSpecBuilder)

A `ResponseSpecification` pre-packages common assertions.

```java
// Built once
private static ResponseSpecification okJsonSpec;

okJsonSpec = new ResponseSpecBuilder()
        .expectStatusCode(200)
        .expectContentType(ContentType.JSON)
        .build();

// Applied to every endpoint that must return 200 + JSON
given().spec(publicSpec).when().get("/products").then().spec(okJsonSpec);
given().spec(userSpec).when().get("/orders").then().spec(okJsonSpec);
```

---

### Run the Variable Tests

```bash
# All 8 tests
mvn test -Dtest=VariablesTest

# Just the chaining test
mvn test -Dtest=VariablesTest#allTypes_fullChainedScenario

# Just the extraction test
mvn test -Dtest=VariablesTest#type3_extractedVariables_captureIdFromCreateResponse
```

---

### Quick Comparison — Postman vs REST-assured

| Concept | Postman | REST-assured |
|---------|---------|-------------|
| Base URL | Environment variable `{{baseUrl}}` | `RestAssured.baseURI = BASE_URL` |
| Credentials | Environment variables `{{adminUsername}}` | `private static final String ADMIN_USER` |
| Extract from response | `pm.environment.set("id", body.id)` | `id = ...extract().path("id")` |
| Use in next request | `{{id}}` in URL | `"/products/" + id` |
| Reusable request setup | Collection-level Auth + Headers | `RequestSpecBuilder` → `RequestSpecification` |
| Reusable assertions | Collection-level Tests script | `ResponseSpecBuilder` → `ResponseSpecification` |

---

## 14. Data-Driven API Testing in Postman

### What is Data-Driven Testing?

You already know this concept from Selenium — instead of writing one test per input, you write the test **once** and feed it a table of inputs. Each row runs the same request with different values.

```
Without data-driven:          With data-driven:
  Test 1: create iPhone         1 request  ←──┐
  Test 2: create Laptop         1 CSV file     │  5 rows → 5 runs
  Test 3: create Chair          (5 rows)   ────┘
  Test 4: create Bottle
  Test 5: create Book
```

In Postman, this works by:
1. Writing a request that uses `{{variableName}}` placeholders
2. Preparing a **CSV or JSON data file** — one row per test run
3. Running it through the **Collection Runner** which feeds each row in turn
4. Writing assertions in the **Scripts** tab that use `pm.iterationData.get("columnName")` to read the current row's values

---

### Core Concepts Before You Start

**`{{variableName}}` in a request** — Postman replaces it with the current iteration's value from the data file before sending.

**`pm.iterationData.get("columnName")`** — reads the value from the current row of your data file inside a Script.

**Collection Runner** — the Postman tool that reads a data file and runs a request once per row.

**Iteration** — one run of the request with one row of data. 5 rows = 5 iterations.

---

### One-Time Setup — Create a Collection

All data-driven tests must live inside a **Collection** (a folder of requests). Do this once:

1. In Postman, click **Collections** in the left sidebar
2. Click **+** → **Blank collection**
3. Name it `E-Commerce API — Data-Driven Tests`
4. Click **Create**

Also set up an Environment so the base URL is a variable (from Section 13):
- Environment name: `Local Dev`
- Variable: `baseUrl` = `http://localhost:8080`

Select `Local Dev` from the environment dropdown (top-right).

---

### Exercise 1 — Create Multiple Products from a CSV (Happy Path)

**Goal:** Run one POST request 5 times — each time creating a different product from the CSV.

**Data file:** [`test-data/01-create-products.csv`](test-data/01-create-products.csv)

```
name,description,price,category,stock
Gaming Chair,Ergonomic gaming chair...,249.99,Furniture,15
Mechanical Keyboard,RGB backlit keyboard,129.99,Electronics,40
Yoga Mat,Non-slip eco-friendly mat,34.99,Sports,200
Stainless Steel Bottle,32oz insulated bottle,24.99,Kitchen,150
Java Programming Book,Complete Java guide,49.99,Books,80
```

#### Step 1 — Add a request to the collection

1. Click the **...** next to your collection → **Add request**
2. Name it `Create Product`
3. Set method to **POST**
4. URL: `{{baseUrl}}/api/products`

#### Step 2 — Set the Auth

1. Click the **Authorization** tab
2. Type: **Basic Auth**
3. Username: `admin` &nbsp;&nbsp; Password: `password123`

#### Step 3 — Set the Body

1. Click the **Body** tab
2. Select **raw** → **JSON**
3. Paste this — notice `{{variableName}}` placeholders for every column:

```json
{
  "name":        "{{name}}",
  "description": "{{description}}",
  "price":        {{price}},
  "category":    "{{category}}",
  "stock":        {{stock}}
}
```

> ⚠️ **Important gotcha:** `price` and `stock` have **no quotes** because they are numbers. If you write `"{{price}}"` with quotes, the server receives a string and returns a 400 validation error.

#### Step 4 — Write the assertions (Scripts tab)

1. Click the **Scripts** tab → **Post-response** (older Postman: the **Tests** tab)
2. Paste this:

```javascript
// Read the expected values from the current CSV row
var expectedName     = pm.iterationData.get("name");
var expectedCategory = pm.iterationData.get("category");
var expectedPrice    = parseFloat(pm.iterationData.get("price"));
var expectedStock    = parseInt(pm.iterationData.get("stock"));

// Assert status code
pm.test("Status is 201 Created", function () {
    pm.response.to.have.status(201);
});

// Assert the response body matches what we sent
pm.test("Returned name matches input", function () {
    pm.expect(pm.response.json().name).to.equal(expectedName);
});

pm.test("Returned category matches input", function () {
    pm.expect(pm.response.json().category).to.equal(expectedCategory);
});

pm.test("Returned price matches input", function () {
    pm.expect(pm.response.json().price).to.equal(expectedPrice);
});

pm.test("Returned stock matches input", function () {
    pm.expect(pm.response.json().stock).to.equal(expectedStock);
});

pm.test("Response contains an auto-generated id", function () {
    pm.expect(pm.response.json().id).to.be.a("number");
});
```

#### Step 5 — Run with the Collection Runner

1. Click the **...** next to your collection → **Run collection**
2. In the runner panel, select only the `Create Product` request (deselect others)
3. Under **Data**, click **Select File** → choose `test-data/01-create-products.csv`
4. Postman shows **Preview** — you should see 5 rows
5. **Iterations** auto-sets to 5 (one per row)
6. Click **Run E-Commerce API — Data-Driven Tests**

#### What to observe

- **5 iterations** run one after the other
- Each row's values are shown next to the iteration number
- Green ticks = all assertions passed for that row
- Red X = assertion failed — click the row to see which test failed and what values were used
- A summary at the top shows **Total passed / Total failed**

---

### Exercise 2 — Test Invalid Inputs (Negative Testing)

**Goal:** Confirm the API returns 400 and flags the correct broken field for 5 different invalid payloads.

**Data file:** [`test-data/02-invalid-products.csv`](test-data/02-invalid-products.csv)

```
name,description,price,category,stock,scenario,expectedField
,,99.99,Electronics,10,Missing name and description,name
Valid Name,,99.99,Electronics,10,Missing description,description
Valid Name,Valid desc,-10,Electronics,10,Negative price,price
Valid Name,Valid desc,99.99,,10,Missing category,category
Valid Name,Valid desc,99.99,Electronics,-5,Negative stock,stock
```

#### Step 1 — Add a new request to the collection

1. Add request → name it `Create Product — Invalid`
2. Method: **POST**, URL: `{{baseUrl}}/api/products`
3. Auth: **Basic Auth** → `admin` / `password123`

#### Step 2 — Body (same placeholders as Exercise 1)

```json
{
  "name":        "{{name}}",
  "description": "{{description}}",
  "price":        {{price}},
  "category":    "{{category}}",
  "stock":        {{stock}}
}
```

#### Step 3 — Scripts → Post-response

```javascript
var scenario      = pm.iterationData.get("scenario");
var expectedField = pm.iterationData.get("expectedField");

// Every row must return 400
pm.test("[" + scenario + "] Status is 400 Bad Request", function () {
    pm.response.to.have.status(400);
});

// The fieldErrors object must contain the expected broken field
pm.test("[" + scenario + "] Field '" + expectedField + "' is reported in errors", function () {
    var body = pm.response.json();
    pm.expect(body.fieldErrors).to.be.an("object");
    pm.expect(body.fieldErrors).to.have.property(expectedField);
});

// The error type must be correct
pm.test("[" + scenario + "] Error type is Validation Failed", function () {
    pm.expect(pm.response.json().error).to.equal("Validation Failed");
});
```

#### Step 4 — Run

1. Collection Runner → select only `Create Product — Invalid`
2. Data file: `test-data/02-invalid-products.csv`
3. Run

#### What to observe

- The **scenario** column value appears in each test name — this makes failures easy to read:
  ```
  ✅ [Missing description] Status is 400 Bad Request
  ✅ [Missing description] Field 'description' is reported in errors
  ✅ [Missing description] Error type is Validation Failed
  ```
- If any row passes validation unexpectedly (returns 201), you know the API is missing a validation rule

---

### Exercise 3 — Filter Products with Multiple Query Params

**Goal:** Run 7 different filter combinations and verify the results match the filter rules.

**Data file:** [`test-data/03-filter-products.csv`](test-data/03-filter-products.csv)

```
category,minPrice,maxPrice,filterDescription
Electronics,,,Filter by Electronics category only
Books,,,Filter by Books category only
Footwear,,,Filter by Footwear category only
,50,200,Price between 50 and 200 (any category)
,10,50,Price between 10 and 50 (any category)
Electronics,500,2000,Electronics between 500 and 2000
Toys,,,Category with no products - expect empty list
```

#### Step 1 — Add the request

1. Add request → name it `Filter Products`
2. Method: **GET**
3. URL: `{{baseUrl}}/api/products`

#### Step 2 — Add Query Parameters

1. Click the **Params** tab
2. Add these three rows (all optional — leave Value blank for now):

| Key | Value |
|-----|-------|
| `category` | `{{category}}` |
| `minPrice` | `{{minPrice}}` |
| `maxPrice` | `{{maxPrice}}` |

> **Postman behaviour:** If a variable resolves to an empty string (because the CSV cell is blank), Postman still sends `?category=` in the URL. The server ignores blank params because our controller uses `@RequestParam(required = false)`.

#### Step 3 — Scripts → Post-response

```javascript
var filterDesc = pm.iterationData.get("filterDescription");
var category   = pm.iterationData.get("category");
var minPrice   = pm.iterationData.get("minPrice");
var maxPrice   = pm.iterationData.get("maxPrice");

var products = pm.response.json();

// Always passes — even empty list is a valid 200
pm.test("[" + filterDesc + "] Status is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("[" + filterDesc + "] Response is an array", function () {
    pm.expect(products).to.be.an("array");
});

// If a category filter was applied, every result must match it
if (category && category.length > 0) {
    pm.test("[" + filterDesc + "] All results belong to category: " + category, function () {
        products.forEach(function(p) {
            pm.expect(p.category.toLowerCase()).to.equal(category.toLowerCase());
        });
    });
}

// If minPrice was given, every result price must be >= minPrice
if (minPrice && minPrice.length > 0) {
    pm.test("[" + filterDesc + "] All prices >= " + minPrice, function () {
        products.forEach(function(p) {
            pm.expect(p.price).to.be.at.least(parseFloat(minPrice));
        });
    });
}

// If maxPrice was given, every result price must be <= maxPrice
if (maxPrice && maxPrice.length > 0) {
    pm.test("[" + filterDesc + "] All prices <= " + maxPrice, function () {
        products.forEach(function(p) {
            pm.expect(p.price).to.be.at.most(parseFloat(maxPrice));
        });
    });
}

// Log for visibility
console.log("[" + filterDesc + "] Returned " + products.length + " products.");
```

#### Step 4 — Run

Collection Runner → select `Filter Products` → data file `03-filter-products.csv` → Run.

#### What to observe

- The last row (`Toys`) returns an empty list `[]` — the test still passes because `200 + []` is correct behaviour
- For rows where `category` is blank, the category assertion is skipped (the `if` block doesn't run)
- `console.log` output is visible in the **Postman Console** (View → Postman Console, or `Ctrl+Alt+C`)

---

### Exercise 4 — Test Authentication with Multiple Credential Sets

**Goal:** Run the same `GET /orders` endpoint with 5 different username/password combinations and verify the correct status code each time.

**Data file:** [`test-data/04-auth-scenarios.csv`](test-data/04-auth-scenarios.csv)

```
username,password,expectedStatus,scenario
admin,password123,200,Valid admin credentials
user,user123,200,Valid user credentials
admin,wrongpassword,401,Correct username wrong password
wronguser,password123,401,Wrong username correct password
hacker,hacker123,401,Both credentials wrong
```

#### Step 1 — Add the request

1. Add request → name it `Auth Scenarios`
2. Method: **GET**
3. URL: `{{baseUrl}}/api/orders`

#### Step 2 — Set Auth using variables

1. Click **Authorization** tab
2. Type: **Basic Auth**
3. Username: `{{username}}`
4. Password: `{{password}}`

Postman fills these from the CSV on each iteration. No hardcoded credentials.

#### Step 3 — Scripts → Post-response

```javascript
var scenario       = pm.iterationData.get("scenario");
var expectedStatus = parseInt(pm.iterationData.get("expectedStatus"));

pm.test("[" + scenario + "] Status is " + expectedStatus, function () {
    pm.response.to.have.status(expectedStatus);
});

// For successful calls, assert the response is an array
if (expectedStatus === 200) {
    pm.test("[" + scenario + "] Response is a valid orders array", function () {
        pm.expect(pm.response.json()).to.be.an("array");
    });
}

// For failed auth, assert no sensitive data leaks
if (expectedStatus === 401) {
    pm.test("[" + scenario + "] Error body does not leak user data", function () {
        var body = pm.response.text();
        pm.expect(body).to.not.include("password");
        pm.expect(body).to.not.include("admin");
    });
}
```

#### Step 4 — Run

Collection Runner → `Auth Scenarios` → `04-auth-scenarios.csv` → Run.

#### What to observe

- Rows 1 and 2 return 200 (green)
- Rows 3, 4, 5 return 401 (green — the test expects 401)
- If a 401 row accidentally returns 200, it means authentication is broken — a real security bug

---

### Reading the Collection Runner Results

After a run you see two views:

**Run summary view:**

```
 Iterations: 5
 Assertions: 30
 Failed:      0
 Skipped:     0

 Iteration 1  ✅  6 / 6 passed
 Iteration 2  ✅  6 / 6 passed
 Iteration 3  ✅  6 / 6 passed
 Iteration 4  ✅  6 / 6 passed
 Iteration 5  ✅  6 / 6 passed
```

**When a test fails**, click on that iteration to expand it:

```
 Iteration 3  ❌  5 / 6 passed
   ✅ Status is 201 Created
   ✅ Returned name matches input
   ❌ Returned price matches input
      AssertionError: expected 0 to equal 34.99
      → The price was sent as a string "34.99" instead of a number 34.99
        Fix: remove the quotes around {{price}} in the request body
```

**Postman Console** (`Ctrl+Alt+C` / `Cmd+Alt+C`) shows your `console.log()` output alongside each iteration — useful for seeing what values were actually used.

**Export results:** After a run, click **Export Results** (top-right of runner) to save a JSON summary — useful for sharing with the team or attaching to a bug report.

---

### The Most Common Mistakes (and How to Fix Them)

| Mistake | Symptom | Fix |
|---------|---------|-----|
| `"{{price}}"` (quoted number) | 400 Validation Failed — price invalid | Remove quotes: `{{price}}` |
| CSV has trailing spaces in column names | Variable not resolving | Check column headers have no spaces around names |
| Data file not selected in Runner | All `{{variable}}` resolve to empty string | In Runner, click Select File before running |
| Request not inside a Collection | Collection Runner not available | Move the request into a collection |
| Blank CSV cells send `?param=` | Filter behaves differently than expected | Use the `if (value && value.length > 0)` guard in scripts |
| `parseInt` / `parseFloat` not used | `"200" === 200` fails (string vs number) | Wrap with `parseInt()` or `parseFloat()` when comparing |

---

### How the Data File Values Flow

```
CSV row:                    name=Gaming Chair, price=249.99, category=Furniture

         ↓  Collection Runner reads the row

Request body before send:   { "name": "{{name}}", "price": {{price}}, ... }

         ↓  Postman resolves variables

Request body sent:          { "name": "Gaming Chair", "price": 249.99, ... }

         ↓  Server responds with 201

Post-response script:       pm.iterationData.get("name")  →  "Gaming Chair"
                            pm.iterationData.get("price") →  "249.99"  ← string! use parseFloat()
```

> `pm.iterationData.get()` always returns a **string**. Use `parseInt()` or `parseFloat()` before numeric comparisons.

---

## 15. File Upload Operations

### What Makes File Uploads Different?

So far, every request you've sent has had a text body — either nothing (GET) or JSON (POST/PUT). File uploads are different because you're sending **binary data** (images, PDFs, CSVs) alongside optional text metadata.

This changes two things:
1. **Content-Type** changes from `application/json` to `multipart/form-data`
2. The **body format** changes from a JSON object to named "parts" — each part contains either text or a file

```
Regular JSON body:                   Multipart body:
──────────────────                   ─────────────────────────────────────────────────
{                                    --boundary
  "name": "value"                    Content-Disposition: form-data; name="description"
}
                                     A text part value here
                                     --boundary
                                     Content-Disposition: form-data; name="file"; filename="photo.jpg"
                                     Content-Type: image/jpeg

                                     [binary file content here]
                                     --boundary--
```

| Feature | JSON Request | File Upload (multipart) |
|---------|-------------|------------------------|
| Content-Type | `application/json` | `multipart/form-data` |
| Body format | Text (JSON string) | Binary parts |
| Postman Body tab setting | raw → JSON | form-data |
| Suitable for | Structured data | Files + optional metadata |

---

### Our File Upload Endpoints

Both endpoints live on the same server you have been using throughout this guide — `http://localhost:8080`. No external service needed.

| Method | URL | Auth | What it does |
|--------|-----|------|-------------|
| `POST` | `http://localhost:8080/api/files/upload` | ADMIN only | Upload one file with an optional description |
| `POST` | `http://localhost:8080/api/files/upload-multiple` | ADMIN only | Upload two or more files with an optional category label |

> **Start the server first** (if it is not already running): `mvn spring-boot:run` — same as every other section.

---

### API 1: Single File Upload

**Full URL:** `http://localhost:8080/api/files/upload`

Accepts one file and an optional text description. Returns the file's name, type, size, and a confirmation message.

#### Exercise — Upload a Single File in Postman

1. Open a new request tab
2. Set method to **POST**
3. Set URL to: `http://localhost:8080/api/files/upload`
4. Click the **Authorization** tab → Type: **Basic Auth** → Username: `admin` → Password: `password123`
5. Click the **Body** tab
6. Select **form-data** (NOT raw, NOT JSON — this is the key difference from all previous requests)

Add these two rows in the form-data table:

| Key | Type | Value |
|-----|------|-------|
| `file` | **File** | Click the file selector on the right → pick any file from your computer |
| `description` | Text | `My first uploaded file` |

> **Critical — the Type dropdown:** After you type `file` as the key, look for a small dropdown on the right side of that row that currently says `Text`. Click it and change it to **File**. Only then does a file picker appear in the Value column. If you leave it as `Text`, Postman sends the word "undefined" as plain text instead of the actual file, and the server returns 400.

7. Click **Send**

#### What the server returns (201 Created)

```json
{
  "fileName": "photo.jpg",
  "fileType": "image/jpeg",
  "size": 204800,
  "description": "My first uploaded file",
  "message": "File uploaded successfully"
}
```

**What to notice:**
- `fileName` — the original name of the file as it existed on your machine
- `fileType` — the MIME type (`image/jpeg`, `application/pdf`, `text/plain`, etc.)
- `size` — file size in bytes
- `Content-Type` in the request was set to `multipart/form-data` **automatically by Postman** when you selected `form-data` — you never typed it

> **Compare with Section 5:** In Section 5 you selected `raw → JSON` and Postman set `Content-Type: application/json`. Here you selected `form-data` and Postman sets `Content-Type: multipart/form-data; boundary=...`. The body format you pick in Postman always drives the Content-Type header.

#### Try the error cases

**Omit the file (send description only):**
Remove the `file` row entirely and click Send.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "No file received. Make sure you selected a file and set the Type to 'File' in Postman."
}
```

**Send without auth:**
Remove the Authorization header and click Send → `401 Unauthorized`

**Send as USER instead of ADMIN:**
Change credentials to `user` / `user123` → `403 Forbidden`

---

### API 2: Multiple File Upload

**Full URL:** `http://localhost:8080/api/files/upload-multiple`

Accepts two or more files in a single request, plus an optional category label. Returns the total count and per-file details.

#### Exercise — Upload Multiple Files in Postman

1. Open a new request tab
2. Set method to **POST**
3. Set URL to: `http://localhost:8080/api/files/upload-multiple`
4. Click the **Authorization** tab → Type: **Basic Auth** → Username: `admin` → Password: `password123`
5. Click the **Body** tab → select **form-data**

Add these rows — set Type to **File** for every `files` row:

| Key | Type | Value |
|-----|------|-------|
| `files` | **File** | Pick any file from your computer |
| `files` | **File** | Pick a second file |
| `files` | **File** | Pick a third file |
| `category` | Text | `Product Photos` |

> **Same key name on every file row — this is intentional.** HTTP multipart sends arrays by repeating the same field name. Spring Boot collects all parts named `files` into a `List<MultipartFile>`. This is different from JSON where you'd write `"files": [...]`.

6. Click **Send**

#### What the server returns (201 Created)

```json
{
  "uploadedCount": 3,
  "category": "Product Photos",
  "files": [
    {
      "fileName": "front.jpg",
      "fileType": "image/jpeg",
      "size": 102400
    },
    {
      "fileName": "back.jpg",
      "fileType": "image/jpeg",
      "size": 98304
    },
    {
      "fileName": "side.jpg",
      "fileType": "image/jpeg",
      "size": 87040
    }
  ],
  "message": "3 file(s) uploaded successfully"
}
```

**What to notice:**
- `uploadedCount` tells you how many files the server actually received
- `files` is an array — one entry per file — each with its own `fileName`, `fileType`, and `size`
- `category` echoes back the text label you sent

#### Try the error cases

**Send only one files row instead of multiple:**
You can send just one file — the server accepts it and returns `uploadedCount: 1`.

**Send no files at all:**
Remove all `files` rows, keep only `category` → `400 Bad Request`

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "No files received. Add at least one file row with Type set to 'File' in Postman."
}
```

---

### How to See the Endpoints in Swagger

With the server running, open `http://localhost:8080/swagger-ui.html`. You will see a new **File Upload** group alongside Products and Orders. Click any endpoint there and use **Try it out** to upload directly from the browser — Swagger handles the multipart form-data automatically.

---

### How the Code Works (inside FileUploadController.java)

Open [FileUploadController.java](src/main/java/com/learn/restapi/controller/FileUploadController.java) to follow along.

**Single file endpoint:**
```java
@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<FileUploadResponse> uploadSingleFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam(required = false) String description) {
```

- `consumes = MediaType.MULTIPART_FORM_DATA_VALUE` — tells Spring this endpoint only accepts `multipart/form-data`, not JSON
- `@RequestParam("file") MultipartFile file` — Spring reads the form-data part named `file` and wraps it in a `MultipartFile` object
- `MultipartFile` gives you: `getOriginalFilename()`, `getContentType()`, `getSize()`, `getBytes()`, `getInputStream()`
- `@RequestParam(required = false) String description` — reads the `description` text part; null if not sent

**Multiple files endpoint:**
```java
@PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<MultiFileUploadResponse> uploadMultipleFiles(
        @RequestParam("files") List<MultipartFile> files,
        @RequestParam(required = false) String category) {
```

- `List<MultipartFile> files` — Spring collects every form-data part named `files` into this list
- That is the only difference from the single-file endpoint — one object becomes a list

| What you set in Postman | What Spring receives |
|------------------------|---------------------|
| Body → form-data | `Content-Type: multipart/form-data` |
| Key `file`, Type File | `@RequestParam("file") MultipartFile file` |
| Key `description`, Type Text | `@RequestParam String description` |
| Key `files` repeated 3 times, Type File each | `@RequestParam("files") List<MultipartFile> files` — 3 items |

---

### Common File Upload Errors

| Error | Status | Cause | Fix |
|-------|--------|-------|-----|
| Unsupported Media Type | 415 | Body set to `raw → JSON` instead of `form-data` | Switch Postman Body tab to **form-data** |
| No file received / Bad Request | 400 | The `file` key row's Type was left as `Text` | Change the Type dropdown to **File** for the file row |
| No files received / Bad Request | 400 | Sent `category` text but no actual file rows | Add at least one file row with Type **File** |
| Unauthorized | 401 | No Authorization header | Add Basic Auth: admin / password123 |
| Forbidden | 403 | Using `user` credentials instead of `admin` | Use admin / password123 — file upload is ADMIN only |
| Payload Too Large | 413 | File exceeds the 10 MB per-file limit | Use a smaller file (limit is set in application.properties) |

---

### File Upload vs JSON — When to Use Which

| Use `raw → JSON` when... | Use `form-data` when... |
|--------------------------|------------------------|
| Sending structured data (product details, order) | Sending a file (image, PDF, CSV, video) |
| All values are text or numbers | At least one value is binary file data |
| You need nested objects in the body | Flat key-value pairs alongside a file |
| Creating or updating a resource with text data | Attaching a document or photo to a resource |

---

## 16. Build a Tiny REST API (Your Exercise)

Now you build one. Add a **Coupon** feature to this project.

### Requirements

A coupon has:
- `id` (auto-generated)
- `code` (e.g., `SAVE10`) — must not be blank
- `discountPercent` — integer 1–100
- `active` — boolean

### Endpoints to build

| Method | URL | Access | Description |
|--------|-----|--------|-------------|
| GET | `/api/coupons` | Public | List all active coupons |
| GET | `/api/coupons/{code}` | Public | Get coupon by code |
| POST | `/api/coupons` | ADMIN | Create a coupon |
| DELETE | `/api/coupons/{code}` | ADMIN | Deactivate a coupon |

### Files to create

1. `model/Coupon.java` — model with validation annotations
2. `service/CouponService.java` — in-memory list with seed data
3. `controller/CouponController.java` — REST controller

### Tips

- Copy the structure from `ProductController.java` as a starting point
- Use `@PathVariable String code` instead of `Long id`
- For deactivation: instead of removing, set `active = false` and return `204 No Content`
- Add `@Tag(name = "Coupons")` to show up in Swagger
- Test with Postman: create → list → get by code → deactivate → list again

### Verification checklist — Postman

- [ ] GET /api/coupons returns 200 with the seed coupons
- [ ] GET /api/coupons/INVALID returns 404 with structured error
- [ ] POST /api/coupons with no auth returns 401
- [ ] POST /api/coupons with admin auth and valid body returns 201
- [ ] POST /api/coupons with `discountPercent: 150` returns 400 validation error
- [ ] GET /api/coupons shows up in Swagger UI at /swagger-ui.html

### Verification checklist — REST-assured

Add your tests to `ECommerceApiTest.java` following the pattern from Section 12. Cover the same scenarios:

- [ ] `getCoupons_returns200AndNonEmptyList`
- [ ] `getCouponByCode_withInvalidCode_returns404`
- [ ] `createCoupon_withNoAuth_returns401`
- [ ] `createCoupon_asAdmin_returns201`
- [ ] `createCoupon_withInvalidDiscountPercent_returns400WithFieldErrors`

---

## Quick Reference — Things to Remember

### HTTP Methods
```
GET    → Read, no body, safe to repeat
POST   → Create, has body
PUT    → Replace, has body
PATCH  → Partial update
DELETE → Remove, usually no body
```

### Status Code Cheat Sheet
```
200 OK           → Success, body returned
201 Created      → Resource created (POST)
204 No Content   → Success, no body (DELETE)
400 Bad Request  → Invalid input from client
401 Unauthorized → Not logged in
403 Forbidden    → Logged in but not allowed
404 Not Found    → Resource doesn't exist
500 Server Error → Bug in server code
```

### Your API endpoints (copy into Postman)
```
GET    http://localhost:8080/api/products
GET    http://localhost:8080/api/products/1
GET    http://localhost:8080/api/products?category=Electronics
GET    http://localhost:8080/api/products?minPrice=50&maxPrice=200
POST   http://localhost:8080/api/products          (admin/password123)
PUT    http://localhost:8080/api/products/1        (admin/password123)
DELETE http://localhost:8080/api/products/1        (admin/password123)
GET    http://localhost:8080/api/orders            (user/user123)
POST   http://localhost:8080/api/orders            (user/user123)
PATCH  http://localhost:8080/api/orders/1/status   (user/user123)
GET    http://localhost:8080/swagger-ui.html       (no auth)
GET    http://localhost:8080/v3/api-docs           (no auth)
POST   http://localhost:8080/api/files/upload          (admin/password123) — form-data, key: file (File type)
POST   http://localhost:8080/api/files/upload-multiple (admin/password123) — form-data, key: files (File type, repeat per file)
```

### Sample request bodies

**Create Product:**
```json
{
  "name": "Product Name",
  "description": "A description here",
  "price": 99.99,
  "category": "Electronics",
  "stock": 50
}
```

**Place Order:**
```json
{
  "productId": 1,
  "quantity": 2
}
```

**Update Order Status:**
```json
{
  "status": "CONFIRMED"
}
```
Valid statuses: `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`

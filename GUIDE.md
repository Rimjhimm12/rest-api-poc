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
13. [Build a Tiny REST API (Your Exercise)](#13-build-a-tiny-rest-api-your-exercise)

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

Once you complete the exercise in Section 13 (Coupon feature), add a test for it:

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

## 13. Build a Tiny REST API (Your Exercise)

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

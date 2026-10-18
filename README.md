# API Error Spring Boot Starter

A reusable Spring Boot library that provides centralized exception handling and consistent JSON error responses for REST APIs.

Instead of implementing error-handling logic in every project, this library provides a common response structure and handles common HTTP, validation, request parsing, and database-related exceptions.

## Features

- **Centralized Exception Handling** using `@RestControllerAdvice`.
- **Consistent Error Responses** with a standardized JSON structure.
- **Validation Error Handling** for `@Valid` and Bean Validation constraints.
- **Field-Level Validation Errors** that identify which fields contain invalid values.
- **Request Information** including the request path and HTTP method.
- **Request Body Error Handling** for missing or malformed JSON and incompatible JSON types.
- **HTTP Exception Handling** for unsupported methods, unsupported media types, missing parameters, and other request errors.
- **Database Exception Handling** for data integrity violations and optimistic locking failures.
- **Configurable Error Response Factory** for creating consistent error responses.
- **Safe Error Messages** that avoid exposing internal exception details to API clients.

## Tech Stack

- Java
- Spring Boot
- Spring Web MVC
- Jakarta Bean Validation
- Jakarta Persistence API

## Error Response Structure

All handled exceptions return a standardized JSON response.

```json
{
  "timestamp": "2026-10-18T05:10:12.4383299",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/products",
  "method": "POST",
  "details": [
    "One or more fields contain invalid values"
  ],
  "fieldErrors": {
    "name": "Product name is required",
    "price": "Price must be greater than zero"
  }
}
```

### Response Fields

| Field | Description |
|---|---|
| `timestamp` | Date and time when the error response was created. |
| `status` | HTTP status code. |
| `error` | Standard HTTP status reason phrase. |
| `message` | Human-readable error message. |
| `path` | Request URI that caused the error. |
| `method` | HTTP request method, such as `GET` or `POST`. |
| `details` | Additional information about the error. |
| `fieldErrors` | A map of field names to their validation error messages. |

For errors unrelated to field validation, `details` and `fieldErrors` are empty collections by default.

## Supported Exception Categories

The library currently handles the following exception types:

| Category | Examples | HTTP Status |
|---|---|---|
| Request validation | `MethodArgumentNotValidException` | 400 |
| Invalid request body | `HttpMessageNotReadableException` | 400 |
| Missing request parameter | `MissingServletRequestParameterException` | 400 |
| Missing multipart part | `MissingServletRequestPartException` | 400 |
| Parameter type mismatch | `MethodArgumentTypeMismatchException` | 400 |
| Constraint violations | `ConstraintViolationException` | 400 |
| Form/model binding errors | `BindException` | 400 |
| Resource not found | `EntityNotFoundException`, `NoResourceFoundException` | 404 |
| Unsupported HTTP method | `HttpRequestMethodNotSupportedException` | 405 |
| Unacceptable response media type | `HttpMediaTypeNotAcceptableException` | 406 |
| Data integrity violation | `DataIntegrityViolationException` | 409 |
| Optimistic locking conflict | `OptimisticLockingFailureException` | 409 |
| Unsupported request media type | `HttpMediaTypeNotSupportedException` | 415 |
| Maximum upload size exceeded | `MaxUploadSizeExceededException` | 413 |
| Unexpected exceptions | `Exception` | 500 |
| Asynchronous request timeout | `AsyncRequestTimeoutException` | 503 |
| Explicit HTTP status exceptions | `ResponseStatusException` | Corresponding status |

The actual HTTP status and behavior may depend on Spring MVC configuration and the exception raised.

## Validation Example

### 1. Define a Request DTO

```java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateProductRequest(

    @NotBlank(message = "Product name is required")
    String name,

    @Positive(message = "Price must be greater than zero")
    BigDecimal price

) {}
```

### 2. Validate the Request

```java
@PostMapping("/products")
public ResponseEntity<String> createProduct(
        @Valid @RequestBody CreateProductRequest request
) {
    return ResponseEntity.ok("Product created");
}
```

Ensure that the application has the `spring-boot-starter-validation` dependency.

### 3. Send an Invalid Request

```json
{
  "name": "",
  "price": -10
}
```

### 4. Receive a Structured Error Response

```json
{
  "timestamp": "2026-10-18T05:10:12",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/products",
  "method": "POST",
  "details": [
    "One or more fields contain invalid values"
  ],
  "fieldErrors": {
    "name": "Product name is required",
    "price": "Price must be greater than zero"
  }
}
```

The validation messages are collected automatically from the DTO constraints, so individual controllers do not need to construct their own error responses.

## ErrorResponseFactory

The library includes an `ErrorResponseFactory` component responsible for creating standardized error responses.

It supports creating responses with default empty collections:

```java
ApiErrorResponse response = factory.create(
    HttpStatus.NOT_FOUND,
    "Resource not found",
    request
);
```

It also supports responses with additional details and field-level errors:

```java
ApiErrorResponse response = factory.create(
    HttpStatus.BAD_REQUEST,
    "Validation failed",
    request,
    List.of("One or more fields contain invalid values"),
    fieldErrors
);
```

This keeps response creation consistent and reduces duplicated code across exception handlers.

## Installation

From the library's root directory, run:

```bash
mvn clean install
```

Then add the dependency to the consuming application's `pom.xml`:

```xml
<dependency>
    <groupId>io.github.mayafouad</groupId>
    <artifactId>api-error-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

Replace the coordinates and version with the values defined in the library's `pom.xml`.

## Requirements

- A compatible Java version.
- A compatible Spring Boot application.
- Spring Web MVC.
- Jakarta Bean Validation for request validation features.

*Use compatible dependency versions for the Spring Boot version targeted by the library.*

## Current Limitations

- Validation handling currently focuses on the exception types explicitly registered in the global exception handler.
- Request IDs are not currently included in the response structure.


---

**Author:** Maya Fouad Fathy

**Project:** API Error Spring Boot Starter

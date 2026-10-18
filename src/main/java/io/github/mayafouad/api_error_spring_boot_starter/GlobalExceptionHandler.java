
package io.github.mayafouad.api_error_spring_boot_starter;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;

import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;


import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorResponseFactory factory;

    public GlobalExceptionHandler(ErrorResponseFactory factory) {
        this.factory = factory;
    }

    // 400 - Validation errors in request body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage())
                .distinct()
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    // 400 - Invalid JSON, wrong JSON types, unreadable body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.BAD_REQUEST,
                "Request body is missing or invalid",
                request
        );
    }

    // 400 - Missing query parameter
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.BAD_REQUEST,
                "Missing required parameter: " + ex.getParameterName(),
                request
        );
    }

    // 400 - Missing multipart file/part
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingPart(
            MissingServletRequestPartException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.BAD_REQUEST,
                "Missing required request part: " + ex.getRequestPartName(),
                request
        );
    }

    // 400 - Wrong parameter type
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.BAD_REQUEST,
                "Invalid value for parameter: " + ex.getName(),
                request
        );
    }

    // 400 - Constraint violations in method parameters
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                request
        );
    }
    
    // 400 - Form or model binding validation errors
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(
            BindException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage())
                .distinct()
                .reduce((a, b) -> a + "; " + b)
                .orElse("Request validation failed");

        return build(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    // 404 - Explicit entity-not-found errors
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                request
        );
    }

     // 404 - Database query returned no result
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleEmptyResult(
            EmptyResultDataAccessException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.NOT_FOUND,
                "Requested resource was not found",
                request
        );
    }
    // 404 - Missing endpoint/resource handled by Spring MVC
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                request
        );
    }

    // 405 - HTTP method not supported
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        HttpHeaders headers = new HttpHeaders();

        if (ex.getSupportedHttpMethods() != null) {
            headers.setAllow(ex.getSupportedHttpMethods());
        }

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(headers)
                .body(factory.create(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "HTTP method not allowed",
                        request
                ));
    }

    // 406 - Requested response media type is not supported
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotAcceptable(
            HttpMediaTypeNotAcceptableException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.NOT_ACCEPTABLE,
                "Requested response format is not supported",
                request
        );
    }

    // 409 - Data integrity violation
    // Example: duplicate unique value or foreign-key constraint
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.CONFLICT,
                "The request conflicts with existing data",
                request
        );
    }

    // 409 - Concurrent update conflict
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLock(
            OptimisticLockingFailureException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.CONFLICT,
                "The resource was modified by another operation",
                request
        );
    }

    // 415 - Unsupported request Content-Type
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Request content type is not supported",
                request
        );
    }

    // 413 - Uploaded file exceeds configured size limit
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Uploaded file exceeds the maximum allowed size",
                request
        );
    }

    // 500 - Any unexpected exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {

            return build(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred",
                    request
            );
    }

    // 500 - Response could not be serialized or written
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseWriteError(
            HttpMessageNotWritableException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to generate the response",
                request
        );
    }

    // 503 - Asynchronous request timed out
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<ApiErrorResponse> handleAsyncTimeout(
            AsyncRequestTimeoutException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.SERVICE_UNAVAILABLE,
                "The request timed out; please try again later",
                request
        );
    }

    // Preserve explicit HTTP status exceptions
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.resolve(
                ex.getStatusCode().value()
        );

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String message = status.is5xxServerError()
                ? "An unexpected error occurred"
                : ex.getReason() != null
                    ? ex.getReason()
                    : status.getReasonPhrase();

        return build(status, message, request);
    }
    
    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        return ResponseEntity
                .status(status)
                .body(factory.create(status, message, request));
    }
}

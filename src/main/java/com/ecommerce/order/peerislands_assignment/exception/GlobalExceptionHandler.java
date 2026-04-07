package com.ecommerce.order.peerislands_assignment.exception;

import com.ecommerce.order.peerislands_assignment.exception.InvalidOrderStateException;
import com.ecommerce.order.peerislands_assignment.exception.OrderNotFoundException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleOrderNotFound(
      OrderNotFoundException ex, WebRequest request) {
    return buildErrorResponse(
        HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", ex.getMessage(), request.getDescription(false));
  }

  @ExceptionHandler(InvalidOrderStateException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidOrderState(
      InvalidOrderStateException ex, WebRequest request) {
    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        "INVALID_ORDER_STATE",
        ex.getMessage(),
        request.getDescription(false));
  }

  @ExceptionHandler(InvalidOrderRequestException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidOrderRequest(
      InvalidOrderRequestException ex, WebRequest request) {
    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        "INVALID_ORDER_REQUEST",
        ex.getMessage(),
        request.getDescription(false));
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<Map<String, Object>> handleOptimisticLock(
      OptimisticLockingFailureException ex, WebRequest request) {
    return buildErrorResponse(
        HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION", ex.getMessage(), request.getDescription(false));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(
      MethodArgumentNotValidException ex, WebRequest request) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .orElse("Validation failed");
    return buildErrorResponse(
        HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message, request.getDescription(false));
  }

  @ExceptionHandler({
      MethodArgumentTypeMismatchException.class,
      TypeMismatchException.class,
      BindException.class
  })
  public ResponseEntity<Map<String, Object>> handleTypeMismatch(
      Exception ex, WebRequest request) {
    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        "INVALID_QUERY_PARAMETER",
        "Invalid request parameter value",
        request.getDescription(false));
  }

  @ExceptionHandler(InvalidDataAccessApiUsageException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidDataAccess(
      InvalidDataAccessApiUsageException ex, WebRequest request) {
    String message = "Invalid pagination or sort parameter";
    if (ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null) {
      String cause = ex.getMostSpecificCause().getMessage();
      if (cause.contains("No property")) {
        message = "Invalid sort property";
      }
    }
    return buildErrorResponse(
        HttpStatus.BAD_REQUEST, "INVALID_QUERY_PARAMETER", message, request.getDescription(false));
  }

  private ResponseEntity<Map<String, Object>> buildErrorResponse(
      HttpStatus status, String errorCode, String message, String pathDescription) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", status.value());
    body.put("error", errorCode);
    body.put("message", message);
    body.put("path", pathDescription);
    return ResponseEntity.status(status).body(body);
  }
}


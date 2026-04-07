package com.ecommerce.order.peerislands_assignment.controller;

import com.ecommerce.order.peerislands_assignment.dto.OrderRequest;
import com.ecommerce.order.peerislands_assignment.dto.OrderResponse;
import com.ecommerce.order.peerislands_assignment.dto.UpdateOrderStatusRequest;
import com.ecommerce.order.peerislands_assignment.model.OrderStatus;
import com.ecommerce.order.peerislands_assignment.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  @Operation(
      summary = "Create a new order",
      description =
          "Use idempotencyKey to safely retry the same create request without creating duplicate orders.")
  public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
    OrderResponse response = orderService.createOrder(request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get an order by id")
  public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
    OrderResponse response = orderService.getOrderById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(summary = "List orders with optional status filter")
  public ResponseEntity<Page<OrderResponse>> listOrders(
      @RequestParam(name = "status", required = false) OrderStatus status, Pageable pageable) {
    Page<OrderResponse> page = orderService.getAllOrders(Optional.ofNullable(status), pageable);
    return ResponseEntity.ok(page);
  }

  @PatchMapping("/{id}/status")
  @Operation(summary = "Update order status (explicit status endpoint)")
  public ResponseEntity<OrderResponse> updateStatusOnly(
      @PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
    return updateOrderStatusInternal(id, request);
  }

  private ResponseEntity<OrderResponse> updateOrderStatusInternal(
      Long id, UpdateOrderStatusRequest request) {
    OrderResponse response = orderService.updateOrderStatus(id, request.getStatus());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/cancel")
  @Operation(summary = "Cancel an order")
  public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
    OrderResponse response = orderService.cancelOrder(id);
    return ResponseEntity.ok(response);
  }
}


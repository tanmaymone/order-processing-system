package com.ecommerce.order.peerislands_assignment.mapper;

import com.ecommerce.order.peerislands_assignment.dto.OrderItemRequest;
import com.ecommerce.order.peerislands_assignment.dto.OrderItemResponse;
import com.ecommerce.order.peerislands_assignment.dto.OrderRequest;
import com.ecommerce.order.peerislands_assignment.dto.OrderResponse;
import com.ecommerce.order.peerislands_assignment.model.Order;
import com.ecommerce.order.peerislands_assignment.model.OrderItem;
import com.ecommerce.order.peerislands_assignment.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public final class OrderMapper {

  private OrderMapper() {
  }

  public static Order toOrderEntity(OrderRequest request) {
    Instant now = Instant.now();

    Order order = Order.builder()
        .customerName(request.getCustomerName())
        .status(OrderStatus.PENDING)
        .statusUpdatedAt(LocalDateTime.now())
        .totalAmount(BigDecimal.ZERO)
        .idempotencyKey(request.getIdempotencyKey())
        .build();

    List<OrderItem> items = request.getItems()
        .stream()
        .map(itemRequest -> toOrderItemEntity(itemRequest, order))
        .collect(Collectors.toList());

    order.setItems(items);
    order.setTotalAmount(
        items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
    );

    return order;
  }

  private static OrderItem toOrderItemEntity(OrderItemRequest request, Order order) {
    BigDecimal subtotal = request.getUnitPrice()
        .multiply(BigDecimal.valueOf(request.getQuantity()));

    return OrderItem.builder()
        .productId(request.getProductId())
        .productName(request.getProductName())
        .quantity(request.getQuantity())
        .unitPrice(request.getUnitPrice())
        .subtotal(subtotal)
        .order(order)
        .build();
  }

  public static OrderResponse toOrderResponse(Order order) {
    List<OrderItemResponse> itemResponses = order.getItems()
        .stream()
        .map(OrderMapper::toOrderItemResponse)
        .collect(Collectors.toList());

    return OrderResponse.builder()
        .id(order.getId())
        .customerName(order.getCustomerName())
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .statusUpdatedAt(order.getStatusUpdatedAt())
        .status(order.getStatus())
        .totalAmount(order.getTotalAmount())
        .items(itemResponses)
        .build();
  }

  private static OrderItemResponse toOrderItemResponse(OrderItem item) {
    return OrderItemResponse.builder()
        .id(item.getId())
        .productId(item.getProductId())
        .productName(item.getProductName())
        .quantity(item.getQuantity())
        .unitPrice(item.getUnitPrice())
        .subtotal(item.getSubtotal())
        .build();
  }
}


package com.ecommerce.order.peerislands_assignment.dto;

import com.ecommerce.order.peerislands_assignment.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponse {

  private final Long id;
  private final String customerName;
  private final Instant createdAt;
  private final Instant updatedAt;
  private final LocalDateTime statusUpdatedAt;
  private final OrderStatus status;
  private final BigDecimal totalAmount;
  private final List<OrderItemResponse> items;
}


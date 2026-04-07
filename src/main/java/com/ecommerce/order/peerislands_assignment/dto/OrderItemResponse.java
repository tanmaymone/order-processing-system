package com.ecommerce.order.peerislands_assignment.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemResponse {

  private final Long id;
  private final String productId;
  private final String productName;
  private final Integer quantity;
  private final BigDecimal unitPrice;
  private final BigDecimal subtotal;
}


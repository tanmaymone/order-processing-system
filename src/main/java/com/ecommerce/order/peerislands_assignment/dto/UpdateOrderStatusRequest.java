package com.ecommerce.order.peerislands_assignment.dto;

import com.ecommerce.order.peerislands_assignment.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {

  @NotNull
  @Schema(
      description = "Target status for the order. Must follow valid state transitions.",
      example = "PROCESSING")
  private OrderStatus status;
}


package com.ecommerce.order.peerislands_assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {

  @NotBlank
  @Schema(
      description = "Customer full name for whom the order is being created",
      example = "Tanmay Mone")
  private String customerName;

  @Schema(
      description =
          "Client-generated unique key for idempotent create requests. Reuse the same key only when retrying the exact same order request.",
      example = "a5b8a5c2-7e7f-4fef-9f40-3b5ef3a9f2b1")
  private String idempotencyKey;

  @NotNull
  @Size(min = 1)
  @Schema(
      description = "At least one item is required. Duplicate productId entries are not allowed.")
  private List<OrderItemRequest> items;
}


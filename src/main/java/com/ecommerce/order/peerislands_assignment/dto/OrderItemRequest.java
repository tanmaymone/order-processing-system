package com.ecommerce.order.peerislands_assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {

  @NotBlank
  @Schema(description = "Unique product identifier (SKU or catalog id)", example = "SKU-1001")
  private String productId;

  @NotBlank
  @Schema(description = "Display name of the product", example = "Wireless Mouse")
  private String productName;

  @NotNull
  @Positive
  @Schema(description = "Number of units to order. Must be greater than 0", example = "2")
  private Integer quantity;

  @NotNull
  @Positive
  @Schema(description = "Unit price for one item. Must be greater than 0", example = "799.00")
  private BigDecimal unitPrice;
}


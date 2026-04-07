package com.ecommerce.order.peerislands_assignment.exception;

import com.ecommerce.order.peerislands_assignment.model.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {

  public InvalidOrderStateException(Long id, OrderStatus current, OrderStatus target) {
    super(
        "Invalid status transition for order "
            + id
            + " from "
            + current
            + " to "
            + target);
  }
}


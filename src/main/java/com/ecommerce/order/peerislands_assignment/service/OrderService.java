package com.ecommerce.order.peerislands_assignment.service;

import com.ecommerce.order.peerislands_assignment.dto.OrderRequest;
import com.ecommerce.order.peerislands_assignment.dto.OrderResponse;
import com.ecommerce.order.peerislands_assignment.model.OrderStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

  OrderResponse createOrder(OrderRequest request);

  OrderResponse getOrderById(Long id);

  Page<OrderResponse> getAllOrders(Optional<OrderStatus> status, Pageable pageable);

  OrderResponse updateOrderStatus(Long id, OrderStatus newStatus);

  OrderResponse cancelOrder(Long id);

  int promotePendingOrdersToProcessing();
}


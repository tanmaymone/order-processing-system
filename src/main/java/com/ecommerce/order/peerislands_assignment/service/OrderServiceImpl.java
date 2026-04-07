package com.ecommerce.order.peerislands_assignment.service;

import com.ecommerce.order.peerislands_assignment.dto.OrderRequest;
import com.ecommerce.order.peerislands_assignment.dto.OrderResponse;
import com.ecommerce.order.peerislands_assignment.exception.InvalidOrderRequestException;
import com.ecommerce.order.peerislands_assignment.exception.InvalidOrderStateException;
import com.ecommerce.order.peerislands_assignment.exception.OrderNotFoundException;
import com.ecommerce.order.peerislands_assignment.mapper.OrderMapper;
import com.ecommerce.order.peerislands_assignment.model.Order;
import com.ecommerce.order.peerislands_assignment.model.OrderStatus;
import com.ecommerce.order.peerislands_assignment.repository.OrderRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;

  @Override
  @Transactional
  public OrderResponse createOrder(OrderRequest request) {
    log.info("Creating order for customer {}", request.getCustomerName());
    validateOrderItems(request);
    if (request.getIdempotencyKey() != null) {
      Optional<Order> existing =
          orderRepository.findByIdempotencyKey(request.getIdempotencyKey());
      if (existing.isPresent()) {
        return OrderMapper.toOrderResponse(existing.get());
      }
    }

    Order order = OrderMapper.toOrderEntity(request);
    Order saved = orderRepository.save(order);
    return OrderMapper.toOrderResponse(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderResponse getOrderById(Long id) {
    Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    return OrderMapper.toOrderResponse(order);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> getAllOrders(Optional<OrderStatus> status, Pageable pageable) {
    Page<Order> page =
        status.map(orderStatus -> orderRepository.findByStatus(orderStatus, pageable))
            .orElseGet(() -> orderRepository.findAll(pageable));
    return page.map(OrderMapper::toOrderResponse);
  }

  @Override
  @Transactional
  public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
    Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

    if (!isValidTransition(order.getStatus(), newStatus)) {
      throw new InvalidOrderStateException(id, order.getStatus(), newStatus);
    }

    LocalDateTime statusUpdatedAt = LocalDateTime.now();
    Instant updatedAt = Instant.now();
    int updatedRows =
        orderRepository.transitionStatus(
            id, order.getStatus(), newStatus, statusUpdatedAt, updatedAt);
    if (updatedRows == 0) {
      throw new OptimisticLockingFailureException(
          "Order was modified concurrently while updating status for id " + id);
    }

    Order refreshed = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    return OrderMapper.toOrderResponse(refreshed);
  }

  @Override
  @Transactional
  public OrderResponse cancelOrder(Long id) {
    Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

    if (order.getStatus() != OrderStatus.PENDING) {
      throw new InvalidOrderStateException(id, order.getStatus(), OrderStatus.CANCELLED);
    }

    LocalDateTime statusUpdatedAt = LocalDateTime.now();
    Instant updatedAt = Instant.now();
    int updatedRows =
        orderRepository.transitionStatus(
            id, OrderStatus.PENDING, OrderStatus.CANCELLED, statusUpdatedAt, updatedAt);
    if (updatedRows == 0) {
      throw new OptimisticLockingFailureException(
          "Order was modified concurrently while cancelling order id " + id);
    }

    Order refreshed = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    return OrderMapper.toOrderResponse(refreshed);
  }

  @Override
  @Transactional
  public int promotePendingOrdersToProcessing() {
    LocalDateTime statusUpdatedAt = LocalDateTime.now();
    Instant updatedAt = Instant.now();
    return orderRepository.promotePendingOrders(
        OrderStatus.PENDING, OrderStatus.PROCESSING, statusUpdatedAt, updatedAt);
  }

  private boolean isValidTransition(OrderStatus current, OrderStatus target) {
    if (current == target) {
      return true;
    }
    return switch (current) {
      case PENDING -> target == OrderStatus.PROCESSING || target == OrderStatus.CANCELLED;
      case PROCESSING -> target == OrderStatus.SHIPPED;
      case SHIPPED -> target == OrderStatus.DELIVERED;
      case DELIVERED, CANCELLED -> false;
    };
  }

  private void validateOrderItems(OrderRequest request) {
    Set<String> normalizedIds =
        request.getItems().stream()
            .map(item -> item.getProductId() == null ? "" : item.getProductId().trim())
            .peek(productId -> {
              if (productId.isEmpty()) {
                throw new InvalidOrderRequestException("Product id must not be blank");
              }
            })
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

    if (normalizedIds.size() != request.getItems().size()) {
      throw new InvalidOrderRequestException("Duplicate productId entries are not allowed in an order");
    }
  }
}


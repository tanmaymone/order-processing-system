package com.ecommerce.order.peerislands_assignment.repository;

import com.ecommerce.order.peerislands_assignment.model.Order;
import com.ecommerce.order.peerislands_assignment.model.OrderStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

  Page<Order> findByStatus(OrderStatus status, Pageable pageable);

  Page<Order> findAll(Pageable pageable);

  Optional<Order> findByIdempotencyKey(String idempotencyKey);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE Order o "
          + "SET o.status = :targetStatus, "
          + "o.statusUpdatedAt = :statusUpdatedAt, "
          + "o.updatedAt = :updatedAt, "
          + "o.version = o.version + 1 "
          + "WHERE o.id = :id AND o.status = :currentStatus")
  int transitionStatus(
      @Param("id") Long id,
      @Param("currentStatus") OrderStatus currentStatus,
      @Param("targetStatus") OrderStatus targetStatus,
      @Param("statusUpdatedAt") LocalDateTime statusUpdatedAt,
      @Param("updatedAt") Instant updatedAt);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE Order o "
          + "SET o.status = :targetStatus, "
          + "o.statusUpdatedAt = :statusUpdatedAt, "
          + "o.updatedAt = :updatedAt, "
          + "o.version = o.version + 1 "
          + "WHERE o.status = :currentStatus")
  int promotePendingOrders(
      @Param("currentStatus") OrderStatus currentStatus,
      @Param("targetStatus") OrderStatus targetStatus,
      @Param("statusUpdatedAt") LocalDateTime statusUpdatedAt,
      @Param("updatedAt") Instant updatedAt);
}


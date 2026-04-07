package com.ecommerce.order.peerislands_assignment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ecommerce.order.peerislands_assignment.dto.OrderItemRequest;
import com.ecommerce.order.peerislands_assignment.dto.OrderRequest;
import com.ecommerce.order.peerislands_assignment.dto.OrderResponse;
import com.ecommerce.order.peerislands_assignment.exception.InvalidOrderRequestException;
import com.ecommerce.order.peerislands_assignment.exception.InvalidOrderStateException;
import com.ecommerce.order.peerislands_assignment.exception.OrderNotFoundException;
import com.ecommerce.order.peerislands_assignment.model.Order;
import com.ecommerce.order.peerislands_assignment.model.OrderStatus;
import com.ecommerce.order.peerislands_assignment.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class OrderServiceImplTest {

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private OrderServiceImpl orderService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createOrderShouldPersistAndReturnResponse() {
    OrderRequest request = buildOrderRequest("SKU-1", "Product 1", "idem-1");

    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);

    given(orderRepository.findByIdempotencyKey("idem-1")).willReturn(Optional.empty());
    given(orderRepository.save(captor.capture()))
        .willAnswer(invocation -> {
          Order value = invocation.getArgument(0);
          value.setId(1L);
          return value;
        });

    OrderResponse response = orderService.createOrder(request);

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getTotalAmount()).isEqualByComparingTo("20.00");
    assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(response.getItems()).hasSize(1);
  }

  @Test
  void createOrderShouldReturnExistingOrderForSameIdempotencyKey() {
    Order existing = buildOrder(99L, OrderStatus.PENDING);
    existing.setIdempotencyKey("idem-1");

    given(orderRepository.findByIdempotencyKey("idem-1")).willReturn(Optional.of(existing));

    OrderResponse response = orderService.createOrder(buildOrderRequest("SKU-1", "Product 1", "idem-1"));

    assertThat(response.getId()).isEqualTo(99L);
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  void createOrderShouldRejectDuplicateProductIdsIgnoringCaseAndWhitespace() {
    OrderRequest request = new OrderRequest();
    request.setCustomerName("John Doe");
    request.setIdempotencyKey("idem-dup");
    request.setItems(List.of(
        buildItemRequest("SKU-1", "Item A", 1, "10.00"),
        buildItemRequest(" sku-1 ", "Item B", 1, "20.00")));

    assertThatThrownBy(() -> orderService.createOrder(request))
        .isInstanceOf(InvalidOrderRequestException.class)
        .hasMessageContaining("Duplicate productId");
  }

  @Test
  void getOrderByIdNotFoundShouldThrow() {
    given(orderRepository.findById(1L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.getOrderById(1L))
        .isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  void getAllOrdersWithStatusFilterShouldUseRepository() {
    Order order = buildOrder(1L, OrderStatus.PENDING);

    given(orderRepository.findByStatus(eq(OrderStatus.PENDING), any(Pageable.class)))
        .willReturn(new PageImpl<>(List.of(order)));

    var page = orderService.getAllOrders(Optional.of(OrderStatus.PENDING), PageRequest.of(0, 10));

    assertThat(page.getContent()).hasSize(1);
  }

  @Test
  void updateOrderStatusShouldUpdateWhenTransitionIsValid() {
    Order order = buildOrder(1L, OrderStatus.PENDING);
    Order refreshed = buildOrder(1L, OrderStatus.PROCESSING);

    given(orderRepository.findById(1L)).willReturn(Optional.of(order), Optional.of(refreshed));
    given(orderRepository.transitionStatus(
        eq(1L), eq(OrderStatus.PENDING), eq(OrderStatus.PROCESSING), any(LocalDateTime.class), any(Instant.class)))
        .willReturn(1);

    OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.PROCESSING);

    assertThat(response.getStatus()).isEqualTo(OrderStatus.PROCESSING);
  }

  @Test
  void updateOrderStatusShouldThrowForInvalidTransition() {
    Order order = buildOrder(1L, OrderStatus.PENDING);
    given(orderRepository.findById(1L)).willReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED))
        .isInstanceOf(InvalidOrderStateException.class);
  }

  @Test
  void cancelOrderInNonPendingStateShouldThrow() {
    Order order = buildOrder(1L, OrderStatus.PROCESSING);
    given(orderRepository.findById(1L)).willReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.cancelOrder(1L))
        .isInstanceOf(InvalidOrderStateException.class);
  }

  @Test
  void cancelOrderShouldThrowOptimisticLockExceptionWhenConcurrentUpdateHappens() {
    Order order = buildOrder(1L, OrderStatus.PENDING);
    given(orderRepository.findById(1L)).willReturn(Optional.of(order));
    given(orderRepository.transitionStatus(
        eq(1L), eq(OrderStatus.PENDING), eq(OrderStatus.CANCELLED), any(LocalDateTime.class), any(Instant.class)))
        .willReturn(0);

    assertThatThrownBy(() -> orderService.cancelOrder(1L))
        .isInstanceOf(OptimisticLockingFailureException.class)
        .hasMessageContaining("concurrently");
  }

  @Test
  void promotePendingOrdersShouldReturnUpdatedCount() {
    given(orderRepository.promotePendingOrders(
        eq(OrderStatus.PENDING), eq(OrderStatus.PROCESSING), any(LocalDateTime.class), any(Instant.class)))
        .willReturn(5);

    int updated = orderService.promotePendingOrdersToProcessing();

    assertThat(updated).isEqualTo(5);
  }

  private OrderRequest buildOrderRequest(String productId, String productName, String idempotencyKey) {
    OrderRequest request = new OrderRequest();
    request.setCustomerName("John Doe");
    request.setIdempotencyKey(idempotencyKey);
    request.setItems(List.of(buildItemRequest(productId, productName, 2, "10.00")));
    return request;
  }

  private OrderItemRequest buildItemRequest(
      String productId, String productName, int quantity, String unitPrice) {
    OrderItemRequest itemRequest = new OrderItemRequest();
    itemRequest.setProductId(productId);
    itemRequest.setProductName(productName);
    itemRequest.setQuantity(quantity);
    itemRequest.setUnitPrice(new BigDecimal(unitPrice));
    return itemRequest;
  }

  private Order buildOrder(Long id, OrderStatus status) {
    Order order = new Order();
    order.setId(id);
    order.setCustomerName("John Doe");
    order.setStatus(status);
    order.setStatusUpdatedAt(LocalDateTime.now());
    order.setUpdatedAt(Instant.now());
    order.setTotalAmount(BigDecimal.TEN);
    order.setItems(List.of());
    return order;
  }
}


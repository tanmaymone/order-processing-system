package com.ecommerce.order.peerislands_assignment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ecommerce.order.peerislands_assignment.dto.OrderRequest;
import com.ecommerce.order.peerislands_assignment.dto.OrderResponse;
import com.ecommerce.order.peerislands_assignment.dto.UpdateOrderStatusRequest;
import com.ecommerce.order.peerislands_assignment.model.OrderStatus;
import com.ecommerce.order.peerislands_assignment.service.OrderService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.mockito.Mockito;

class OrderControllerTest {

  private final OrderService orderService = Mockito.mock(OrderService.class);
  private final OrderController controller = new OrderController(orderService);

  @Test
  void createOrderShouldReturnCreatedAndBody() {
    OrderRequest request = new OrderRequest();
    OrderResponse response = buildOrderResponse(1L, OrderStatus.PENDING);
    given(orderService.createOrder(request)).willReturn(response);

    MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    servletRequest.setRequestURI("/api/orders");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

    ResponseEntity<OrderResponse> result = controller.createOrder(request);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getId()).isEqualTo(1L);
    verify(orderService).createOrder(request);
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void getOrderShouldReturnOkAndBody() {
    OrderResponse response = buildOrderResponse(77L, OrderStatus.PROCESSING);
    given(orderService.getOrderById(77L)).willReturn(response);

    ResponseEntity<OrderResponse> result = controller.getOrder(77L);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getStatus()).isEqualTo(OrderStatus.PROCESSING);
  }

  @Test
  void listOrdersShouldReturnPageAndAcceptStatusFilter() {
    OrderResponse response = buildOrderResponse(10L, OrderStatus.PENDING);
    given(orderService.getAllOrders(
        Mockito.eq(Optional.of(OrderStatus.PENDING)), Mockito.any(Pageable.class)))
        .willReturn(new PageImpl<>(List.of(response)));

    ResponseEntity<?> result =
        controller.listOrders(OrderStatus.PENDING, org.springframework.data.domain.PageRequest.of(0, 5));

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isInstanceOf(PageImpl.class);
  }

  @Test
  void updateStatusShouldReturnUpdatedOrder() {
    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
    request.setStatus(OrderStatus.SHIPPED);
    OrderResponse response = buildOrderResponse(1L, OrderStatus.SHIPPED);
    given(orderService.updateOrderStatus(1L, OrderStatus.SHIPPED)).willReturn(response);

    ResponseEntity<OrderResponse> result = controller.updateStatusOnly(1L, request);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getStatus()).isEqualTo(OrderStatus.SHIPPED);
  }

  @Test
  void cancelOrderShouldReturnUpdatedOrder() {
    OrderResponse response = buildOrderResponse(5L, OrderStatus.CANCELLED);
    given(orderService.cancelOrder(5L)).willReturn(response);

    ResponseEntity<OrderResponse> result = controller.cancelOrder(5L);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getStatus()).isEqualTo(OrderStatus.CANCELLED);
  }

  private OrderResponse buildOrderResponse(Long id, OrderStatus status) {
    return OrderResponse.builder()
        .id(id)
        .customerName("John Doe")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .statusUpdatedAt(LocalDateTime.now())
        .status(status)
        .totalAmount(BigDecimal.valueOf(20))
        .items(List.of())
        .build();
  }
}

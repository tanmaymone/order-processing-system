package com.ecommerce.order.peerislands_assignment.scheduler;

import com.ecommerce.order.peerislands_assignment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusScheduler {

  private final OrderService orderService;

  @Scheduled(cron = "0 */5 * * * *")
  public void promotePendingOrders() {
    int updated = orderService.promotePendingOrdersToProcessing();
    if (updated > 0) {
      log.info("Promoted {} pending orders to PROCESSING", updated);
    }
  }
}


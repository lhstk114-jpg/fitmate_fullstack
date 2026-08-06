package org.spring.backend.shop.order.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionOrderRequestDto {
  private Long productId;

  private LocalDate startDate;
}

package org.spring.backend.shop.payment.dto;

import java.util.List;

import org.spring.backend.shop.order.dto.OrderItemDto;
import org.spring.backend.shop.product.type.ProductType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentSuccessDto {
  private String productName;
  private int amount;
  private String paymentMethod;

  private List<OrderItemDto> orderItems;

  private ProductType productType;
}

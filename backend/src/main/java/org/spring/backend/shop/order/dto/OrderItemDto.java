package org.spring.backend.shop.order.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.spring.backend.shop.order.entity.OrderItemEntity;
import org.spring.backend.shop.product.type.ProductType;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class OrderItemDto {
  private Long id;

  private int price;

  private int quantity;

  private String productName;

  private String productImage;

  private Long productId;

  private Long orderId;

  private LocalDate startDate;

  private ProductType productType;

  public static OrderItemDto toOrderItemDto(OrderItemEntity orderItemEntity) {
    return OrderItemDto.builder()
        .id(orderItemEntity.getId())
        .productName(orderItemEntity.getProductName())
        .price(orderItemEntity.getPrice())
        .quantity(orderItemEntity.getQuantity())
        .productImage(
            orderItemEntity.getProductEntity()
                .getFileEntities()
                .isEmpty()
                    ? null
                    : orderItemEntity.getProductEntity()
                        .getFileEntities()
                        .get(0)
                        .getNewFileName())
        .productId(orderItemEntity.getProductEntity().getId())
        .orderId(orderItemEntity.getOrderEntity().getId())
        .startDate(orderItemEntity.getStartDate())
        .productType(
            orderItemEntity
                .getProductEntity()
                .getProductType())
        .build();
  }
}

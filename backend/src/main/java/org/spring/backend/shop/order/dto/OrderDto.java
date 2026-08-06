package org.spring.backend.shop.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.spring.backend.shop.order.entity.OrderEntity;
import org.spring.backend.shop.order.type.DeliveryStatus;
import org.spring.backend.shop.order.type.OrderStatus;
import org.spring.backend.shop.payment.dto.PaymentDto;

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
public class OrderDto {
    private Long id;

    private int totalPrice;

    private OrderStatus orderStatus;

    private DeliveryStatus deliveryStatus;

    // 주문자 정보 (회원 정보)
    private String memberName;
    private String memberPhone;
    private String memberEmail;
    private String memberAddress;

    // 배송 정보 (주문 당시 저장)
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String deliveryMemo;
    private String receiverDetailAddress;

    private List<OrderItemDto> orderItemDtos;

    // 결제 내역
    private PaymentDto paymentDto;


    private LocalDateTime createTime;


    public static OrderDto toOrderDto(OrderEntity orderEntity) {


        return OrderDto.builder()
                .id(orderEntity.getId())
                .totalPrice(orderEntity.getTotalPrice())
                .orderStatus(orderEntity.getOrderStatus())
                .deliveryStatus(orderEntity.getDeliveryStatus())
                .memberName(orderEntity.getMemberEntity().getUserName())
                .memberPhone(orderEntity.getMemberEntity().getUserPhone())
                .memberEmail(orderEntity.getMemberEntity().getUserEmail())
                .memberAddress(orderEntity.getMemberEntity().getUserAddress())
                .orderItemDtos(orderEntity.getOrderItemEntities().stream().map(OrderItemDto::toOrderItemDto).toList())
                .paymentDto(
                        orderEntity.getPaymentEntities().isEmpty()
                                ? null
                                : PaymentDto.toPaymentDto(orderEntity.getPaymentEntities().get(0)))
                .receiverName(orderEntity.getReceiverName())
                .receiverPhone(orderEntity.getReceiverPhone())
                .receiverAddress(orderEntity.getReceiverAddress())
                .receiverDetailAddress(orderEntity.getReceiverDetailAddress())
                .deliveryMemo(orderEntity.getDeliveryMemo())
                .createTime(orderEntity.getCreateTime())
                .build();
    }

}

package org.spring.backend.shop.order.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartOrderRequestDto {

    private List<Long> cartIds;

    private OrderDto order;
}

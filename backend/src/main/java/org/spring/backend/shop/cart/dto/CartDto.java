package org.spring.backend.shop.cart.dto;

import java.time.LocalDateTime;
import java.util.List;

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
public class CartDto {
  private Long id;

  private List<CartListDto> cartListDtos;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;

}

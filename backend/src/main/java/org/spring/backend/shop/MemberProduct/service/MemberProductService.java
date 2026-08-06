package org.spring.backend.shop.MemberProduct.service;

import java.time.LocalDate;
import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.shop.MemberProduct.dto.MemberProductDto;
import org.spring.backend.shop.product.entity.ProductEntity;

public interface MemberProductService {
  void create(MemberEntity memberEntity, ProductEntity productEntity, LocalDate startDate);

  public List<MemberProductDto> getActivePtProducts(String email);

  boolean checkSubscribe(String email);

  public List<MemberProductDto> getMyProducts(Long memberId);
}

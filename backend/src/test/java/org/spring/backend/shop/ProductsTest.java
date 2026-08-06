package org.spring.backend.shop;

import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.order.entity.OrderEntity;
import org.spring.backend.shop.order.entity.OrderItemEntity;
import org.spring.backend.shop.order.repository.OrderItemRepository;
import org.spring.backend.shop.order.repository.OrderRepository;
import org.spring.backend.shop.order.type.DeliveryStatus;
import org.spring.backend.shop.order.type.OrderStatus;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.repository.ProductRepository;
import org.spring.backend.shop.product.type.BillingType;
import org.spring.backend.shop.product.type.ProductStatus;
import org.spring.backend.shop.product.type.ProductType;
import org.spring.backend.trainer.entity.TrainerEntity;
import org.spring.backend.trainer.repository.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class ProductsTest {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private OrderItemRepository orderItemRepository;

  @Autowired
  private TrainerRepository trainerRepository;

  @Test
  void insert() {
    for (int i = 0; i < 2; i++) {


      ProductEntity productEntity = ProductEntity.builder()
              .productName("상품" + i)
              .description("상품" + i + "설명입니다.")
              .price(10000 + i)
              .productType(ProductType.GOODS)
              .billingType(BillingType.ONE_TIME)
              .productStatus(ProductStatus.ACTIVE)
              .category("다이어트")
              .build();

      productRepository.save(productEntity);
    }
  }

  @Test
  void insert2() {

    for (int i = 0; i < 2; i++) {

      ProductEntity productEntity = ProductEntity.builder()
          .productName("헬스장 이용권" + i)
          .description("헬스장 이용권" + i + "설명입니다.")
          .price(10000 + i)
          .productType(ProductType.GYM)
          .billingType(BillingType.ONE_TIME)
          .productStatus(ProductStatus.ACTIVE)
          .category("헬스장")
          .duration(30)
          .build();
      productRepository.save(productEntity);
    }
  }

  @Test
  void insert3() {

    for (int i = 0; i < 2; i++) {

      ProductEntity productEntity = ProductEntity.builder()
          .productName("PT이용권" + i)
          .description("PT이용권" + i + "설명입니다.")
          .price(10000 + i)
          .productType(ProductType.PT)
          .billingType(BillingType.ONE_TIME)
          .productStatus(ProductStatus.ACTIVE)
          .category("PT")
          .sessionCount(20)
          .build();

      productRepository.save(productEntity);
    }
  }

  @Test
  void insert4() {
      ProductEntity productEntity = ProductEntity.builder()
              .productName("FitMate Plus+")
              .description("FitMate Plus+ 설명입니다.")
              .price(10000)
              .productType(ProductType.PREMIUM)
              .billingType(BillingType.SUBSCRIPTION)
              .productStatus(ProductStatus.ACTIVE)
              .category("Premium")
              .build();

      productRepository.save(productEntity);
  }

  @Test
  void orderTest() {

    MemberEntity member = memberRepository.findById(1L)
        .orElseThrow();

    ProductEntity product1 = productRepository.findById(1L)
        .orElseThrow();

    ProductEntity product2 = productRepository.findById(2L)
        .orElseThrow();

    ProductEntity product3 = productRepository.findById(3L)
        .orElseThrow();

    // 주문 1
    OrderEntity order1 = OrderEntity.builder()
        .totalPrice(9900)
        .orderStatus(OrderStatus.SUCCESS)
        .deliveryStatus(DeliveryStatus.READY)
        .receiverName("김이박")
        .receiverPhone("010-1111-1111")
        .receiverAddress("서울 강남구")
        .deliveryMemo("문 앞")
        .memberEntity(member)
        .build();
    order1.setOrderItemEntities(new ArrayList<>());
    OrderItemEntity item1 = OrderItemEntity.builder()
        .price(9900)
        .quantity(1)
        .productName("프리미엄")
        .startDate(LocalDate.now())
        .productEntity(product1)
        .orderEntity(order1)
        .build();

    order1.getOrderItemEntities().add(item1);

    // 주문 2
    OrderEntity order2 = OrderEntity.builder()
        .totalPrice(300000)
        .orderStatus(OrderStatus.SUCCESS)
        .deliveryStatus(DeliveryStatus.SHIPPING)
        .receiverName("홍길동")
        .receiverPhone("010-2222-2222")
        .receiverAddress("서울 마포구")
        .memberEntity(member)
        .build();
    order2.setOrderItemEntities(new ArrayList<>());
    OrderItemEntity item2 = OrderItemEntity.builder()
        .price(300000)
        .quantity(1)
        .productName("PT 10회 이용권")
        .startDate(LocalDate.now())
        .productEntity(product2)
        .orderEntity(order2)
        .build();

    order2.getOrderItemEntities().add(item2);

    // 주문 3
    OrderEntity order3 = OrderEntity.builder()
        .totalPrice(50000)
        .orderStatus(OrderStatus.SUCCESS)
        .deliveryStatus(DeliveryStatus.COMPLETE)
        .receiverName("이영희")
        .receiverPhone("010-3333-3333")
        .receiverAddress("서울 송파구")
        .memberEntity(member)
        .build();
    order3.setOrderItemEntities(new ArrayList<>());
    OrderItemEntity item3 = OrderItemEntity.builder()
        .price(25000)
        .quantity(2)
        .productName("FitMate 운동복")
        .productEntity(product3)
        .orderEntity(order3)
        .build();

    order3.getOrderItemEntities().add(item3);

    orderRepository.save(order1);
    orderRepository.save(order2);
    orderRepository.save(order3);
    orderItemRepository.save(item1);
    orderItemRepository.save(item2);
    orderItemRepository.save(item3);

  }
  @Test
  void trainerTest() {

    MemberEntity member1 = memberRepository.findById(1L)
            .orElseThrow();

    MemberEntity member2 = memberRepository.findById(2L)
            .orElseThrow();

    MemberEntity member3 = memberRepository.findById(3L)
            .orElseThrow();

    TrainerEntity trainer1 = TrainerEntity.builder()
            .member(member1)
            .career("5년")
            .specialty("웨이트 트레이닝")
            .introduce("체형 교정과 근력 향상을 전문으로 지도합니다.")
            .certificate("생활스포츠지도사 2급")
            .profileImage("/upload/trainer/profile1.jpg")
            .build();

    TrainerEntity trainer2 = TrainerEntity.builder()
            .member(member2)
            .career("7년")
            .specialty("다이어트 · 체형관리")
            .introduce("개인 맞춤형 다이어트 프로그램을 제공합니다.")
            .certificate("NASM-CPT")
            .profileImage("/upload/trainer/profile2.jpg")
            .build();

    TrainerEntity trainer3 = TrainerEntity.builder()
            .member(member3)
            .career("10년")
            .specialty("재활 운동 · 기능성 트레이닝")
            .introduce("부상 예방과 재활 운동을 전문으로 지도합니다.")
            .certificate("재활운동전문가")
            .profileImage("/upload/trainer/profile3.jpg")
            .build();

    trainerRepository.save(trainer1);
    trainerRepository.save(trainer2);
    trainerRepository.save(trainer3);
  }
}

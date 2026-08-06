package org.spring.backend.shop.order.service.serviceImpl;

import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.MemberProduct.service.MemberProductService;
import org.spring.backend.shop.cart.entity.CartListEntity;
import org.spring.backend.shop.cart.repository.CartListRepository;
import org.spring.backend.shop.order.dto.OrderDto;
import org.spring.backend.shop.order.dto.OrderItemDto;
import org.spring.backend.shop.order.dto.SubscriptionOrderRequestDto;
import org.spring.backend.shop.order.entity.OrderEntity;
import org.spring.backend.shop.order.entity.OrderItemEntity;
import org.spring.backend.shop.order.repository.OrderItemRepository;
import org.spring.backend.shop.order.repository.OrderRepository;
import org.spring.backend.shop.order.service.OrderService;
import org.spring.backend.shop.order.type.DeliveryStatus;
import org.spring.backend.shop.order.type.OrderStatus;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;
        private final MemberRepository memberRepository;
        private final ProductRepository productRepository;
        private final CartListRepository cartListRepository;
        private final MemberProductService memberProductService;

        @Override
        public Long insertDirectOrder(Long memberId, OrderDto orderDto) {

                System.out.println(orderDto);

                for (OrderItemDto item : orderDto.getOrderItemDtos()) {

                }
                // 회원 조회
                MemberEntity memberEntity = memberRepository.findById(memberId)

                                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

                // 주문 생성
                OrderEntity orderEntity = OrderEntity.builder()
                                .totalPrice(0)
                                .orderStatus(OrderStatus.PENDING)
                                .deliveryStatus(DeliveryStatus.READY)
                                .receiverName(orderDto.getReceiverName())
                                .receiverPhone(orderDto.getReceiverPhone())
                                .receiverAddress(orderDto.getReceiverAddress())
                                .receiverDetailAddress(orderDto.getReceiverDetailAddress())
                                .deliveryMemo(orderDto.getDeliveryMemo())
                                .memberEntity(memberEntity)
                                .build();

                orderRepository.save(orderEntity);

                int totalPrice = 0;

                // 주문 상품 저장
                for (OrderItemDto itemDto : orderDto.getOrderItemDtos()) {

                        ProductEntity productEntity = productRepository.findById(itemDto.getProductId())
                                        .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

                        OrderItemEntity orderItemEntity = OrderItemEntity.builder()
                                        .orderEntity(orderEntity)
                                        .productEntity(productEntity)
                                        .productName(productEntity.getProductName())
                                        .price(productEntity.getPrice())
                                        .quantity(itemDto.getQuantity())
                                        .build();

                        orderItemRepository.save(orderItemEntity);

                        totalPrice += productEntity.getPrice() * itemDto.getQuantity();
                }

                orderEntity.setTotalPrice(totalPrice);

                return orderEntity.getId();

        }

        @Override
        public Long insertCartOrder(Long memberId, List<Long> cartListIds, OrderDto orderDto) {

                // 회원 조회
                MemberEntity memberEntity = memberRepository.findById(memberId)
                                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

                // 주문 생성
                OrderEntity orderEntity = OrderEntity.builder()
                                .totalPrice(0)
                                .orderStatus(OrderStatus.PENDING)
                                .deliveryStatus(DeliveryStatus.READY)
                                .receiverName(orderDto.getReceiverName())
                                .receiverPhone(orderDto.getReceiverPhone())
                                .receiverAddress(orderDto.getReceiverAddress())
                                .receiverDetailAddress(orderDto.getReceiverDetailAddress()) 
                                .deliveryMemo(orderDto.getDeliveryMemo())
                                .memberEntity(memberEntity)
                                .build();

                orderRepository.save(orderEntity);

                int totalPrice = 0;

                for (Long cartListId : cartListIds) {

                        CartListEntity cartListEntity = cartListRepository.findById(cartListId)
                                        .orElseThrow(() -> new IllegalArgumentException("장바구니 상품이 존재하지 않습니다."));

                        ProductEntity productEntity = cartListEntity.getProductEntity();

                        OrderItemEntity orderItemEntity = OrderItemEntity.builder()
                                        .orderEntity(orderEntity)
                                        .productEntity(productEntity)
                                        .productName(productEntity.getProductName())
                                        .price(productEntity.getPrice())
                                        .quantity(cartListEntity.getQuantity())
                                        .build();

                        orderItemRepository.save(orderItemEntity);

                        totalPrice += productEntity.getPrice() * cartListEntity.getQuantity();
                }

                orderEntity.setTotalPrice(totalPrice);

                return orderEntity.getId();
        }

        @Override
        public List<OrderDto> orderList(Long memberId) {

                return orderRepository.findGoodsOrdersByMemberId(memberId)
                                .stream()
                                .map(OrderDto::toOrderDto)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public OrderDto orderDetail(Long orderId) {

                OrderEntity orderEntity = orderRepository.findDetailById(orderId)
                                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

                return OrderDto.toOrderDto(orderEntity);
        }

        @Override
        public void cancelOrder(Long orderId) {
                OrderEntity orderEntity = orderRepository.findById(orderId)
                                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

                orderEntity.setOrderStatus(OrderStatus.CANCELED);
        }

        @Override
        public void updateOrderStatus(Long orderId, DeliveryStatus deliveryStatus) {

                OrderEntity orderEntity = orderRepository.findById(orderId)
                                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

                orderEntity.setDeliveryStatus(deliveryStatus);
        }

        @Override
        @Transactional
        public Long insertSubscriptionOrder(Long memberId, SubscriptionOrderRequestDto request) {

                MemberEntity member = memberRepository.findById(memberId)
                                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

                ProductEntity product = productRepository.findById(request.getProductId())
                                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

                OrderEntity order = OrderEntity.builder()
                                .memberEntity(member)
                                .totalPrice(product.getPrice())
                                .orderStatus(OrderStatus.SUCCESS)
                                .deliveryStatus(DeliveryStatus.NONE)
                                .receiverName(null)
                                .receiverPhone(null)
                                .receiverAddress(null)
                                .build();

                orderRepository.save(order);

                OrderItemEntity item = OrderItemEntity.builder()
                                .orderEntity(order)
                                .productEntity(product)
                                .productName(product.getProductName())
                                .quantity(1)
                                .price(product.getPrice())
                                .startDate(request.getStartDate())
                                .build();

                orderItemRepository.save(item);
                // 결제 완료와 동시에 이용권 발급!
                memberProductService.create(member, product, request.getStartDate());

                return order.getId();
        }

        @Override
        public Page<OrderDto> adminOrderList(Pageable pageable) {

                return orderRepository.findAll(pageable)
                                .map(OrderDto::toOrderDto);
        }
}

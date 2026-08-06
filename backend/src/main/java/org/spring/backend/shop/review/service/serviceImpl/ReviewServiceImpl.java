package org.spring.backend.shop.review.service.serviceImpl;

import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.order.entity.OrderEntity;
import org.spring.backend.shop.order.entity.OrderItemEntity;
import org.spring.backend.shop.order.repository.OrderItemRepository;
import org.spring.backend.shop.order.repository.OrderRepository;
import org.spring.backend.shop.order.type.OrderStatus;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.repository.ProductRepository;
import org.spring.backend.shop.review.dto.ReviewDto;
import org.spring.backend.shop.review.entity.ReviewEntity;
import org.spring.backend.shop.review.repository.ReviewRepository;
import org.spring.backend.shop.review.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final MemberRepository memberRepository;
  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  @Override
  public void insertReview(Long memberId, ReviewDto reviewDto, Long orderItemId) {
    MemberEntity member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    OrderItemEntity orderItem = orderItemRepository.findById(orderItemId)
        .orElseThrow(() -> new IllegalArgumentException("주문 상품이 없습니다."));

    // 본인 주문인지 검증
    if (!orderItem.getOrderEntity().getMemberEntity().getId().equals(memberId)) {
      throw new IllegalArgumentException("본인이 구매한 상품만 리뷰작성이 가능합니다.");
    }
    if (reviewRepository.existsByOrderItemEntity_Id(orderItemId)) {
      throw new IllegalArgumentException("이미 리뷰를 작성한 상품입니다.");
    }
    if (orderItem.getOrderEntity().getOrderStatus() != OrderStatus.SUCCESS) {
      throw new IllegalArgumentException("구매 완료된 상품만 리뷰를 작성할 수 있습니다.");
    }
    ReviewEntity review = ReviewEntity.builder()
        .memberEntity(member)
        .orderItemEntity(orderItem)
        .productEntity(orderItem.getProductEntity()) // 필요하면 자동 연결
        .content(reviewDto.getContent())
        .rating(reviewDto.getRating())
        .build();

    reviewRepository.save(review);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReviewDto> reviewListByProduct(Long productId) {
    return reviewRepository.findByProductEntity_Id(productId)
        .stream()
        .map(ReviewDto::toReviewDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ReviewDto reviewDetail(Long reviewId) {

    ReviewEntity review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

    return ReviewDto.toReviewDto(review);
  }

  @Override
  public void updateReview(Long reviewId, ReviewDto reviewDto) {
    ReviewEntity review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

    review.setContent(reviewDto.getContent());
    review.setRating(reviewDto.getRating());
  }

  @Override
  public void deleteReview(Long reviewId) {

    ReviewEntity review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

    reviewRepository.delete(review);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReviewDto> myReviewList(Long memberId) {
    return reviewRepository.findByMemberEntity_Id(memberId)
        .stream()
        .map(ReviewDto::toReviewDto)
        .toList();
  }
}

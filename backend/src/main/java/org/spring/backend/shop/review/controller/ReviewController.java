package org.spring.backend.shop.review.controller;

import java.util.List;

import org.spring.backend.shop.review.dto.ReviewDto;
import org.spring.backend.shop.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
  private final ReviewService reviewService;

  // 리뷰 작성
  @PostMapping("/{memberId}/{orderId}")
  public ResponseEntity<Void> insertReview(
      @PathVariable Long memberId,
      @PathVariable Long orderItemId,
      @RequestBody ReviewDto reviewDto) {

    reviewService.insertReview(memberId, reviewDto, orderItemId);
    return ResponseEntity.ok().build();
  }

  // 상품별 리뷰 조회
  @GetMapping("/product/{productId}")
  public ResponseEntity<List<ReviewDto>> reviewListByProduct(
      @PathVariable Long productId) {

    return ResponseEntity.ok(reviewService.reviewListByProduct(productId));
  }

  // 리뷰 상세 조회
  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> reviewDetail(
      @PathVariable Long reviewId) {

    return ResponseEntity.ok(reviewService.reviewDetail(reviewId));
  }

  // 리뷰 수정
  @PutMapping("/{reviewId}")
  public ResponseEntity<Void> updateReview(
      @PathVariable Long reviewId,
      @RequestBody ReviewDto reviewDto) {

    reviewService.updateReview(reviewId, reviewDto);
    return ResponseEntity.ok().build();
  }

  // 리뷰 삭제
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReview(
      @PathVariable Long reviewId) {

    reviewService.deleteReview(reviewId);
    return ResponseEntity.ok().build();
  }

  // 내가 작성한 리뷰 조회
  @GetMapping("/member/{memberId}")
  public ResponseEntity<List<ReviewDto>> myReviewList(
      @PathVariable Long memberId) {

    return ResponseEntity.ok(reviewService.myReviewList(memberId));
  }
}

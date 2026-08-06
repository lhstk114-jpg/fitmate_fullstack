package org.spring.backend.shop.review.service;

import java.util.List;

import org.spring.backend.shop.review.dto.ReviewDto;

public interface ReviewService {
      // 리뷰 작성
    void insertReview(Long memberId, ReviewDto reviewDto, Long orderItemId);

    // 상품별 리뷰 조회
    List<ReviewDto> reviewListByProduct(Long productId);

    // 리뷰 상세 조회
    ReviewDto reviewDetail(Long reviewId);

    // 리뷰 수정
    void updateReview(Long reviewId, ReviewDto reviewDto);

    // 리뷰 삭제
    void deleteReview(Long reviewId);

    // 내가 쓴 리뷰 조회
    List<ReviewDto> myReviewList(Long memberId);
}

package org.spring.backend.shop.payment.service;

import java.util.List;

import org.spring.backend.shop.payment.dto.PaymentDto;
import org.spring.backend.shop.payment.dto.PaymentSuccessDto;
import org.spring.backend.shop.payment.entity.PaymentEntity;

public interface PaymentService {

  void paymentInsert(PaymentDto paymentDto);

  // 마이페이지 결제조회
  List<PaymentDto> paymentListFn(Long memberId);

  // 관리자용 결제목록 조회
  List<PaymentDto> paymentAllList();

  // 단일 결제상세 조회
  PaymentDto findById(Long id);

  // 최종 결제 승인 프로세스 (변경 감지 적용)
  PaymentSuccessDto paymentApproval(String pgToken, Long paymentId);

  // 카카오 결제 승인 요청 (v1/payment/approve)
  void paymentApproveKakao(PaymentEntity paymentEntity);

  // DB의 JSON 목록 조회 유틸
  String getJsonDb();

  // 카카오 결제 준비 요청 (v1/payment/ready)
  String pgRequest(String pg, Long orderId);

  // 일반결제 성공
  public PaymentSuccessDto normalPayment(Long orderId);

}

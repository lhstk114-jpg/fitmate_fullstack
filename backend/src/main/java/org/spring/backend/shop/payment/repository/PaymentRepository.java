package org.spring.backend.shop.payment.repository;

import java.util.List;

import org.spring.backend.shop.payment.entity.PaymentEntity;
import org.spring.backend.shop.payment.type.PaymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

  // 마이페이지
  @Query("""
          SELECT p
          FROM PaymentEntity p
          WHERE p.orderEntity.memberEntity.id = :memberId
             OR p.subscriptionEntity.memberEntity.id = :memberId""")
  @EntityGraph(attributePaths = {
      "orderEntity",
      "subscriptionEntity"})
  List<PaymentEntity> findPaymentListByMemberId(@Param("memberId") Long memberId);

  // 관리자용 전체 조회
  @EntityGraph(attributePaths = { "orderEntity", "subscriptionEntity" })
  List<PaymentEntity> findAll();

  // 변경 감지(Dirty Checking) 대신 벌크 연산이 꼭 필요한 때를 위한 안전장치 추가
  @Modifying(clearAutomatically = true)
  @Query(value = """
                UPDATE payment_tb
                SET pg_token = :pgToken
                WHERE payment_id = :paymentId
                """, nativeQuery = true)
  void updatePgToken(@Param("paymentId") Long paymentId, @Param("pgToken") String pgToken);

  @Modifying(clearAutomatically = true)
  @Query(value = """
                UPDATE payment_tb
                SET payment_status = :paymentStatus
                WHERE payment_id = :paymentId
                """, nativeQuery = true)
  void updateStatus(@Param("paymentId") Long paymentId, @Param("paymentStatus") PaymentStatus paymentStatus);

}

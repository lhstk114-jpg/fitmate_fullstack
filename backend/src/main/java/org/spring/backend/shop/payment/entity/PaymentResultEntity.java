package org.spring.backend.shop.payment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "payment_result_tb")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentResultId;

    private Long paymentId; // 연산 및 조인을 위해 Long 권장
    private Long memberId;
    private String productName;
    private Long productPrice;

}

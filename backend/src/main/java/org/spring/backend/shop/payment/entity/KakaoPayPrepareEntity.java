package org.spring.backend.shop.payment.entity;

import org.spring.backend.shop.payment.dto.KakaoPayPrepareDto;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KakaoPayPrepareEntity {

    private String tid; // 고유 거래번호
    private String tmsResult; //TMS처리결과
    private String nextRedirectAppUrl; //앱 결제용 URL
    private String nextRedirectMobileUrl; //모바일용 URL
    private String nextRedirectPcUrl; // PC용 URL
    private String androidAppScheme; // 안드로이드 앱용
    private String iosAppScheme; // iOS앱용
    private String createdAt; // 결제 준비 생성 시간

    public static KakaoPayPrepareEntity toEntity(KakaoPayPrepareDto kakaoPayPrepareDto) {
        return KakaoPayPrepareEntity.builder()
                .tid(kakaoPayPrepareDto.getTid())
                .tmsResult(kakaoPayPrepareDto.getTms_result())
                .nextRedirectAppUrl(kakaoPayPrepareDto.getNext_redirect_app_url())
                .nextRedirectMobileUrl(kakaoPayPrepareDto.getNext_redirect_mobile_url())
                .nextRedirectPcUrl(kakaoPayPrepareDto.getNext_redirect_pc_url())
                .androidAppScheme(kakaoPayPrepareDto.getAndroid_app_scheme())
                .iosAppScheme(kakaoPayPrepareDto.getIos_app_scheme())
                .createdAt(kakaoPayPrepareDto.getCreated_at())
                .build();

    }


}
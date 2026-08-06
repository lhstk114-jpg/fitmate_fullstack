package org.spring.backend.main.dto;

import lombok.*;
import org.spring.backend.admin.popup.dto.PopupDto;
import org.spring.backend.community.dto.CommunityDto;
import org.spring.backend.shop.product.dto.ProductDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MainResponseDto {
    //공지사항 리스트
    List<CommunityDto> noticeList;
    //공지사항 제외 리스트
    List<CommunityDto> communityList;
    //상품리스트
    List<ProductDto> productList;
    //팝업리스트
    List<PopupDto> popupList;

}

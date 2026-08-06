package org.spring.backend.admin.popup.service;

import org.spring.backend.admin.popup.dto.PopupDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface PopupService {

    // 팝업 등록
    void insertPopup(PopupDto popupDto) throws IOException;
    // 관리자 팝업 전체 목록 조회
//    List<PopupDto> popupList();

    // 관리자 팝업 전체 목록 조회
//    @Transactional(readOnly = true)
//    @Override
//    public List<PopupDto> popupList() {
//
//        return popupRepository
//                .findAll()
//                .stream()
//                .map(this::convertPopupDto)
//                .toList();
//    }
    Page<PopupDto> popupList(Pageable pageable, String subject, String search);

    //팝업 상세 조회
    PopupDto popupDetail(Long id);
    // 팝업 수정
    void updatePopup(PopupDto popupDto) throws IOException;
    // 팝업 삭제
    void deletePopup(Long id) throws IOException;

    //활성화된 popup 조회
    List<PopupDto> getActivePopupList();
}

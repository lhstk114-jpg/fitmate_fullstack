package org.spring.backend.admin.popup.service.impl;

import lombok.RequiredArgsConstructor;
import org.spring.backend.admin.popup.dto.PopupDto;
import org.spring.backend.admin.popup.entity.PopupEntity;
import org.spring.backend.admin.popup.repository.PopupRepository;
import org.spring.backend.admin.popup.service.PopupService;
import org.spring.backend.file.enumtype.TableType;
import org.spring.backend.file.entity.FileEntity;
import org.spring.backend.file.handler.FileHandler;
import org.spring.backend.file.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PopupServiceImpl implements PopupService {
    private final PopupRepository popupRepository;
    private final FileRepository fileRepository;
    private final FileHandler fileHandler;

    // 팝업 이미지 저장 경로
    @Value("${img.path.popup}")
    private String popupPath;

    // PopupEntity와 해당 팝업의 FileEntity를 함께 조회
    // PopupDto로 변환하는 공통 메서드
    private PopupDto convertPopupDto(PopupEntity popupEntity) {

        FileEntity fileEntity =
                fileRepository
                        .findByPopupEntity(popupEntity)
                        .orElse(null);

        return PopupDto.toPopupDto(
                popupEntity,
                fileEntity
        );
    }

    // 팝업 등록
    @Transactional
    @Override
    public void insertPopup(PopupDto popupDto) throws IOException {

        PopupEntity popupEntity =
                PopupEntity.toInsertPopupEntity(popupDto);

        //PopupEntity를 먼저 저장합니다.
        PopupEntity savedPopup =
                popupRepository.save(popupEntity);

        // 실제 파일이 존재할 때만 파일 저장
        if (popupDto.getAttachFile() != null
                && !popupDto.getAttachFile().isEmpty()) {

            fileHandler.insertFile(
                    popupPath,
                    TableType.POPUP,
                    savedPopup.getId(),
                    popupDto.getAttachFile()
            );
        }
    }

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
    @Transactional(readOnly = true)
    @Override
    public Page<PopupDto> popupList(Pageable pageable, String subject, String search) {
        if(subject==null||subject.isBlank()||search==null||search.isBlank()){
            return popupRepository.findAll(pageable).map(this::convertPopupDto);
        }
        //멤버리스트 검색필터링기능
        //검색 변수
        Page<PopupEntity> popupEntities;
        switch (subject){
            case "endDate"
                    ->popupEntities = popupRepository.findByEndDateContaining(pageable, search);
            case "startDate"
                    ->  popupEntities =popupRepository.findByStartDateContaining(pageable, search);
            case "title"
                    -> popupEntities = popupRepository.findByTitleContaining(pageable, search);
            case "active" -> {
                Boolean active = Boolean.parseBoolean(search);
                popupEntities =popupRepository.findByActive(active,pageable);}

            case "sortOrder" -> {
                try {
                    Integer sortOrder = Integer.parseInt(search);
                    popupEntities = popupRepository.findBySortOrder(sortOrder,pageable);
                } catch (NumberFormatException e) {
                    // sortOrder 검색값이 숫자가 아니면 빈 결과 반환
                    popupEntities = Page.empty(pageable);
                }
            }
            default -> popupEntities = popupRepository.findAll(pageable);
        }
        return popupEntities.map(this::convertPopupDto);
    }

    //팝업 상세 조회
    @Transactional(readOnly = true)
    @Override
    public PopupDto popupDetail(Long id) {

        PopupEntity popupEntity =
                popupRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException("해당 팝업이 없습니다. id=" + id));

        return convertPopupDto(popupEntity);
    }

    // 팝업 수정
    @Transactional
    @Override
    public void updatePopup(PopupDto popupDto) throws IOException {

        PopupEntity popupEntity =
                popupRepository
                        .findById(popupDto.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("해당 팝업이 없습니다. id=" + popupDto.getId()));

        //팝업 정보 수정
        popupEntity.toUpdatePopup(popupDto);

        // 새 이미지가 선택된 경우에만 파일 교체
        if (popupDto.getAttachFile() != null
                && !popupDto.getAttachFile().isEmpty()) {

            fileHandler.insertFile(
                    popupPath,
                    TableType.POPUP,
                    popupEntity.getId(),
                    popupDto.getAttachFile()
            );
        }
    }

    // 팝업 삭제
    // 실제 파일과 FileEntity를 먼저 후 PopupEntity 삭제
    @Transactional
    @Override
    public void deletePopup(Long id) throws IOException {
//        System.out.println("백엔드 팝업 삭제 실행");
        PopupEntity popupEntity =
                popupRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException("해당 팝업이 없습니다. id=" + id));
        //실제 저장 파일과 FileEntity 삭제
        Optional<FileEntity> fileEntity=fileRepository.findByPopupEntity(popupEntity);
        if(fileEntity.isPresent()){

            fileHandler.deleteFile(
                    popupPath,
                    TableType.POPUP,
                    id);
        }

        // 팝업 정보 삭제
        popupRepository.delete(popupEntity);
    }

    //활성화된 popup 조회
    @Override
    @Transactional(readOnly = true)
    public List<PopupDto> getActivePopupList() {

        LocalDateTime now = LocalDateTime.now();

        return popupRepository
                .findActivePopup(now)
                .stream()
                .map(this::convertPopupDto)
                .toList();
    }
}

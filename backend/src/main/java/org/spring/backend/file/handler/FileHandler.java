package org.spring.backend.file.handler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.spring.backend.calendar.entity.PersonalScheduleEntity;
import org.spring.backend.calendar.repository.PersonalScheduleRepository;
import org.spring.backend.file.enumtype.TableType;
import org.spring.backend.community.entity.CommunityEntity;
import org.spring.backend.community.repository.CommunityRepository;
import org.spring.backend.file.entity.FileEntity;
import org.spring.backend.file.repository.FileRepository;
import org.spring.backend.admin.popup.entity.PopupEntity;
import org.spring.backend.admin.popup.repository.PopupRepository;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.repository.ProductRepository;
import org.spring.backend.shop.product.type.ImageType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
// 공통 파일 유틸리티(파일삭제, 파일생성)
public class FileHandler {
    private final FileRepository fileRepository;
    private final MemberRepository memberRepository;
    private final CommunityRepository communityRepository;
    private final ProductRepository productRepository;
    private final PopupRepository popupRepository;
    private final PersonalScheduleRepository personalScheduleRepository;

    // 파일삭제
    @Transactional
    // 경로, 테이블타입, 해당하는 파일의 아이디
    public void deleteFile(String filePath, TableType tableType, Long id)
            throws IOException {
        // 비어있는 파일엔티티 생성
        Optional<FileEntity> optionalFileEntity = Optional.empty();
        // URI기반으로 경로 변환
        Path baseDirPath;
        try {
            baseDirPath = Paths.get(URI.create(filePath));
        } catch (Exception e) {
            baseDirPath = Paths.get(filePath);
        }
        // 테이블타입별로 나눔(타입추가시에 common/TableType enum 수정
        switch (tableType) {
            // 해당하는 타입별로 memberEntity기준(1:N매칭한거)으로 찾아냄
            case MEMBER -> optionalFileEntity = fileRepository.findByMemberEntity(
                    memberRepository.findById(id).orElseThrow(() -> new NoSuchElementException("존재하지않는 멤버입니다.")));
            case COMMUNITY -> optionalFileEntity = fileRepository.findByCommunityEntity(
                    communityRepository.findById(id).orElseThrow(() -> new NoSuchElementException("존재하지않는 게시글입니다.")));
            case POPUP -> optionalFileEntity = fileRepository.findByPopupEntity(
                    popupRepository.findById(id).orElseThrow(() -> new NoSuchElementException("존재하지않는 팝업입니다.")));
            case SCHEDULE -> optionalFileEntity = fileRepository.findByPersonalScheduleEntity(
                    personalScheduleRepository.findById(id).orElseThrow(() -> new NoSuchElementException("존재하지않는 스케줄입니다.")));
            default -> throw new IllegalArgumentException("tableType이 존재하지 않습니다.");
        }

        // 파일DB에 저장되어있는게 없으면 예외처리
        if (optionalFileEntity.isEmpty()) {
            // System.out.println("파일엔티티가 존재하지 않습니다.");
            return;
        }
        try {
            // 파일이 최종저장되어있는 절대 경로 생성
            Path targetFilePath = baseDirPath.resolve(optionalFileEntity.get().getNewFileName());
            // 파일로 변경
            File deleteFile = targetFilePath.toFile();
            // 해당하는 파일이 있을 경우 삭제
            if (deleteFile.exists())
                deleteFile.delete();
            // 파일DB에서도 제거
            fileRepository.delete(optionalFileEntity.get());
        } catch (Exception e) {
            // 파일제거 중 오류발생시 예외처리
            System.out.println("파일 삭제 중 에러 : " + e.getMessage());
            throw new IOException("파일삭제에 실패하였습니다.");
        }
    }

    // 단일파일 저장
    // 파일경로, 테이블타입, 저장할 테이블 요소의 아이디, 파일
    public void insertFile(String filePath, TableType tableType, Long id, MultipartFile file)
            throws IOException {
        if (file == null || file.isEmpty()) {
            return; // 파일이 없으면 진행하지 않음
        }
        // 우선 비어있는 파일엔티티 생성
        // 파일이 있는지 비교용
        Optional<FileEntity> optionalFileEntity = Optional.empty();
        // 파일 엔티티 생성을 위한 비교용
        FileEntity fileEntity = null;
        // 파일의 원본이름 저장
        String oldFileName = file.getOriginalFilename();
        // 파일의 새로운이름 저장(생성된UUID_원본파일이름)
        String newFileName = UUID.randomUUID() + "_" + oldFileName;
        // URI기반으로 경로 변환
        Path baseDirPath;
        try {
            baseDirPath = Paths.get(URI.create(filePath));
        } catch (Exception e) {
            baseDirPath = Paths.get(filePath);
        }
        // 테이블 타입별로 나눠서 엔티티 생성
        switch (tableType) {
            // 각자의 테이블에 데이터가 있는지 확인
            // 이후 파일엔티티에 데이터 확인 후 저장
            case MEMBER -> {
                // 기존 멤버 데이터 확인
                MemberEntity memberEntity = memberRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("회원정보가 없습니다."));
                // 새 파일 업로드 여부 확인용 파일엔티티
                optionalFileEntity = fileRepository.findByMemberEntity(memberEntity);
                // 새 파일엔티티 생성
                fileEntity = FileEntity.builder()
                        .oldFileName(oldFileName)
                        .newFileName(newFileName)
                        .tableType(tableType)
                        .memberEntity(memberEntity)
                        .build();
            }
            case COMMUNITY -> {
                CommunityEntity communityEntity = communityRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
                optionalFileEntity = fileRepository.findByCommunityEntity(communityEntity);
                // 새 파일엔티티 생성
                fileEntity = FileEntity.builder()
                        .oldFileName(oldFileName)
                        .newFileName(newFileName)
                        .tableType(tableType)
                        .communityEntity(communityEntity)
                        .build();
            }
            case PRODUCT -> {
                ProductEntity productEntity = productRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));
                // 새 파일엔티티 생성
                fileEntity = FileEntity.builder()
                        .oldFileName(oldFileName)
                        .newFileName(newFileName)
                        .tableType(tableType)
                        .productEntity(productEntity)
                        .build();
            }
            case POPUP -> {
                PopupEntity popupEntity = popupRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("팝업이 없습니다."));
                optionalFileEntity = fileRepository.findByPopupEntity(popupEntity);
                // 새 파일엔티티 생성
                fileEntity = FileEntity.builder()
                        .oldFileName(oldFileName)
                        .newFileName(newFileName)
                        .tableType(tableType)
                        .popupEntity(popupEntity)
                        .build();
            }
            case SCHEDULE -> {
                PersonalScheduleEntity personalScheduleEntity = personalScheduleRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("스케줄이 없습니다."));
                optionalFileEntity = fileRepository.findByPersonalScheduleEntity(personalScheduleEntity);
                // 새 파일엔티티 생성
                fileEntity = FileEntity.builder()
                        .oldFileName(oldFileName)
                        .newFileName(newFileName)
                        .tableType(tableType)
                        .personalScheduleEntity(personalScheduleEntity)
                        .build();
            }
            default -> throw new IllegalArgumentException("tableType이 존재하지 않습니다.");
        }
        // 해당하는 파일이 존재할 시 제거
        if (optionalFileEntity.isPresent()) {
            try {
                // 파일이 최종저장되어있는 절대 경로 생성
                Path targetFilePath = baseDirPath.resolve(optionalFileEntity.get().getNewFileName());
                // 파일로 변경
                File deleteFile = targetFilePath.toFile();
                // 해당하는 파일이 있을 경우 삭제
                if (deleteFile.exists())
                    deleteFile.delete();
                // 파일DB에서도 제거
                fileRepository.delete(optionalFileEntity.get());
            } catch (Exception e) {
                // 파일제거 중 오류발생시 예외처리
                System.out.println("파일 삭제 중 에러 : " + e.getMessage());
                throw new IOException("파일삭제에 실패하였습니다.");
            }
        }
        // 새로운 파일 저장
        try {
            // 파일이 최종저장되어있는 절대 경로 생성
            Path targetPath = baseDirPath.resolve(newFileName);
            // 만약 폴더가 없을때는 생성
            if (!Files.exists(targetPath.getParent()))
                Files.createDirectories(targetPath.getParent());
            // 파일 저장
            file.transferTo(targetPath.toFile());
            // 파일DB에 정보 저장
            fileRepository.save(fileEntity);
        } catch (Exception e) {
            System.out.println("파일 저장 중 에러 발생: " + e.getMessage());
            throw new IOException("파일저장에 실패하였습니다.");
        }
    }

    // 단일파일 저장(이미지타입, 정렬 순서 추가)
    // 파일경로, 테이블타입, 저장할 테이블 요소의 아이디, 파일, 이미지타입, 이미지 순서
    public void insertFile(String filePath, TableType tableType, Long id, MultipartFile file, ImageType imageType,
            int sortOrder)
            throws IOException {
        if (file == null || file.isEmpty()) {
            return; // 파일이 없으면 진행하지 않음
        }
        // 우선 비어있는 파일엔티티 생성
        // 파일이 있는지 비교용
        Optional<FileEntity> optionalFileEntity = Optional.empty();
        // 파일 엔티티 생성을 위한 비교용
        FileEntity fileEntity = null;
        // 파일의 원본이름 저장
        String oldFileName = file.getOriginalFilename();
        // 파일의 새로운이름 저장(생성된UUID_원본파일이름)
        String newFileName = UUID.randomUUID() + "_" + oldFileName;
        // URI기반으로 경로 변환
        Path baseDirPath;
        try {
            baseDirPath = Paths.get(URI.create(filePath));
        } catch (Exception e) {
            baseDirPath = Paths.get(filePath);
        }

        // 테이블 타입별로 나눠서 엔티티 생성
        switch (tableType) {
            // 각자의 테이블에 데이터가 있는지 확인
            // 이후 파일엔티티에 데이터 확인 후 저장
            case MEMBER -> {
                // 기존 멤버 데이터 확인
                MemberEntity memberEntity = memberRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("회원정보가 없습니다."));
                // 새 파일 업로드 여부 확인용 파일엔티티
                optionalFileEntity = fileRepository.findByMemberEntity(memberEntity);
                // 새 파일엔티티 생성
                fileEntity = FileEntity.builder()
                        .oldFileName(oldFileName)
                        .newFileName(newFileName)
                        .tableType(tableType)
                        .imageType(imageType)
                        .sortOrder(sortOrder)
                        .memberEntity(memberEntity)
                        .build();
            }
            case COMMUNITY -> {
                CommunityEntity communityEntity = communityRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
                optionalFileEntity = fileRepository.findByCommunityEntity(communityEntity);
                // 새 파일엔티티 생성
                fileEntity = FileEntity.builder()
                        .oldFileName(oldFileName)
                        .newFileName(newFileName)
                        .tableType(tableType)
                        .imageType(imageType)
                        .sortOrder(sortOrder)
                        .communityEntity(communityEntity)
                        .build();
            }
            case PRODUCT -> {
                ProductEntity productEntity = productRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));

                fileEntity = FileEntity.builder()
                        .oldFileName(oldFileName)
                        .newFileName(newFileName)
                        .tableType(tableType)
                        .imageType(imageType)
                        .sortOrder(sortOrder)
                        .productEntity(productEntity)
                        .build();
            }
            case POPUP -> {
                PopupEntity popupEntity = popupRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("팝업이 없습니다."));
                optionalFileEntity = fileRepository.findByPopupEntity(popupEntity);
                // 새 파일엔티티 생성
                fileEntity = FileEntity.builder()
                        .oldFileName(oldFileName)
                        .newFileName(newFileName)
                        .tableType(tableType)
                        .imageType(imageType)
                        .sortOrder(sortOrder)
                        .popupEntity(popupEntity)
                        .build();
            }
            case  SCHEDULE-> {
                PersonalScheduleEntity personalScheduleEntity = personalScheduleRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("스케줄이 없습니다."));
                optionalFileEntity = fileRepository.findByPersonalScheduleEntity(personalScheduleEntity);
                // 새 파일엔티티 생성
                fileEntity = FileEntity.builder()
                        .oldFileName(oldFileName)
                        .newFileName(newFileName)
                        .tableType(tableType)
                        .imageType(imageType)
                        .sortOrder(sortOrder)
                        .personalScheduleEntity(personalScheduleEntity)
                        .build();
            }
                   default -> throw new IllegalArgumentException("tableType이 존재하지 않습니다.");
        }
        // 해당하는 파일이 존재할 시 제거
        if (optionalFileEntity.isPresent()) {
            try {
                // 파일이 최종저장되어있는 절대 경로 생성
                Path targetFilePath = baseDirPath.resolve(optionalFileEntity.get().getNewFileName());
                // 파일로 변경
                File deleteFile = targetFilePath.toFile();
                // 해당하는 파일이 있을 경우 삭제
                if (deleteFile.exists())
                    deleteFile.delete();
                // 파일DB에서도 제거
                fileRepository.delete(optionalFileEntity.get());
            } catch (Exception e) {
                // 파일제거 중 오류발생시 예외처리
                System.out.println("파일 삭제 중 에러 : " + e.getMessage());
                throw new IOException("파일삭제에 실패하였습니다.");
            }
        }
        try {
            // 새로운 파일 저장
            // 파일이 최종저장되어있는 절대 경로 생성
            Path targetPath = baseDirPath.resolve(newFileName);
            // 만약 폴더가 없을때는 생성
            if (!Files.exists(targetPath.getParent()))
                Files.createDirectories(targetPath.getParent());
            // 파일 저장
            file.transferTo(targetPath.toFile());
            // 파일DB에 정보 저장
            fileRepository.save(fileEntity);
        } catch (Exception e) {
            System.out.println("파일 저장 중 에러 발생: " + e.getMessage());
            throw new IOException("파일저장에 실패하였습니다.");
        }
    }

    // 파일 여러개 저장
    public void insertMultipleFile(String filePath, TableType tableType, Long id, MultipartFile file)
            throws IOException {

    }

    // 상품 삭제 시 이미지 전체 삭제
    @Transactional
    public void deleteProductFiles(String filePath, Long productId) throws IOException {
        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품이 없습니다."));

        List<FileEntity> fileList = fileRepository.findByProductEntityOrderBySortOrderAsc(productEntity);

        if (fileList.isEmpty()) {
            return;
        }
        // 파일 경로 파싱 안전장치
        Path baseDirPath;
        try {
            baseDirPath = Paths.get(URI.create(filePath));
        } catch (Exception e) {
            baseDirPath = Paths.get(filePath);
        }

        for (FileEntity fileEntity : fileList) {
            Path target = baseDirPath.resolve(fileEntity.getNewFileName());
            File file = target.toFile();

            if (file.exists()) {
                if (!file.delete()) {
                    throw new IOException("실제 파일 삭제 실패: " + fileEntity.getNewFileName());
                }
                System.out.println("물리 파일 삭제 성공 : " + fileEntity.getNewFileName());
            } else {
                System.out.println("⚠️ 경고: 서버 컴퓨터에 실제 파일이 존재하지 않습니다: " + file.getAbsolutePath());
            }
        }
        fileRepository.deleteAll(fileList);
    }

    // 상품 이미지 하나 삭제
    @Transactional
    public void deleteSingleFile(Long fileId, String filePath) throws IOException {

        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new NoSuchElementException("파일 없음"));

        Path baseDirPath;

        try {
            baseDirPath = Paths.get(URI.create(filePath));
        } catch (Exception e) {
            baseDirPath = Paths.get(filePath);
        }

        Path target = baseDirPath.resolve(fileEntity.getNewFileName());
        File file = target.toFile();

        if (file.exists()) {
            if (file.exists() && !file.delete()) {
                throw new IOException("파일 삭제 실패");
            }
        }
        fileRepository.delete(fileEntity);
    }

}

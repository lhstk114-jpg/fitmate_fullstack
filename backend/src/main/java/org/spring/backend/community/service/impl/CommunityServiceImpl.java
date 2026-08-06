package org.spring.backend.community.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.spring.backend.file.entity.FileEntity;
import org.spring.backend.member.enumtype.Role;
import org.spring.backend.file.enumtype.TableType;
import org.spring.backend.community.dto.CommunityDto;
import org.spring.backend.community.dto.TabDto;
import org.spring.backend.community.entity.CategoryEntity;
import org.spring.backend.community.entity.CommunityEntity;
import org.spring.backend.community.entity.TabEntity;
import org.spring.backend.community.repository.CategoryRepository;
import org.spring.backend.community.repository.CommunityRepository;
import org.spring.backend.community.repository.TabRepository;
import org.spring.backend.community.service.CommunityService;
import org.spring.backend.file.handler.FileHandler;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 게시글(커뮤니티) 서비스 구현체
 * - 게시글 CRUD, 메인페이지 랭킹 데이터, 탭/카테고리/키워드 검색을 담당
 * - 게시글은 categoryEntity 연관관계와 별개로 categoryName/tabId/tabName을 비정규화해 저장 (조회 성능용).
 *   탭/카테고리 이름이 바뀌면 관련 게시글들의 이 값도 함께 갱신되도록 TabServiceImpl.tabUpdate에서
 *   CommunityRepository의 벌크 업데이트 쿼리를 호출하게 되어 있음 (아래 참고).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityServiceImpl implements CommunityService{
  
private final CommunityRepository communityRepository;
private final CategoryRepository categoryRepository;
private final MemberRepository memberRepository;
private final FileHandler fileHandler;
private final TabRepository tabRepository;

    // 첨부파일 저장 경로 (application.properties의 img.path.community, UploadController와 동일 프로퍼티 사용)
    @Value("${img.path.community}")
    private String path;

    /**
     * 탭 작성/수정/삭제 권한 체크
     * 관리자 전용 탭(adminOnly=true, 공지사항)이면 요청자가 ADMIN 권한인지 확인, 일반 탭이면 통과
     */
    private void checkTabWritePermission(TabEntity tab, String requesterEmail) {
        if (!Boolean.TRUE.equals(tab.getAdminOnly())) {
            return; // 일반 탭이면 통과
        }

        MemberEntity requester = memberRepository.findByUserEmail(requesterEmail)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다"));

        // Role은 enum이므로 String과 비교하면 안 됨. enum끼리 비교해야 함.
        if (requester.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(
                    "공지사항은 관리자만 작성/수정/삭제할 수 있습니다."
            );
        }
    }

    /**
     * 관리자 전용 API(adminDelete 등)에서 요청자가 ADMIN 권한인지 확인
     * ✅ 신규 추가: 기존 adminDelete()에는 이런 권한 검증이 전혀 없어서, 컨트롤러/시큐리티 설정에
     *    빈틈이 있으면 누구나 게시글을 삭제할 수 있는 취약점이 될 수 있었음. 서비스단에도 방어 로직을 추가.
     */
    private void checkAdminOnly(String requesterEmail) {
        MemberEntity requester = memberRepository.findByUserEmail(requesterEmail)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다"));

        if (requester.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("관리자만 삭제할 수 있습니다.");
        }
    }

      //고유 파일 이름 생성 (UUID + 원본 파일명으로 파일명 충돌 방지, UploadController와 동일한 방식)
      //고유 파일 이름 생성
    private String generateUniqueFileName(String originalFileName) {
        UUID uuid = UUID.randomUUID();
        return uuid + "-" + originalFileName;
    }

    /**
     * 게시글 작성
     * 1) 작성자 회원 조회 → 2) 카테고리 조회 → 3) 탭 작성 권한 검증
     * → 4) 게시글 저장 (categoryName/tabId/tabName을 카테고리/탭 연관관계에서 값을 꺼내 비정규화 저장)
     * → 5) 첨부파일이 있으면 디스크에 저장 후 FileEntity로 별도 등록
     * (본문 내 이미지는 TiptapEditor가 별도로 /api/upload/image를 통해 업로드하므로 이 로직과는 무관,
     *  여기서 다루는 attachFile은 본문과 별개의 첨부파일)
     */
    @Override
    public void communityInsert(CommunityDto communityDto, String userEmail) {
        MemberEntity memberEntity = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(()->new NoSuchElementException("회원이 존재하지 않습니다"));

        CategoryEntity categoryEntity = categoryRepository.findById(communityDto.getCategoryId())
                .orElseThrow(()->new NoSuchElementException("존재하지 않는 카테고리입니다."));

        checkTabWritePermission(categoryEntity.getTabEntity(), userEmail);

        boolean hasFile = communityDto.getAttachFile() != null && !communityDto.getAttachFile().isEmpty();

        CommunityEntity communityEntity = CommunityEntity.builder()
                .memberEntity(memberEntity)
                .title(communityDto.getTitle())
                .userName(communityDto.getUserName())
                .content(communityDto.getContent())
                .categoryEntity(categoryEntity)
                .categoryName(categoryEntity.getCategoryName())
                .tabId(categoryEntity.getTabEntity().getId())
                .tabName(categoryEntity.getTabEntity().getTabName())
                .userEmail(communityDto.getUserEmail())
                .hasFile(hasFile?1:0)
                .fileEntities((List<FileEntity>) communityDto.getAttachFile())
                .hit(0)
                .reply(0)
                .thumbnail(extractThumbnail(communityDto.getContent())) // 본문 HTML에서 첫 번째 이미지를 썸네일로 자동 추출
                .build();

        CommunityEntity saveCommunity = communityRepository.save(communityEntity);
        if (hasFile){
            try {
                String originalFilename = communityDto.getAttachFile().getOriginalFilename();
                String newFileName = generateUniqueFileName(originalFilename);
                String filePath = path + "/" + newFileName;

                File fileDir = new File(path);
                if (!fileDir.exists()) fileDir.mkdirs();

                communityDto.getAttachFile().transferTo(new File(filePath));

                fileHandler.insertFile(filePath, TableType.COMMUNITY, saveCommunity.getId(), communityDto.getAttachFile());
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 중 오류 발생", e);
            }
        }
    }

    /**
     * 전체 게시글 목록 조회 (subject/search 조합에 따라 제목/내용/작성자 검색으로 분기)
     * subject 또는 search가 비어있으면 전체 조회
     * ✅ @Transactional 추가: toCommunityDto()가 categoryEntity(LAZY) 연관관계에 접근하도록 수정되었으므로,
     *    영속성 컨텍스트가 응답 변환이 끝날 때까지 열려있도록 트랜잭션을 보장 (OSIV가 꺼져있어도 안전하게 동작)
     */
    @Override
  @Transactional
  public Page<CommunityDto> communityList(Pageable pageable, String subject, String search) {
      if (subject==null||subject.isBlank()||search==null||search.isBlank()){
          return communityRepository.findAll(pageable).map(CommunityDto::toCommunityDto);
      }
    Page<CommunityEntity> communityEntities = null;
      switch (subject){
          case "title":
              communityEntities = communityRepository.findByTitleContaining(pageable, search);
                      break;
          case "content":
              communityEntities=communityRepository.findByContentContaining(pageable, search);
                      break;
          case "userName":
              communityEntities=communityRepository.findByUserNameContaining(pageable, search);
                      break;
          default:
              communityEntities= communityRepository.findAll(pageable);
      }
      return communityEntities.map(CommunityDto::toCommunityDto);
  }

    /**
     * 게시글 수정
     * 1) 회원/게시글 조회 → 2) 작성자 본인 또는 관리자인지 검증(checkManage)
     * → 3) 새 카테고리 조회 → 4) 해당 탭 작성 권한 검증 → 5) 필드 갱신 (카테고리를 바꿀 수도 있으므로
     *    categoryEntity/categoryName/tabId/tabName을 전부 새로 세팅)
     * ⚠️ userName은 communityDto가 아니라 memberEntity(현재 로그인한 사용자)의 이름으로 덮어씀
     *    → 원래 작성자가 그 사이 닉네임을 바꿨다면 수정 시점의 최신 닉네임으로 갱신되는 효과
     */
    @Override
    @Transactional
    public void communityUpdate(Long id, CommunityDto communityDto, String userEmail) {
        //멤버조회
        MemberEntity memberEntity = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다"));
        //게시글 조회
        CommunityEntity entity = communityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다: " + id));

        checkManage(entity.getUserEmail(), userEmail);

        // 카테고리 조회
        CategoryEntity categoryEntity = categoryRepository.findById(communityDto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 카테고리입니다."));
        //탭 작성 권한 있는지 확인
        checkTabWritePermission(categoryEntity.getTabEntity(), userEmail);

        entity.setTitle(communityDto.getTitle());
        entity.setContent(communityDto.getContent());
        entity.setHasFile(communityDto.getHasFile());
        entity.setThumbnail(extractThumbnail(communityDto.getContent())); // 본문이 바뀌었을 수 있으니 썸네일도 재추출

        entity.setCategoryEntity(categoryEntity);
        entity.setCategoryName(categoryEntity.getCategoryName());

        entity.setTabId(categoryEntity.getTabEntity().getId());
        entity.setTabName(categoryEntity.getTabEntity().getTabName());

        entity.setUserName(memberEntity.getUserName());
        // @Transactional 안이므로 별도 save() 호출 없이 메서드 종료 시 변경분이 자동으로 DB에 반영됨(dirty checking)
    }

    /**
     * 게시글 삭제 (일반 사용자용)
     * 작성자 본인 또는 관리자만 가능(checkManage) + 소속 탭의 작성 권한도 함께 검증
     * (checkTabWritePermission은 사실상 "관리자 전용 탭이면 관리자만"이라는 의미라 checkManage와 일부 중복되는 조건)
     */
    @Override
    @Transactional
    public void communityDelete(Long id, String userEmail) {
        CommunityEntity entity = communityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다"));

        checkManage(entity.getUserEmail(), userEmail);

        checkTabWritePermission(entity.getCategoryEntity().getTabEntity(), userEmail);

        communityRepository.deleteById(id);
    }

    /**
     * 게시글 삭제 (관리자 전용 엔드포인트용)
     * ✅ 수정: userEmail 파라미터를 추가로 받아 checkAdminOnly로 ADMIN 권한을 검증한 뒤에만 삭제하도록 변경.
     *    (기존에는 아무 검증 없이 바로 삭제 - 컨트롤러/시큐리티 설정에 구멍이 있으면 누구나 삭제 가능한 상태였음)
     */
    @Override
    @Transactional
    public void adminDelete(Long id, String userEmail) {
        CommunityEntity entity = communityRepository.findById(id)
                .orElseThrow(()-> new NoSuchElementException("게시글이 존재하지 않습니다"));

        checkAdminOnly(userEmail);

        communityRepository.deleteById(id);
    }

    /**
     * 게시글 상세 조회
     * categoryId/categoryName/tabId/tabName을 모두 categoryEntity 연관관계를 타고 조회 (연관관계 기준 - 항상 최신 값)
     */
    @Override
  @Transactional
  public CommunityDto communityDetail(Long id, String userEmail) {
   CommunityEntity communityEntity = communityRepository.findById(id)
   .orElseThrow(()->new IllegalArgumentException("게시글이 존재하지 않습니다"));

   return CommunityDto.builder()
   .id(communityEntity.getId())
           .userName(communityEntity.getUserName())
   .title(communityEntity.getTitle())
   .content(communityEntity.getContent())
   .categoryId(communityEntity.getCategoryEntity().getId())
           .categoryName(communityEntity.getCategoryEntity().getCategoryName())
   .hasFile(communityEntity.getHasFile())
           .tabId(communityEntity.getCategoryEntity().getTabEntity().getId())
           .tabName(communityEntity.getCategoryEntity().getTabEntity().getTabName())
   .hit(communityEntity.getHit())
   .reply(communityEntity.getReply())
   .createTime(communityEntity.getCreateTime())
   .updateTime(communityEntity.getUpdateTime())
           .userEmail(communityEntity.getUserEmail())
   .build();
  }

    // 조회수 1 증가 (@Transactional + dirty checking으로 별도 save() 없이 반영됨)
    @Transactional
    @Override
    public void updateHit(Long id) {
        CommunityEntity entity = communityRepository.findById(id).orElseThrow();
        entity.setHit(entity.getHit() + 1);
    }

    /**
     * 커뮤니티 메인페이지 데이터 구성
     * - 모든 탭을 순회하며: 관리자 전용 탭(공지사항)은 최신순 top5, 일반 탭은 조회수순 top5를 뽑아 byTab에 저장
     * - "전체게시판 추천글"(all)은 공지사항 탭 하나를 찾아 그 탭을 제외한 전체 게시글 중 조회수 top5로 구성
     *   (공지사항 탭이 없으면 빈 리스트)
     * - tabs에는 전체 탭 목록(TabDto)도 함께 내려줘서, 프론트가 각 카드의 제목(tabName) 등을 표시하는 데 사용
     */
    @Override
    @Transactional
    public Map<String, Object> mainList() {
        List<TabEntity> allTab = tabRepository.findAll();
        Map<Long, List<CommunityDto>> tabRanking = new LinkedHashMap<>();
        //탭별 top5 추출해서 채우기
        for (TabEntity tab : allTab){
            List<CommunityEntity> top5;
            if (Boolean.TRUE.equals(tab.getAdminOnly())){
                top5 = communityRepository.findTop5ByCategoryEntity_TabEntity_IdOrderByCreateTimeDesc(tab.getId());
            }else{
                top5 = communityRepository.findTop5ByCategoryEntity_TabEntity_IdOrderByHitDesc(tab.getId());
            }
            tabRanking.put(tab.getId(), toDtoList(top5));
        }
        TabEntity noticeTab = allTab.stream()
                .filter(TabEntity::getAdminOnly)
                .findFirst().orElse(null);
        List<CommunityDto> allRanking = noticeTab!=null
                ? toDtoList(communityRepository.findTop5ByCategoryEntity_TabEntity_IdNotOrderByHitDesc(noticeTab.getId()))
                : Collections.emptyList();

        Map<String , Object> result = new LinkedHashMap<>();
        result.put("byTab", tabRanking);
        result.put("all", allRanking);
        result.put("tabs", allTab.stream().map(TabDto::toTabDto).collect(Collectors.toList()));
        return result;
    }

    // 엔티티 리스트 → DTO 리스트 변환 헬퍼
    private List<CommunityDto> toDtoList(List<CommunityEntity> entities){
        return entities.stream().map(CommunityDto::toCommunityDto).collect(Collectors.toList());
    }


    /**
     * 탭/카테고리/키워드 조건으로 게시글 목록 동적 조회 (JPA Specification 사용)
     * - categoryId가 있으면 카테고리로만 필터링 (tabId는 무시됨 - else if 구조라 categoryId가 우선순위 높음)
     * - categoryId가 없고 tabId만 있으면 탭으로 필터링. 이때 CommunityEntity의 비정규화된 tabId 컬럼이 아니라
     *   categoryEntity → tabEntity 연관관계를 타고 필터링 (비정규화 컬럼은 신뢰하지 않고 연관관계 기준으로 정확하게 조회)
     * - keyword가 있으면 제목 또는 작성자명에 포함되는지로 추가 필터링
     */
    @Override
    @Transactional
    public Page<CommunityDto> findCommunityList(Long tabId, Long categoryId, String keyword, Pageable pageable) {

        Specification<CommunityEntity> spec = Specification.unrestricted();

        // 카테고리 필터
        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("categoryEntity").get("id"), categoryId));
        }
        // 탭 필터 (categoryEntity -> tabEntity 관계를 타고 감. 중복 tabId 컬럼은 신뢰 X)
        else if (tabId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("categoryEntity").get("tabEntity").get("id"), tabId));
        }

        // 검색어 필터 제목 또는 작성자에 포함되면 매치
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(root.get("title"), "%" + keyword + "%"),
                    cb.like(root.get("userName"), "%" + keyword + "%")
            ));
        }

        Page<CommunityEntity> entities = communityRepository.findAll(spec, pageable);

        // 여기서도 categoryId/tabId는 연관관계(categoryEntity)를 타고 정확하게 채움
        return entities.map(el -> CommunityDto.builder()
                .id(el.getId())
                .userName(el.getUserName())
                .title(el.getTitle())
                .content(el.getContent())
                .categoryId(el.getCategoryEntity().getId())
                .categoryName(el.getCategoryEntity().getCategoryName())
                .tabId(el.getCategoryEntity().getTabEntity().getId())
                .tabName(el.getCategoryEntity().getTabEntity().getTabName())
                .createTime(el.getCreateTime())
                .hit(el.getHit())
                .thumbnail(el.getThumbnail())
                .build()
        );
    }

    //작성자 또는 관리자 검증 (게시글 수정/삭제 시 공통으로 사용)
    //작성자 또는 관리자 검증
    private void checkManage(String ownerEmail, String requesterEmail) {
        MemberEntity requester = memberRepository.findByUserEmail(requesterEmail)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다"));

        boolean isOwner = ownerEmail.equals(requesterEmail);

        boolean isAdmin = requester.getRole() == Role.ADMIN;

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("본인 또는 관리자만 수정/삭제할 수 있습니다");
        }
    }

    //썸네일 추출 서식 (본문 HTML에서 첫 번째 <img> 태그의 src 속성값을 추출하는 정규식)
    //썸네일 추출 서식
    private static final Pattern IMG_SRC_PATTERN =
            Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    // content(HTML) 안에서 첫 번째 이미지 src를 뽑아 썸네일로 사용 (본문에 이미지가 없으면 null → 프론트가 빈 썸네일 박스로 표시)
    // content(HTML) 안에서 첫 번째 이미지 src를 뽑아 썸네일로 사용
    private String extractThumbnail(String content) {
        if (content == null || content.isBlank()) return null;
        Matcher matcher = IMG_SRC_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }
}

package org.spring.backend.community.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import org.spring.backend.community.entity.CategoryEntity;
import org.spring.backend.community.entity.CommunityEntity;
import org.spring.backend.member.entity.MemberEntity;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 게시글 DTO
 * 게시글 작성/조회/수정 등 커뮤니티 API 전반에서 요청/응답 바디로 사용됨
 * 엔티티 → DTO 변환 방식이 생성자와 정적 팩토리 메서드(toCommunityDto) 두 가지로 존재하는데,
 * 서로 다른 필드를 채우는 방식이 다르므로(아래 각 메서드 주석 참고) 어디서 어떤 걸 쓰는지 주의가 필요함
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class CommunityDto {
  private Long id;

  private String userName;

  private String title;

  private String content;

  private Long categoryId;

  private String categoryName;

  private String tabName;

  private int reply;

    private String userEmail;

  // 파일 업로드용 (본문 이미지가 아닌 첨부파일 처리 시 사용되는 것으로 추정, 실제 사용처는 서비스단 확인 필요)
  private MultipartFile attachFile;

  private int hasFile;

  private LocalDateTime createTime;
  
  private LocalDateTime updateTime;

  private int hit;

  private String originalFileName;

  private Long tabId;

    // 목록 화면에서 카드/썸네일로 보여줄 대표 이미지 URL (CommunityList, CommunityMain 등에서 사용)
    private String thumbnail;

  // JPA 엔티티를 DTO 필드로 직접 들고 있음 (일반적으로는 지양되는 패턴이지만 기존 코드 그대로 유지)
  private MemberEntity memberEntity;

  private CategoryEntity categoryEntity;

  /**
   * 엔티티 → DTO 변환 (생성자 버전)
   * categoryId/categoryName/tabId/tabName을 모두 entity.getCategoryEntity()를 거쳐
   * 연관관계(CategoryEntity → TabEntity)를 타고 가져옴 (연관관계 기준 - 항상 최신 값을 반영)
   */
  public CommunityDto(CommunityEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.userName = entity.getUserName();
        this.content = entity.getContent();
        this.createTime = entity.getCreateTime();
        this.updateTime = entity.getUpdateTime();
        this.categoryId=entity.getCategoryEntity().getId();
        this.categoryName=entity.getCategoryEntity().getCategoryName();
        this.hit= entity.getHit();
        this.tabId=entity.getCategoryEntity().getTabEntity().getId();
        this.tabName=entity.getCategoryEntity().getTabEntity().getTabName();
        this.reply= entity.getReply();
        this.thumbnail = entity.getThumbnail();
    }

    /**
     * 엔티티 → DTO 변환 (정적 팩토리 버전)
     * categoryName/tabId/tabName은 CommunityEntity에 비정규화되어 직접 저장된 컬럼 값을 그대로 사용하지만,
     * categoryId만은 비정규화 컬럼이 따로 없어(CommunityEntity에 categoryId 필드 자체가 없음)
     * categoryEntity 연관관계를 타고 가져오도록 수정함.
     * ✅ 수정 전: .categoryId(communityEntity.getId())  → 게시글 자신의 id가 잘못 들어가던 버그
     * ✅ 수정 후: categoryEntity가 null이 아니면 그 id를, null이면 null을 반환 (연관관계가 끊긴 데이터 방어)
     * ⚠️ categoryEntity는 LAZY 로딩이므로, 이 메서드를 호출하는 시점에 영속성 컨텍스트(트랜잭션)가 열려 있어야
     *    LazyInitializationException 없이 정상 동작함 (mainList, findCommunityList는 @Transactional이 걸려있어 안전,
     *    communityList도 안전하게 이 메서드를 쓸 수 있도록 @Transactional을 추가해둠 - CommunityServiceImpl 참고)
     */
    public static CommunityDto toCommunityDto(CommunityEntity communityEntity){
      return CommunityDto.builder()
              .id(communityEntity.getId())
              .userName(communityEntity.getUserName())
              .title(communityEntity.getTitle())
              .content(communityEntity.getContent())
              .categoryId(communityEntity.getCategoryEntity() != null
                      ? communityEntity.getCategoryEntity().getId()
                      : null)
              .categoryName(communityEntity.getCategoryName())
              .tabId(communityEntity.getTabId())
              .tabName(communityEntity.getTabName())
              .reply(communityEntity.getReply())
              .userEmail(communityEntity.getUserEmail())
              .hasFile(communityEntity.getHasFile())
              .createTime(communityEntity.getCreateTime())
              .updateTime(communityEntity.getUpdateTime())
              .hit(communityEntity.getHit())
              .originalFileName(communityEntity.getOriginalFileName())
              .thumbnail(communityEntity.getThumbnail())
              .build();
    }
}

package org.spring.backend.community.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.spring.backend.common.BasicTime;
import org.spring.backend.file.entity.FileEntity;
import org.spring.backend.member.entity.MemberEntity;

/**
 * 게시글 엔티티 (community_tb)
 * - BasicTime 상속으로 createTime/updateTime 자동 관리
 * - 실제 분류 연관관계는 categoryEntity(→ tabEntity)로 맺어져 있지만,
 *   조회 성능/편의를 위해 categoryName/tabId/tabName을 비정규화(denormalized)해서 컬럼에 그대로 저장해두고 있음
 *   ⚠️ 이 경우 카테고리/탭 이름이 나중에 바뀌면 게시글에 저장된 categoryName/tabName은 자동으로 갱신되지 않으므로
 *      탭/카테고리 이름 수정 시 기존 게시글들의 값도 함께 갱신해주는 로직이 서비스단에 있는지 확인이 필요함
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "community_tb")
public class CommunityEntity extends BasicTime {
  @Id
  @Column(name = "community_id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;

    private String title;

    // 본문 (LONGTEXT: 대용량 HTML 콘텐츠 - Tiptap 에디터가 만든 HTML + base64/URL 이미지 태그 등을 저장)
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    // 비정규화된 탭 id (categoryEntity.tabEntity.id와 동일해야 함, 위 클래스 주석 참고)
    private Long tabId;

    // 비정규화된 카테고리 이름
    private String categoryName;

    // 비정규화된 탭 이름
    private String tabName;

    private String userEmail;

    // 댓글 개수 (댓글 작성/삭제 시 서비스단에서 증감시키는 카운터 컬럼으로 추정)
    private int reply;

    // 첨부파일 보유 여부 (0/1) - fileEntities와 별개로 존재하는 플래그
    private int hasFile;

    // 조회수
    private int hit;

    private String originalFileName;

    // 목록에서 보여줄 대표 썸네일 이미지 URL
    private String thumbnail;


    // 실제 분류 연관관계 (지연 로딩) - 위쪽의 categoryName/tabId/tabName은 이 관계의 스냅샷
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    private CategoryEntity categoryEntity;

    // 작성자 회원 (지연 로딩)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private MemberEntity memberEntity;

    // 이 게시글에 달린 댓글 목록 - 게시글 삭제 시 댓글도 함께 삭제(cascade + orphanRemoval)
    @OneToMany(mappedBy = "communityEntity",cascade = CascadeType.ALL,orphanRemoval= true)
    private List<CommunityReplyEntity> communityReplyEntity = new ArrayList<>();

    //파일엔티티와 1:N 매핑
    // 게시글 삭제 시에만 파일도 함께 삭제(cascade = REMOVE), 댓글과 달리 orphanRemoval은 없음
    // (목록에서 파일을 빼는 것만으로는 자동 삭제되지 않고, 게시글 자체가 삭제될 때만 같이 삭제됨)
    @OneToMany(mappedBy = "communityEntity",
    fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<FileEntity> fileEntities;
}

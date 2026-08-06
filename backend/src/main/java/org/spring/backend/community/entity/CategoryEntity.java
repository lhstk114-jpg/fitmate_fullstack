package org.spring.backend.community.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 카테고리 엔티티 (category_tb)
 * - 탭(TabEntity) 하위의 세부 분류 단위 (예: 자유게시판 탭 아래의 "일반", "FAQ", "QNA" 등)
 * - 카테고리 삭제 시 그 카테고리에 속한 게시글(communityEntity)도 cascade + orphanRemoval로 함께 삭제됨
 *   → TabDetail.jsx에서 카테고리를 삭제하고 저장하면, 해당 카테고리의 게시글도 전부 삭제된다는 뜻이므로
 *     운영 중인 카테고리를 삭제할 때는 주의가 필요함
 */
@Entity
@Builder
@Setter
@Getter
@Table(name="category_tb")
@AllArgsConstructor
@NoArgsConstructor
public class CategoryEntity {
  @Id
  @Column(name="category_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String categoryName;

  // 이 카테고리가 속한 탭 (지연 로딩)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="tab_id")
  private TabEntity tabEntity;

  // 이 카테고리에 속한 게시글 목록 (카테고리 삭제 시 게시글도 함께 삭제됨 - 위 설명 참고)
  @OneToMany(mappedBy = "categoryEntity", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CommunityEntity> communityEntity = new ArrayList<>();
}

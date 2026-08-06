package org.spring.backend.community.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 탭 엔티티 (tab_tb)
 * - 게시판의 최상위 분류 단위 (예: "자유게시판", "공지사항" 등)
 * - adminOnly가 true면 관리자만 작성/삭제 가능한 탭(공지사항용)으로 취급됨 (프론트 여러 곳에서 이 값으로 권한 분기)
 * - categoryList: 하위 카테고리들과 1:N 관계, cascade + orphanRemoval로
 *   탭 삭제 시 카테고리도 함께 삭제되고, 목록에서 빠진 카테고리는 자동 삭제됨 (TabDetail.jsx의 카테고리 삭제 방식과 대응)
 */
@Entity
@Builder
@Setter
@Getter
@Table(name="tab_tb")
@AllArgsConstructor
@NoArgsConstructor
public class TabEntity {
  @Id
  @Column(name="tab_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String tabName;

  // 관리자 전용 탭 여부 (공지사항 등) - 기본값 false
  @Column(name = "admin_only")
  private Boolean adminOnly = false;

  // JSON 직렬화 시 제외 (탭 조회 응답에 카테고리 전체를 자동으로 포함시키지 않기 위함 -
  // 카테고리는 별도 API(/community/category)로 조회하는 구조)
  @JsonIgnore
  @OneToMany(mappedBy = "tabEntity", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CategoryEntity> categoryList = new ArrayList<>();
}

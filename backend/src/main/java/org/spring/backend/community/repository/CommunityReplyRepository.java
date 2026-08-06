package org.spring.backend.community.repository;

import org.spring.backend.community.entity.CommunityReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 댓글 리포지토리
 */
@Repository
public interface CommunityReplyRepository extends JpaRepository<CommunityReplyEntity, Long> {

    // 특정 게시글의 댓글 전체 조회, 작성자 회원 정보(memberEntity)를 LEFT JOIN FETCH로 함께 즉시 로딩
    // (댓글 목록을 순회하며 매번 회원 조회 쿼리가 추가로 나가는 N+1 문제 방지, LEFT JOIN이라 탈퇴 회원의 댓글도 조회됨)
    @Query("SELECT r FROM CommunityReplyEntity r LEFT JOIN FETCH r.memberEntity WHERE r.communityEntity.id = :communityId")
    List<CommunityReplyEntity> findAllByCommunityId(@Param("communityId") Long communityId);
}

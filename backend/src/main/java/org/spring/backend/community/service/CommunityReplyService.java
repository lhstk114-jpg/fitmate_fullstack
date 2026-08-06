package org.spring.backend.community.service;

import org.spring.backend.community.dto.CommunityReplyDto;

import java.util.List;

/**
 * 댓글 서비스 인터페이스
 * 구현체: CommunityReplyServiceImpl
 */
public interface CommunityReplyService {
    // 댓글 작성 (QNA 카테고리는 관리자만 작성 가능하도록 구현체에서 권한 검증)
    public void insertReply(CommunityReplyDto dto);

    // 특정 게시글의 댓글 전체 조회
    public List<CommunityReplyDto> replyList(Long communityId);

    // 댓글 삭제
    public void deleteReply(Long id);

    // 댓글 수정 (내용만 수정 가능)
    public void updateReply(Long id, CommunityReplyDto dto);

    // 댓글 단건 상세 조회
    public CommunityReplyDto detailReply(Long id);
}

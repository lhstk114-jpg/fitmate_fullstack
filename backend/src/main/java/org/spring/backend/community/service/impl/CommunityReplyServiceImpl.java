package org.spring.backend.community.service.impl;

import lombok.RequiredArgsConstructor;
import org.spring.backend.member.enumtype.Role;
import org.spring.backend.community.dto.CommunityReplyDto;
import org.spring.backend.community.entity.CommunityEntity;
import org.spring.backend.community.entity.CommunityReplyEntity;
import org.spring.backend.community.repository.CommunityReplyRepository;
import org.spring.backend.community.repository.CommunityRepository;
import org.spring.backend.community.service.CommunityReplyService;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * 댓글 서비스 구현체
 * - QNA 카테고리 게시글은 관리자만 댓글 작성 가능하도록 서버측에서 검증 (프론트의 403 처리와 대응)
 */
@Service
@RequiredArgsConstructor
public class CommunityReplyServiceImpl implements CommunityReplyService {
    private final CommunityRepository communityRepository;
    private final CommunityReplyRepository communityReplyRepository;
    private final MemberRepository memberRepository;

    /**
     * 댓글 작성 권한 체크
     * ✅ 수정: 기존에는 이 메서드(정확히 "QNA" 일치, categoryEntity 연관관계 사용)와
     *    insertReply 안의 별도 체크(대소문자 무관 "QNA" 포함, categoryName 비정규화 컬럼 사용)가
     *    서로 다른 기준으로 중복 존재했음. 판단 기준을 이 메서드 하나로 통일하고,
     *    insertReply의 중복 체크는 제거함(아래 insertReply 참고).
     * - CommunityEntity에 비정규화 저장된 categoryName 컬럼을 사용 (categoryEntity는 LAZY라 트랜잭션
     *   범위를 벗어나면 접근 시 예외가 날 수 있는데, categoryName 컬럼은 즉시 로딩되어 있어 더 안전함)
     * - "QNA" 포함 여부를 대소문자 구분 없이 판단 (예: "QNA", "qna게시판" 등 모두 매치)
     */
    private void checkReplyWritePermission(CommunityEntity communityEntity, String requesterEmail){
        String categoryName = communityEntity.getCategoryName();
        boolean isQna = categoryName != null && categoryName.toUpperCase().contains("QNA");

        if (!isQna) {
            return;
        }

        MemberEntity requester = memberRepository.findByUserEmail(requesterEmail)
                .orElseThrow(()-> new NoSuchElementException("회원이 존재하지 않습니다"));
        if (requester.getRole()!= Role.ADMIN){
            throw new AccessDeniedException("QNA 게시글의 댓글은 관리자만 작성할 수 있습니다.");
        }
    }

    /**
     * 댓글 작성
     * 1) SecurityContext에서 현재 로그인한 사용자를 가져와 2) 회원 정보 조회 → 3) 게시글 조회
     * → 4) 권한 검증(checkReplyWritePermission 한 곳으로 통일) → 5) 저장
     */
    @Override
    public void insertReply(CommunityReplyDto dto) {
        // 1. SecurityContext에서 현재 인증된 유저의 정보를 가져옵니다.
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. 해당 username을 기반으로 DB에서 Member를 찾습니다.
        MemberEntity memberEntity = memberRepository.findByUserEmail(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 회원을 찾을 수 없습니다."));


        // 3. 게시글 정보 조회 (dto에 들어있는 communityId 사용)
        Long communityId = Long.parseLong(String.valueOf(dto.getCommunityId()));
        CommunityEntity communityEntity = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 4. QNA 게시글 댓글 권한 검증 (기준을 이 메서드 하나로 통일)
        checkReplyWritePermission(communityEntity, memberEntity.getUserEmail());

        // 5. 엔티티 생성 시 조회한 memberEntity를 직접 사용 (작성자명/이메일도 스냅샷으로 함께 저장)
        CommunityReplyEntity replyEntity = CommunityReplyEntity.builder()
                .content(dto.getContent())
                .userEmail(memberEntity.getUserEmail())
                .userName(memberEntity.getUserName()) // 로그인한 유저의 이름을 사용
                .communityEntity(communityEntity)
                .memberEntity(memberEntity) // null이 아닌 실제 엔티티 객체 전달
                .build();

        communityReplyRepository.save(replyEntity);
    }

    // 특정 게시글의 댓글 목록을 조회해 DTO로 변환
    @Override
    public List<CommunityReplyDto> replyList(Long communityId) {
        return communityReplyRepository.findAllByCommunityId(communityId)
                .stream().map(CommunityReplyDto::toReplyDto).toList();
    }

    // 댓글 삭제 (존재 여부만 확인, 작성자/관리자 권한 검증은 이 메서드에는 없음 - 컨트롤러/시큐리티단에서 처리되는지 확인 필요)
    @Override
    public void deleteReply(Long id) {
        if (!communityReplyRepository.existsById(id)){
            throw new IllegalArgumentException("댓글이 존재하지 않습니다");
        }
        communityReplyRepository.deleteById(id);
    }

    // 댓글 수정 (내용만 갱신, 마찬가지로 이 메서드 안에는 별도 권한 검증이 없음)
    @Override
    public void updateReply(Long id, CommunityReplyDto dto) {
        CommunityReplyEntity replyEntity = communityReplyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다"));

        replyEntity.setContent(dto.getContent());
        communityReplyRepository.save(replyEntity);
    }

    // 댓글 단건 상세 조회
    @Override
    public CommunityReplyDto detailReply(Long id) {
        CommunityReplyEntity replyEntity = communityReplyRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("댓글이 없습니다"));
        return CommunityReplyDto.toReplyDto(replyEntity);
    }
}

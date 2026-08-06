package org.spring.backend.member.service;

import org.spring.backend.member.dto.MemberDto;
import org.spring.backend.member.enumtype.Role;
import org.spring.backend.shop.reservation.dto.ReservationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface MemberService {
    void insertMember(MemberDto memberDto);
    void insertAdminMember(MemberDto memberDto);

    boolean emailCheck(String userEmail);

    List<MemberDto> memberList();

    //관리자페이지 회원관리 시 사용할 멤버리스트
    Page<MemberDto> memberList(Pageable pageable, String subject, String search, Role role);
    //트레이너 회원관리 시 사용할 멤버리스트
    Page<ReservationDto> memberListSummary(Pageable pageable, String subject, String search, Long trainerId);

    //관리자페이지 회원관리 시 사용할 정보 조회
    MemberDto memberDetail(Long id);

    //트레이너의 회원관리 시 사용할 정보 조회(제한된 정보)
    MemberDto memberSummary(Long id);

    //멤버가 트레이너 조회 시 사용할 정보 조회(제한된 정보)
    MemberDto trainerSummary(Long id);

    //멤버 개인페이지 조회 시 사용할 정보 조회
    MemberDto memberDetail(String userEmail);

    void memberUpdate(MemberDto memberDto) throws IOException;

    void memberDelete(Long id) throws IOException;

    //이메일을 입력하면 데이터 제거
    void memberDelete(String userEmail) throws IOException;

    //authSlice에 들어갈 기본적인 정보 조회
    MemberDto memberInit(String userEmail);
}

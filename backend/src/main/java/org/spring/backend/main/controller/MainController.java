package org.spring.backend.main.controller;

import lombok.RequiredArgsConstructor;
import org.spring.backend.community.dto.CommunityDto;
import org.spring.backend.member.enumtype.Interest;
import org.spring.backend.main.dto.MainResponseDto;
import org.spring.backend.main.service.MainService;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.jwt.CustomUserDetails;
import org.spring.backend.member.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {
    private final MainService mainService;
    private final MemberRepository memberRepository;
    @GetMapping("")
    public ResponseEntity<MainResponseDto> getMainData(
            @AuthenticationPrincipal CustomUserDetails user       //backend 기준 user 정보가 담기는곳
    ) {
        System.out.println("===== /main 요청 들어옴 =====");
        System.out.println("user = " + user);

        //response 변수 선언
        MainResponseDto response;

        //비로그인 상태일때
        if (user == null) {
            System.out.println("비회원 메인 실행");
            response = mainService.getDefaultMainData();
        //로그인 상태일때
        } else {
            System.out.println("회원 메인 실행");
//            CustomUserDetails 에서 userEmail 가져옴
            String userEmail = user.getUsername();
            System.out.println("이메일"+userEmail);
            //userEmail로 memberEntity조회
            Optional<MemberEntity> optionalMemberEntity =
                    memberRepository.findByUserEmail(userEmail);
            if (optionalMemberEntity.isEmpty()) {
                throw new NoSuchElementException("회원 정보를 불러올수 없습니다.");
            }

            MemberEntity memberEntity = optionalMemberEntity.get();
            //memberEntity -> memberAddEntity-> Interest 가져옴
            //user Interest 가 null일때 -> default 실행
            if (memberEntity.getMemberAddEntity() == null ||
                    memberEntity.getMemberAddEntity().getInterest() == null) {
                response = mainService.getDefaultMainData();
            //user Interest 를 담아서 service실행
            } else {
                Interest interest = memberEntity.getMemberAddEntity().getInterest();
                response = mainService.getMainData(interest);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/community/best")
    public ResponseEntity<List<CommunityDto>> getBestCommunityList(
            @RequestParam String tabName) {

        return ResponseEntity.ok(
                mainService.getBestCommunityList(tabName)
        );
    }

}

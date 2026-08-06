package org.spring.backend.member.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.spring.backend.member.dto.MemberDto;
import org.spring.backend.member.enumtype.Role;
import org.spring.backend.member.jwt.CustomUserDetails;
import org.spring.backend.member.service.MemberService;
import org.spring.backend.shop.reservation.dto.ReservationDto;
import org.spring.backend.shop.reservation.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ReservationService reservationService;

    @PostMapping("/join")
    public ResponseEntity<?> join(MemberDto memberDto){
        // 회원가입 비즈니스 로직 실행
        memberService.insertMember(memberDto);

        return ResponseEntity.ok("ok");
    }


    //초기 authSlice에 멤버데이터를 넣기 위한 api
    @GetMapping("/init/{userEmail:.+}") //이메일 특성상 test@email.com으로 들어오기에 .뒤까지 읽을수 있게 설정
    public ResponseEntity<?> memberinit(@PathVariable("userEmail")String userEmail){
        MemberDto memberDto = memberService.memberInit(userEmail);

        Map<String, MemberDto> map = new HashMap<>();
        map.put("result", memberDto);
        //상태 (state), 값(body)
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    //마이페이지 로딩시 회원정보 전달 api
    @GetMapping("/detail")
    public ResponseEntity<?> memberDetail(@AuthenticationPrincipal CustomUserDetails userDetails){
        String userEmail = userDetails.getUsername();

        MemberDto memberDto = memberService.memberDetail(userEmail);

        Map<String, MemberDto> map = new HashMap<>();
        map.put("result", memberDto);

        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    //회원탈퇴 api
    @DeleteMapping("/quit")
    public ResponseEntity<?> myPageDelete(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          HttpServletResponse response){
        String userEmail = userDetails.getUsername();
        try{
            memberService.memberDelete(userEmail);
            // refresh쿠키를 삭제시키기 위해 만료된 쿠키생성
            Cookie cookie = new Cookie("refresh", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            return ResponseEntity.ok("ok");
        }catch (Exception e){
            System.out.println("회원탈퇴중 에러발생: " + e.getMessage());
            return ResponseEntity.status(500).body("회원탈퇴 실패: " + e.getMessage());
        }

    }

    //회원수정 api
    @PutMapping("/update")
    public ResponseEntity<?> myPageUpdate(@ModelAttribute MemberDto memberDto) throws IOException {
        memberService.memberUpdate(memberDto);
        return ResponseEntity.ok("ok");
    }

    //이메일체크 api
    @PostMapping("/email")
    public ResponseEntity<?> emailCheck(MemberDto memberDto){
        if(memberService.emailCheck(memberDto.getUserEmail())){
            return ResponseEntity.ok("no");
        }else{
            return ResponseEntity.ok("ok");
        }
    }

    @GetMapping("/members/summary/{id}")
    public ResponseEntity<?> memberSummary(@PathVariable("id")Long id){
        MemberDto memberDto = memberService.memberSummary(id);
        List<ReservationDto> reservationDto = reservationService.getMemberReservation(id);

        Map<String, Object> map = new HashMap<>();
        map.put("result", memberDto);
        map.put("resultReservation", reservationDto);

        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
    @GetMapping("/trainers/summary/{id}")
    public ResponseEntity<?> trainerSummary(@PathVariable("id")Long id){
        MemberDto memberDto = memberService.trainerSummary(id);

        Map<String, Object> map = new HashMap<>();
        map.put("result", memberDto);

        return ResponseEntity.status(HttpStatus.OK).body(map);
    }


    //일반 회원이 아닌사람만 접근가능 api

    @PostMapping("/admin/insert")
    public ResponseEntity<?> insert(@ModelAttribute MemberDto memberDto){
        // 회원가입 비즈니스 로직 실행
        memberService.insertAdminMember(memberDto);

        return ResponseEntity.ok("ok");
    }

    @GetMapping("/admin/memberList")
    public ResponseEntity<?> memberList(@PageableDefault(page = 0, size = 5, sort="id",
    direction = Sort.Direction.ASC)Pageable pageable,
                                        @RequestParam(value = "subject",required = false)String subject,
                                        @RequestParam(value = "role",required = false)String roleStr,
                                        @RequestParam(value = "search", required = false)String search){
        // String -> Role Enum 변환 (null이거나 유효하지 않은 문자열 처리)
        Role role = Role.MEMBER;
        if (roleStr != null && !roleStr.trim().isEmpty() && !"ALL".equalsIgnoreCase(roleStr)) {
            try {
                role = Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 Enum 문자열이 들어왔을 때 처리 (예: null 세팅 또는 예외 던지기)
                role = null;
            }
        }
        Page<MemberDto> memberList = memberService.memberList(pageable, subject, search, role);

        int newPage = memberList.getNumber(); //현재페이지
        int totalPage = memberList.getTotalPages(); //전체페이지
        int blockNum = 5; //한페이지에 보여질 페이지넘버의 수

        //블록 시작
        int startPage = (newPage / blockNum) * blockNum + 1; //시작페이지
        //블록 끝
        int endPage = Math.min(startPage+blockNum-1, totalPage); //끝페이지
        Map<String, Object> response = new HashMap<>();
        response.put("memberList", memberList.getContent());
        response.put("currentPage", newPage);
        response.put("totalPage", totalPage);
        response.put("startPage", startPage);
        response.put("totalElements", memberList.getTotalElements());
        response.put("endPage", endPage);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/admin/memberListSummary")
    public ResponseEntity<?> memberListSummary(@PageableDefault(page = 0, size = 5, sort="id",
                                                direction = Sort.Direction.ASC)Pageable pageable,
                                        @RequestParam(value = "subject",required = false)String subject,
                                        @RequestParam(value = "search", required = false)String search,
        @AuthenticationPrincipal CustomUserDetails userDetails){
        String userEmail = userDetails.getUsername();
        MemberDto memberDto = memberService.memberDetail(userEmail);
        Page<ReservationDto> memberList = memberService.memberListSummary(pageable, subject, search, memberDto.getId());
        memberList.forEach(System.out::println);
        int newPage = memberList.getNumber(); //현재페이지
        int totalPage = memberList.getTotalPages(); //전체페이지
        int blockNum = 5; //한페이지에 보여질 페이지넘버의 수

        //블록 시작
        int startPage = (newPage / blockNum) * blockNum + 1; //시작페이지
        //블록 끝
        int endPage = Math.min(startPage+blockNum-1, totalPage); //끝페이지
        Map<String, Object> response = new HashMap<>();
        response.put("memberList", memberList.getContent());
        response.put("currentPage", newPage);
        response.put("totalPage", totalPage);
        response.put("startPage", startPage);
        response.put("totalElements", memberList.getTotalElements());
        response.put("endPage", endPage);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/admin/detail/{id}")
    public ResponseEntity<?> adminMemberDetail(@PathVariable("id")Long id){
        MemberDto memberDto = memberService.memberDetail(id);

        Map<String, MemberDto> map = new HashMap<>();
        map.put("result", memberDto);

        return ResponseEntity.status(HttpStatus.OK).body(map);
    }



    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable("id")Long id) throws IOException {
        memberService.memberDelete(id);
        return ResponseEntity.ok("ok");
    }
}

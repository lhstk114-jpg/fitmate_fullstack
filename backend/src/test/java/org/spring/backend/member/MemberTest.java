package org.spring.backend.member;

import org.junit.jupiter.api.Test;
import org.spring.backend.member.dto.MemberDto;
import org.spring.backend.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MemberTest {

    @Autowired
    MemberService memberService;

    //1. 회원가입
    @Test
    void insert(){

        for(int i=1;i<50;i++){
            String memberName = "m"+i;
            String memberEmail = "m"+i+"@email.com";
            String pw = "11";
            MemberDto memberDto = MemberDto.builder()
                    .userEmail(memberEmail)
                    .userPw(pw)
                    .userName(memberName)
                    .build();
            memberService.insertMember(memberDto);
        }


    }
}

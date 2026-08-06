package org.spring.backend.shop.MemberProduct.controller;

import java.util.List;

import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.jwt.CustomUserDetails;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.shop.MemberProduct.dto.MemberProductDto;
import org.spring.backend.shop.MemberProduct.repository.MemberProductRepository;
import org.spring.backend.shop.MemberProduct.service.MemberProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/member-products")
@RequiredArgsConstructor
public class MemberProductController {

    private final MemberProductService memberProductService;
    private final MemberProductRepository memberProductRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/active-pt")
    public ResponseEntity<List<MemberProductDto>> getActivePtProducts(
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                memberProductService.getActivePtProducts(user.getUsername()));
    }

    @GetMapping("/subscribe")
    public ResponseEntity<Boolean> checkSubscribe(
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                memberProductService.checkSubscribe(user.getUsername()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<MemberProductDto>> getMyProducts(
            @AuthenticationPrincipal CustomUserDetails user) {
        MemberEntity member = memberRepository
                .findByUserEmail(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));
  
        List<MemberProductDto> list =
                memberProductService.getMyProducts(member.getId());
    
        return ResponseEntity.ok(list);
    }
}
package org.spring.backend.member.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.spring.backend.member.entity.RefreshEntity;
import org.spring.backend.member.repository.RefreshRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
//소셜로그인 oauth2 성공시의 핸들러
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
//    private final RefreshRepository refreshRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    //프론트 페이지 리다이렉트를 위한 서버주소(프론트서버주소)
    @Value("${app.front-url}")
    private String redirectURL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //유저 정보 가져오기
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String userEmail = customUserDetails.getUsername();
        //재 로그인시 리프레쉬 토큰 쌓이는것을 방지
//        refreshRepository.deleteByUserEmail(userEmail);
        redisTemplate.delete(userEmail);

        //권한정보 가져오기
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        //JWT토큰 생성
        String access = jwtUtil.createJwt("access",userEmail, role, 60 * 60 *  100L);
        String refresh = jwtUtil.createJwt("refresh",userEmail, role, 86400000L);

        //Refresh토큰 저장
//        addRefreshEntity(userEmail, refresh, 86400000L);
        addRefreshToRedis(userEmail, refresh, 86400000L);

        //Refresh쿠키 저장
        response.addCookie(createCookie("refresh",refresh));

        Map<String, Object> claims = new HashMap<>();
        claims.put("userEmail",userEmail);
        claims.put("role",role);
        claims.put("access",access);

        String jsonStr = objectMapper.writeValueAsString(claims);
        //쿠키값에 공백이나 특수문자가 들어가지않게 URL인코딩
        String encodedJsonStr = URLEncoder.encode(jsonStr, StandardCharsets.UTF_8);

        //기존 로그인방식과 달리 oauth2는 json방식으로 바로 보내주지 못하기에
        //임시 memberInfo쿠키에 URLEncoding이 완료된 문자열을 넣어서 쿠키로 보냄
        Cookie memberInfoCookie = new Cookie("memberInfo",encodedJsonStr);
        memberInfoCookie.setPath("/");
        memberInfoCookie.setMaxAge(60);
        memberInfoCookie.setHttpOnly(false);
        response.addCookie(memberInfoCookie);

        //성공하고나면 보낼 데이터, 리다이렉트 주소
        getRedirectStrategy().sendRedirect(request, response, redirectURL);
    }

    private void addRefreshToRedis(String userEmail, String refresh, long expireMs) {
        // opsForValue().set(key, value, timeout, timeunit)을 사용해 만료시간 자동관리
        redisTemplate.opsForValue().set(
                userEmail,
                refresh,
                expireMs,
                TimeUnit.MILLISECONDS
        );
    }

    //Refresh토큰 DB서버에 저장
//    private void addRefreshEntity(String userEmail, String refresh, Long expireMs){
//        Date date = new Date(System.currentTimeMillis() + expireMs);
//
//        RefreshEntity refreshEntity = RefreshEntity.builder()
//                .userEmail(userEmail)
//                .refresh(refresh)
//                .expiration(date.toString())
//                .build();
//        refreshRepository.save(refreshEntity);
//    }

    //쿠키 생성
    private Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);

        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }
}

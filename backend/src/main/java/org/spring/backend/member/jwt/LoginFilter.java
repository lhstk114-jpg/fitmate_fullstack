package org.spring.backend.member.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    //사용자 인증 처리 AuthenticationManager객체
    private final AuthenticationManager authenticationManager;
    //JWT토큰 생성 및 검증 유틸리티
    private final JWTUtil jwtUtil;
    //Refresh토큰 저장 레포지토리
//    private final RefreshRepository refreshRepository;

    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String userName = obtainUsername(request);
        String password = obtainPassword(request);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userName, password,
                null);
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String userEmail = customUserDetails.getUsername();
        //재 로그인시에 refresh토큰이 쌓이는걸 방지하기 위해 제거
//        refreshRepository.deleteByUserEmail(userEmail);
        redisTemplate.delete(userEmail);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        String access = jwtUtil.createJwt("access",userEmail, role, 60 * 60 *  10000L);
        String refresh = jwtUtil.createJwt("refresh",userEmail, role, 86400000L);

        //Refresh토큰 저장
        addRefreshToRedis(userEmail, refresh, 86400L);

        //Refresh쿠키 저장
        response.addCookie(createCookie("refresh",refresh));

        Map<String, Object> claims = new HashMap<>();
        claims.put("userEmail",userEmail);
        claims.put("role",role);
        claims.put("access",access);

        String jsonStr = objectMapper.writeValueAsString(claims);
        response.setContentType("application/json");

        PrintWriter printWriter = response.getWriter();
        printWriter.println(jsonStr);
        printWriter.close();
    }

    private void addRefreshToRedis(String userEmail, String refresh, long expireS) {
        // opsForValue().set(key, value, timeout, timeunit)을 사용해 만료시간 자동관리
        redisTemplate.opsForValue().set(
                userEmail,
                refresh,
                expireS,
                TimeUnit.SECONDS
        );
    }
    //쿠키 생성
    private Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);

        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    //인증 실패시
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(401);
    }
}



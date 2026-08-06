package org.spring.backend.member.service.impl;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.spring.backend.member.jwt.JWTUtil;
import org.spring.backend.member.service.TokenValidationService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenValidationServiceImpl implements TokenValidationService {
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public String validateRefreshAndGetEmail(HttpServletRequest request, HttpServletResponse response) {
        //Refresh토큰 가져오기
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                //쿠키중 refresh이름이 달린 쿠키 찾기
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                }
            }
        }

        //Refresh토큰 유효성 검사
        if(refresh == null){
            deleteRefreshCookie(response);
            throw new IllegalArgumentException("refresh token null");
        }
        //유효기간 만료 검사
        try{
            jwtUtil.isExpired(refresh);
        }catch (ExpiredJwtException e){
            deleteRefreshCookie(response);
            throw new IllegalArgumentException("refresh token expired");
        }
        //토큰카테고리가 refresh인지 확인(이름만 refresh인것을 걸러내기 위함)
        String category = jwtUtil.getCategory(refresh);
        if(!category.equals("refresh")){
            deleteRefreshCookie(response);
            throw new IllegalArgumentException("invalid refresh token format");
        }
        //Refresh테이블에 저장되어있는지 확인
        String userEmail = jwtUtil.getUserEmail(refresh);
        try{
            Boolean isExist = redisTemplate.hasKey(userEmail);
            if(!isExist){
                deleteRefreshCookie(response);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new IllegalArgumentException("invalid refresh session");
            }
        }catch (IllegalArgumentException e) {
            //throw에러가 try~catch문에서 일어나면 catch로 넘어가기에
            //에러내용 그대로 반환
            throw e;
        } catch (Exception e){
            deleteRefreshCookie(response);
            //서버에러(Redis서버 다운시)
            throw new IllegalStateException("database connection error");
        }
        return userEmail;
    }

    @Override
    public void deleteRefreshCookie(HttpServletResponse response) {
        // refresh쿠키를 삭제시키기 위해 만료된 쿠키생성
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}

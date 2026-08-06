package org.spring.backend.member.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.spring.backend.member.repository.RefreshRepository;
import org.spring.backend.member.service.TokenValidationService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {
//    private final RefreshRepository refreshRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final TokenValidationService tokenValidationService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException{
        String requestUri = request.getRequestURI();
        //로그아웃이 아닐때
        if(!requestUri.matches("^\\/logout$")){
            chain.doFilter(request,response);
            return;
        }
        String requestMethod = request.getMethod();
        //request 메소드가 POST가 아닐때
        if(!requestMethod.equals("POST")){
            chain.doFilter(request,response);
            return;
        }

        try {
            String userEmail = tokenValidationService.validateRefreshAndGetEmail(request, response);

            // 검증 완료 후 세션 삭제 및 쿠키 무효화
            redisTemplate.delete(userEmail);
            tokenValidationService.deleteRefreshCookie(response);

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (IllegalArgumentException e) {
            // 토큰 누락, 만료, 위조, 세션 없음 등의 예외 발생 시 -> 400 Bad Request
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        } catch (IllegalStateException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

package org.spring.backend.member.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.spring.backend.member.jwt.JWTUtil;
import org.spring.backend.member.service.TokenValidationService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class ReissueController {
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    private final TokenValidationService tokenValidationService;

    @PostMapping("/api/reissue")
    @Transactional
    public ResponseEntity<?> reissue(HttpServletRequest request,
                                     HttpServletResponse response){
        try{

            String userEmail = tokenValidationService.validateRefreshAndGetEmail(request,response);
            String refresh = getRefreshFromCookie(request);
            String role = jwtUtil.getRole(refresh);

            //토큰생성
            String newAccess = jwtUtil.createJwt("access",userEmail, role, 60* 60 *10000L);
            String newRefresh = jwtUtil.createJwt("refresh",userEmail,role,84600000L);
            redisTemplate.delete(userEmail);
            addRefreshToRedis(userEmail, newRefresh, 864000L);

            response.setHeader("access",newAccess);
            response.addCookie(createCookie("refresh", newRefresh));
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (IllegalArgumentException e) {
            // 토큰 만료, 포맷 오버 등 잘못된 요청 처리는 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (IllegalStateException e) {
            // Redis 서버 다운 등 예외
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }
    // 쿠키 배열에서 안전하게 refresh 토큰만 찾아 꺼내는 헬퍼 메서드
    private String getRefreshFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
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

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }
}

package org.spring.backend.member.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.spring.backend.member.enumtype.Role;
import org.spring.backend.member.entity.MemberEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        //access토큰 확인
        String accessToken = request.getHeader("access");

        //토큰이 없을때 다음 필터로
        if(accessToken == null){
            filterChain.doFilter(request, response);
            return;
        }
        //토큰의 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try{
            jwtUtil.isExpired(accessToken);
        }catch (ExpiredJwtException e){
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        //토큰 카테고리 확인
        String category = jwtUtil.getCategory(accessToken);

        if(!category.equals("access")){
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        //유저계정, 권한 가져오기
        String userEmail = jwtUtil.getUserEmail(accessToken);
        String role = jwtUtil.getRole(accessToken);

        MemberEntity memberEntity = MemberEntity.builder()
                .userEmail(userEmail)
                .role(Role.valueOf(role)).build();
        CustomUserDetails customUserDetails = new CustomUserDetails(memberEntity);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails,null,
                customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}

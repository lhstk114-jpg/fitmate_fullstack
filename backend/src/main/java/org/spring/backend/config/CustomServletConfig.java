package org.spring.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CustomServletConfig implements WebMvcConfigurer {

    // Cors 설정을 통해 React 개발 서버와의 통신을 허용
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                //  실제 React 개발 서버 주소 및 운영 주소를 명시
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS")
                .allowedHeaders("Authorization", "Cache-Control", "Content-Type")
                .maxAge(3600); // 프리플라이트(Preflight) 요청 캐싱 시간 (초 단위, 1시간 권장)
    }
}
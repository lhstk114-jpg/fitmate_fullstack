package org.spring.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfigMvcClass implements WebMvcConfigurer {
    //각자 사용하는 이미지별 경로 주입
    @Value("${img.path.member}")
    private String memberPath;

    @Value("${img.path.product}")
    private String itemPath;

    @Value("${img.path.community}")
    private String communityPath;

    @Value("${img.path.popup}")
    private String popupPath;

    @Value("${img.path.schedule}")
    private String schedulePath;

    @Value("${img.path.exercise}")
    private String exercisePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        //각각 사용하는 파일 경로 변환
        String memberLoc = ensureTrailingSlash(memberPath);
        String itemLoc = itemPath;
        String communityLoc = ensureTrailingSlash(communityPath);
        String popupLoc = ensureTrailingSlash(popupPath);
        String scheduleLoc = ensureTrailingSlash(schedulePath);
        String exerciseLoc = ensureTrailingSlash(exercisePath);

        // 멤버 프로필 이미지 경로 매핑
        registry.addResourceHandler("/upload/member/**")
                .addResourceLocations(memberLoc);

        // 아이템 이미지 경로 매핑
        registry.addResourceHandler("/upload/product/**")
                .addResourceLocations(itemLoc);

        // 커뮤니티 이미지 경로 매핑
        registry.addResourceHandler("/upload/community/**")
                .addResourceLocations(communityLoc);
        // 팝업 이미지 경로 매핑
        registry.addResourceHandler("/upload/popup/**")
                .addResourceLocations(popupLoc);
        // 스케줄 이미지 경로 매핑
        registry.addResourceHandler("/upload/schedule/**")
                .addResourceLocations(scheduleLoc);
        // 운동 이미지 경로 매핑
        registry.addResourceHandler("/upload/exercise/**")
                .addResourceLocations(exerciseLoc);

    }
    //경로 끝에 / 경로로 변경해주고, 윈도우 프로토콜 형식을 맞춰주는 메서드
    private String ensureTrailingSlash(String path) {
        if (path == null || path.isEmpty()) return "";

        // 1. 이미 'file:'로 시작한다면 유지, 아니면 추가
        if (!path.startsWith("file:")) {
            path = "file:/" + path; // 로컬 경로일 경우 file:/로 시작
        }

        // 2. 슬래시 개수를 강제로 1개로 줄이는 로직 제거 (이게 문제였음)
        // 3. 맨 끝에 슬래시가 없다면 붙여줌
        if (!path.endsWith("/")) {
            path = path + "/";
        }

        return path;
    }
}

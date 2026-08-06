package org.spring.backend.map.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class MapController {
    @Value("${KAKAO_MAP_KEY}")
    private String kakaoKey;

    @GetMapping("/kakaoMap")
    public ResponseEntity<Map<String, String>> getKakaoMapConfig() {

        return ResponseEntity.ok(
                Map.of("kakaoKey", kakaoKey)
        );
    }
}


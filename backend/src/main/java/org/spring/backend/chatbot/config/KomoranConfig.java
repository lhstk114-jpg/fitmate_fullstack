package org.spring.backend.chatbot.config;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Configuration
public class KomoranConfig {

    @Bean
    public Komoran komoran() {

        Komoran komoran = new Komoran(DEFAULT_MODEL.LIGHT);

        try {

            ClassPathResource resource = new ClassPathResource("user.dic");

            // JAR 내부 리소스를 임시 파일로 복사
            Path tempFile = Files.createTempFile("user", ".dic");

            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Komoran에 임시 파일 경로 전달
            komoran.setUserDic(tempFile.toAbsolutePath().toString());

            System.out.println("코모란 사용자 사전 주입 성공");

        } catch (Exception e) {

            System.err.println("코모란 사용자 사전 로드 실패 : " + e.getMessage());

        }

        return komoran;
    }
}
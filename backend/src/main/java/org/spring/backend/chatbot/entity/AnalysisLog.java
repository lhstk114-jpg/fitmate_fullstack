package org.spring.backend.chatbot.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "analysis_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED) //JPA기본 엔티티 생성자의 안정성 확보
public class AnalysisLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String originalMessage;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String analyzedResult;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public AnalysisLog(String originalMessage, String analyzedResult){
        this.originalMessage = originalMessage;
        this.analyzedResult = analyzedResult;
        this.createdAt = LocalDateTime.now();
    }
}

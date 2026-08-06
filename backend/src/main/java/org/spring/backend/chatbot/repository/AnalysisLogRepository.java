package org.spring.backend.chatbot.repository;

import org.spring.backend.chatbot.entity.AnalysisLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisLogRepository extends JpaRepository<AnalysisLog, Long> {
    // 최신 데이터를 위로 올리기 위해 ID 내림차순 정렬 조회 메서드 추가
    java.util.List<AnalysisLog> findAllByOrderByIdDesc();
}
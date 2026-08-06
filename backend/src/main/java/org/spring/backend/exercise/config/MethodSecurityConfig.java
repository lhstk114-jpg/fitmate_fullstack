package org.spring.backend.exercise.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * @PreAuthorize 등 메서드 단위 보안 어노테이션을 쓰려면 필요.
 * ExerciseController의 /sync/{target}, /sync/all, /translate 엔드포인트가
 * @PreAuthorize("hasRole('ADMIN')")를 사용하므로 이 설정이 어딘가에는 있어야 동작함.
 *
 * ⚠️ 정리 후보: 이미 프로젝트의 SecurityConfig 등에 @EnableMethodSecurity가 선언되어 있다면
 * 이 파일은 중복이니 지워도 된다 (중복 선언 자체는 에러 아님). 메인 시큐리티 설정 파일을
 * 확인해서 중복이면 이 파일을 삭제하는 걸 권장.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}

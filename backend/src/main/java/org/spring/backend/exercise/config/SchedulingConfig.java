package org.spring.backend.exercise.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Scheduled 어노테이션이 실제로 동작하려면 애플리케이션 어딘가에
 * @EnableScheduling이 선언되어 있어야 한다.
 *
 * ⚠️ 정리 후보: 이미 메인 Application 클래스(@SpringBootApplication 붙은 곳)나 다른 패키지에
 * @EnableScheduling이 있다면 이 파일은 필요 없으니 지워도 된다 (중복 선언 자체는 에러 나지 않지만
 * 굳이 둘 필요는 없음). 또한 이 exercise 패키지 안에서 @Scheduled를 쓰는 곳은
 * 현재 전체 주석 처리된 ExerciseSyncScheduler뿐이라, 그 스케줄러 자체를 삭제하기로 하면
 * 이 설정 파일도 이 패키지에서는 필요 없어질 수 있음.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

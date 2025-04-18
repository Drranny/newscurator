package kr.ac.dankook.cs.curation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 뉴스 큐레이션 애플리케이션의 메인 클래스
 * - @SpringBootApplication: 컴포넌트 스캔, 설정, 자동 설정 활성화
 * - @EnableScheduling: 스케줄링 작업 (@Scheduled 메서드) 활성화
 */
@SpringBootApplication
@EnableScheduling // 스케줄링 기능 활성화 (@Scheduled 사용 가능하게 함)
public class CurationApplication {

    public static void main(String[] args) {
        // Spring Boot 애플리케이션 실행
        SpringApplication.run(CurationApplication.class, args);
    }
}

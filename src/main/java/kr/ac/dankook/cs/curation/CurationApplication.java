package kr.ac.dankook.cs.curation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 스케줄링 기능 활성화
public class CurationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurationApplication.class, args);
    }
}
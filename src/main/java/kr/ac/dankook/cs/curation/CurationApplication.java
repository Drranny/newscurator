package kr.ac.dankook.cs.curation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.Token;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@SpringBootApplication
@EnableScheduling // 스케줄링 기능 활성화 (@Scheduled 사용 가능하게 함)
public class CurationApplication {

  public static void main(String[] args) {
    // Spring Boot 애플리케이션 실행
    SpringApplication.run(CurationApplication.class, args);
  }
}

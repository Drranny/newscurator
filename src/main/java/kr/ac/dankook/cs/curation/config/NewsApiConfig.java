package kr.ac.dankook.cs.curation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 뉴스 API와 관련된 설정 정보를 제공하는 클래스
 * - RestTemplate 빈 등록
 * - application.properties 또는 yml에서 설정 값 주입
 */
@Component
@Configuration
@ConfigurationProperties(prefix = "newsapi")
public class NewsApiConfig {

    /**
     * RestTemplate 빈 등록
     * - 외부 API 호출 시 사용하는 Spring의 HTTP 클라이언트 도구
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // application.properties 또는 application.yml에서 주입받는 설정 값들
    private String key;
    private String url;
    private int views = 0;

    public String getApiKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getApiUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFromDate() {
        // 15일 전 날짜를 반환
        LocalDate now = LocalDate.now();
        return now.minusDays(15).format(DateTimeFormatter.ISO_DATE);
    }

    public String getToDate() {
        // 어제 날짜를 반환 (당일 제외)
        LocalDate now = LocalDate.now();
        return now.minusDays(1).format(DateTimeFormatter.ISO_DATE);
    }
}

package kr.ac.dankook.cs.curation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 뉴스 API와 관련된 설정 정보를 제공하는 클래스
 * - RestTemplate 빈 등록
 * - application.properties 또는 yml에서 설정 값 주입
 */
@Configuration  // 이 클래스가 설정 클래스임을 Spring에게 알림
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

    @Value("${newsapi.date.from}")
    private String fromDate; // 뉴스 검색 시작 날짜

    @Value("${newsapi.date.to}")
    private String toDate; // 뉴스 검색 종료 날짜

    @Value("${newsapi.url}")
    private String apiUrl; // 뉴스 API의 기본 URL

    @Value("${newsapi.key}")
    private String apiKey; // 뉴스 API 키

    // Getter 메서드들 - 다른 클래스에서 설정 값 접근 가능하게 함

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}

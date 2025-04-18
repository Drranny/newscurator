package kr.ac.dankook.cs.curation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.dankook.cs.curation.config.NewsApiConfig;
import kr.ac.dankook.cs.curation.entity.RecommendedArticle;
import kr.ac.dankook.cs.curation.repository.RecommendedArticleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 외부 뉴스 API에서 AI 관련 뉴스를 수집하고,
 * 중복되지 않은 기사만 추천 목록 DB에 저장하는 서비스 클래스
 */
@Service
public class NewsApiService {

    private final RestTemplate restTemplate;
    private final NewsApiConfig newsApiConfig;
    private final ObjectMapper objectMapper;
    private final RecommendedArticleRepository recommendedArticleRepository;

    @Autowired
    public NewsApiService(RestTemplate restTemplate,
                          NewsApiConfig newsApiConfig,
                          ObjectMapper objectMapper,
                          RecommendedArticleRepository recommendedArticleRepository) {
        this.restTemplate = restTemplate;
        this.newsApiConfig = newsApiConfig;
        this.objectMapper = objectMapper;
        this.recommendedArticleRepository = recommendedArticleRepository;
    }

    /**
     * NewsAPI로부터 AI 관련 한국어 뉴스를 수집하여 저장
     * @return 저장된 기사 리스트
     */
    public List<RecommendedArticle> fetchAiKoreanNews() {
        String apiUrl = newsApiConfig.getApiUrl();
        String apiKey = newsApiConfig.getApiKey();
        String query = "AI OR 인공지능"; // 검색 키워드
        String language = "ko";         // 한국어 뉴스만
        String from = newsApiConfig.getFromDate();
        String to = newsApiConfig.getToDate();

        // 한글 쿼리를 안전하게 인코딩
        query = URLEncoder.encode(query, StandardCharsets.UTF_8);

        // 최종 호출 URL 구성
        String fullUrl = String.format(
                "%s?q=%s&language=%s&from=%s&to=%s&sortBy=publishedAt&pageSize=100&apiKey=%s",
                apiUrl, query, language, from, to, apiKey
        );

        System.out.println("호출 URL: " + fullUrl);

        // API 호출
        ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);
        String responseBody = response.getBody();

        List<RecommendedArticle> newArticles = new ArrayList<>();

        try {
            // JSON 파싱
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode articles = root.path("articles");

            System.out.println("NewsAPI 응답 원문:\n" + responseBody);
            System.out.println("articles 개수: " + articles.size());

            for (JsonNode article : articles) {
                RecommendedArticle recommendedArticle = new RecommendedArticle();

                // 필드 매핑
                recommendedArticle.setTitle(article.path("title").asText());
                recommendedArticle.setDescription(article.path("description").asText());
                recommendedArticle.setUrl(article.path("url").asText());
                recommendedArticle.setAuthor(article.path("author").asText());

                // 발행일 파싱
                String publishedAtStr = article.path("publishedAt").asText();
                if (!publishedAtStr.isEmpty()) {
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(publishedAtStr);
                    recommendedArticle.setPublishedAt(zonedDateTime.toLocalDateTime());
                }

                // source.name 값 처리
                JsonNode source = article.path("source");
                if (source != null) {
                    recommendedArticle.setSourceName(source.path("name").asText());
                }

                // 추천 메타 정보 설정
                recommendedArticle.setRecommendedAt(LocalDateTime.now());
                recommendedArticle.setReason("AI/인공지능 관련 뉴스");
                recommendedArticle.setCategory("AI/Technology");
                recommendedArticle.setKeywords("AI,인공지능,Technology");

                // 중복 URL 방지
                if (!recommendedArticleRepository.existsByUrl(recommendedArticle.getUrl())) {
                    newArticles.add(recommendedArticle);
                    System.out.println("저장 대상 뉴스: " + recommendedArticle.getTitle() + " | " + recommendedArticle.getUrl());
                }
            }

            System.out.println("저장 예정 기사 수: " + newArticles.size());

        } catch (IOException e) {
            e.printStackTrace(); // 파싱 오류 로그 출력
        }

        // 일괄 저장
        recommendedArticleRepository.saveAll(newArticles);
        return newArticles;
    }
}

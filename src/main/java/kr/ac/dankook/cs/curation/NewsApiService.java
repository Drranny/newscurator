package kr.ac.dankook.cs.curation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.dankook.cs.curation.config.NewsApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import kr.ac.dankook.cs.curation.model.RecommendedArticle;
import kr.ac.dankook.cs.curation.repository.RecommendedArticleRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class NewsApiService {

    private final RestTemplate restTemplate;
    private final NewsApiConfig newsApiConfig;
    private final ObjectMapper objectMapper;
    private final RecommendedArticleRepository recommendedArticleRepository; // 데이터베이스 연동

    @Autowired
    public NewsApiService(RestTemplate restTemplate, NewsApiConfig newsApiConfig, ObjectMapper objectMapper, RecommendedArticleRepository recommendedArticleRepository) {
        this.restTemplate = restTemplate;
        this.newsApiConfig = newsApiConfig;
        this.objectMapper = objectMapper;
        this.recommendedArticleRepository = recommendedArticleRepository;
    }

    public List<RecommendedArticle> fetchAiKoreanNews() {
        String apiUrl = newsApiConfig.getApiUrl();
        String apiKey = newsApiConfig.getApiKey();
        String query = "AI OR 인공지능";
        String language = "ko";
        
        // 날짜 범위 가져오기
        String from = newsApiConfig.getFromDate();
        String to = newsApiConfig.getToDate();

        // 날짜 범위 추가한 URL 생성
        String fullUrl = String.format("%s?q=%s&language=%s&from=%s&to=%s&apiKey=%s", apiUrl, query, language, from, to, apiKey);

        ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);
        String responseBody = response.getBody();

        List<RecommendedArticle> newArticles = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode articles = root.path("articles");

            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

            for (JsonNode article : articles) {
                RecommendedArticle recommendedArticle = new RecommendedArticle();
                recommendedArticle.setTitle(article.path("title").asText());
                recommendedArticle.setDescription(article.path("description").asText());
                recommendedArticle.setUrl(article.path("url").asText());
                recommendedArticle.setAuthor(article.path("author").asText());
                
                // 날짜 파싱
                String publishedAtStr = article.path("publishedAt").asText();
                if (!publishedAtStr.isEmpty()) {
                    recommendedArticle.setPublishedAt(LocalDateTime.parse(publishedAtStr, formatter));
                }
                
                JsonNode source = article.path("source");
                if (source != null) {
                    recommendedArticle.setSourceName(source.path("name").asText());
                }

                // Set recommendedAt field (required)
                recommendedArticle.setRecommendedAt(LocalDateTime.now());
                
                // AI 관련 기사라는 이유 설정
                recommendedArticle.setReason("AI/인공지능 관련 뉴스");
                recommendedArticle.setCategory("AI/Technology");
                recommendedArticle.setKeywords("AI,인공지능,Technology");

                // 새로운 추천 기사 리스트에 추가
                newArticles.add(recommendedArticle);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 데이터베이스에 저장
        recommendedArticleRepository.saveAll(newArticles);

        return newArticles;
    }
}

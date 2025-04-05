package kr.ac.dankook.cs.curation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import kr.ac.dankook.cs.curation.model.NewsArticle;

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
    private final NewsArticleRepository newsArticleRepository; // 데이터베이스 연동 시

    @Autowired
    public NewsApiService(RestTemplate restTemplate, NewsApiConfig newsApiConfig, ObjectMapper objectMapper, NewsArticleRepository newsArticleRepository) {
        this.restTemplate = restTemplate;
        this.newsApiConfig = newsApiConfig;
        this.objectMapper = objectMapper;
        this.newsArticleRepository = newsArticleRepository;
    }

    public List<NewsArticle> fetchAiKoreanNews() {
        String apiUrl = newsApiConfig.getApiUrl();
        String apiKey = newsApiConfig.getApiKey();
        String query = "AI OR 인공지능";
        String language = "ko";

        String fullUrl = String.format("%s?q=%s&language=%s&apiKey=%s", apiUrl, query, language, apiKey);

        ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);
        String responseBody = response.getBody();

        List<NewsArticle> newArticles = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode articles = root.path("articles");

            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

            for (JsonNode article : articles) {
                NewsArticle newsArticle = new NewsArticle();
                newsArticle.setTitle(article.path("title").asText());
                newsArticle.setDescription(article.path("description").asText());
                newsArticle.setUrl(article.path("url").asText());
                newsArticle.setAuthor(article.path("author").asText());
                String publishedAtStr = article.path("publishedAt").asText();
                if (!publishedAtStr.isEmpty()) {
                    newsArticle.setPublishedAt(LocalDateTime.parse(publishedAtStr, formatter));
                }
                JsonNode source = article.path("source");
                if (source != null) {
                    newsArticle.setSourceName(source.path("name").asText());
                }
                newArticles.add(newsArticle);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 데이터베이스 연동 시 저장 로직 추가
        // newsArticleRepository.saveAll(newArticles);

        return newArticles;
    }
}
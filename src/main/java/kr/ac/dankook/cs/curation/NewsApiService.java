package kr.ac.dankook.cs.curation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.dankook.cs.curation.config.NewsApiConfig;
import kr.ac.dankook.cs.curation.entity.AiArticle;
import kr.ac.dankook.cs.curation.entity.BigdataArticle;
import kr.ac.dankook.cs.curation.entity.SecurityArticle;
import kr.ac.dankook.cs.curation.entity.HardwareArticle;
import kr.ac.dankook.cs.curation.repository.AiArticleRepository;
import kr.ac.dankook.cs.curation.repository.BigdataArticleRepository;
import kr.ac.dankook.cs.curation.repository.SecurityArticleRepository;
import kr.ac.dankook.cs.curation.repository.HardwareArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 외부 뉴스 API에서 다양한 키워드로 뉴스를 수집하고,
 * 중복되지 않은 기사만 DB에 저장하는 서비스 클래스
 */
@Service
public class NewsApiService {

    private static final Logger log = LoggerFactory.getLogger(NewsApiService.class);

    private final RestTemplate restTemplate;
    private final NewsApiConfig config;
    private final ObjectMapper mapper;
    private final AiArticleRepository aiRepo;
    private final BigdataArticleRepository bdRepo;
    private final SecurityArticleRepository secRepo;
    private final HardwareArticleRepository hwRepo;

    @Autowired
    public NewsApiService(RestTemplate restTemplate,
                          NewsApiConfig config,
                          ObjectMapper mapper,
                          AiArticleRepository aiRepo,
                          BigdataArticleRepository bdRepo,
                          SecurityArticleRepository secRepo,
                          HardwareArticleRepository hwRepo) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.mapper = mapper;
        this.aiRepo = aiRepo;
        this.bdRepo = bdRepo;
        this.secRepo = secRepo;
        this.hwRepo = hwRepo;
    }

    public List<AiArticle> fetchAiKoreanNews() {
        try {
            return fetchByKeyword("AI OR 인공지능", aiRepo, AiArticle::new);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch AI news", e);
        }
    }

    public List<BigdataArticle> fetchBigdataKoreanNews() {
        try {
            return fetchByKeyword("빅데이터 OR \"big data\"", bdRepo, BigdataArticle::new);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch Bigdata news", e);
        }
    }

    public List<SecurityArticle> fetchSecurityKoreanNews() {
        try {
            return fetchByKeyword("보안 OR security", secRepo, SecurityArticle::new);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch Security news", e);
        }
    }

    public List<HardwareArticle> fetchHardwareKoreanNews() {
        try {
            return fetchByKeyword("하드웨어 OR hardware", hwRepo, HardwareArticle::new);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch Hardware news", e);
        }
    }

    private <T> List<T> fetchByKeyword(
            String keyword,
            org.springframework.data.jpa.repository.JpaRepository<T, Long> repo,
            java.util.function.Supplier<T> supplier
    ) throws IOException {
        String url = String.format(
            "%s?q=%s&language=ko&from=%s&to=%s&sortBy=publishedAt&pageSize=100&apiKey=%s",
            config.getApiUrl(),
            URLEncoder.encode(keyword, StandardCharsets.UTF_8),
            config.getFromDate(),
            config.getToDate(),
            config.getApiKey()
        );
        JsonNode articles = mapper.readTree(restTemplate.getForObject(url, String.class))
                                  .path("articles");

        log.info("FetchByKeyword 키워드='{}' → 기사 건수 = {}", keyword, articles.size());

        List<T> list = new ArrayList<>();
        for (JsonNode n : articles) {
            T entity = supplier.get();
            if (entity instanceof AiArticle) {
                AiArticle a = (AiArticle) entity;
                a.setTitle(n.path("title").asText());
                a.setDescription(n.path("description").asText());
                a.setUrl(n.path("url").asText());
                a.setAuthor(n.path("author").asText(null));
                String p = n.path("publishedAt").asText();
                if (!p.isEmpty()) a.setPublishedAt(ZonedDateTime.parse(p).toLocalDateTime());
                a.setRecommendedAt(LocalDateTime.now());
                a.setCategory(keyword);
                if (!aiRepo.existsByUrl(a.getUrl())) list.add(entity);
            } else if (entity instanceof BigdataArticle) {
                BigdataArticle b = (BigdataArticle) entity;
                b.setTitle(n.path("title").asText());
                b.setDescription(n.path("description").asText());
                b.setUrl(n.path("url").asText());
                b.setAuthor(n.path("author").asText(null));
                String p = n.path("publishedAt").asText();
                if (!p.isEmpty()) b.setPublishedAt(ZonedDateTime.parse(p).toLocalDateTime());
                b.setRecommendedAt(LocalDateTime.now());
                b.setCategory(keyword);
                if (!bdRepo.existsByUrl(b.getUrl())) list.add(entity);
            } else if (entity instanceof SecurityArticle) {
                SecurityArticle s = (SecurityArticle) entity;
                s.setTitle(n.path("title").asText());
                s.setDescription(n.path("description").asText());
                s.setUrl(n.path("url").asText());
                s.setAuthor(n.path("author").asText(null));
                String p = n.path("publishedAt").asText();
                if (!p.isEmpty()) s.setPublishedAt(ZonedDateTime.parse(p).toLocalDateTime());
                s.setRecommendedAt(LocalDateTime.now());
                s.setCategory(keyword);
                if (!secRepo.existsByUrl(s.getUrl())) list.add(entity);
            } else if (entity instanceof HardwareArticle) {
                HardwareArticle h = (HardwareArticle) entity;
                h.setTitle(n.path("title").asText());
                h.setDescription(n.path("description").asText());
                h.setUrl(n.path("url").asText());
                h.setAuthor(n.path("author").asText(null));
                String p = n.path("publishedAt").asText();
                if (!p.isEmpty()) h.setPublishedAt(ZonedDateTime.parse(p).toLocalDateTime());
                h.setRecommendedAt(LocalDateTime.now());
                h.setCategory(keyword);
                if (!hwRepo.existsByUrl(h.getUrl())) list.add(entity);
            }
        }
        return repo.saveAll(list);
    }

    public void fetchAllCategories() {
        try {
            fetchByKeyword("AI OR 인공지능", aiRepo, AiArticle::new);
            fetchByKeyword("빅데이터 OR \"big data\"", bdRepo, BigdataArticle::new);
            fetchByKeyword("보안 OR security", secRepo, SecurityArticle::new);
            fetchByKeyword("하드웨어 OR hardware", hwRepo, HardwareArticle::new);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch news categories", e);
        }
    }
}

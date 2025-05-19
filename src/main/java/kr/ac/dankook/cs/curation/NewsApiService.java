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
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.model.Token;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);

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
            return fetchByKeyword("artificial intelligence OR AI OR 인공지능", aiRepo, AiArticle::new);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch AI news", e);
        }
    }

    public List<BigdataArticle> fetchBigdataKoreanNews() {
        try {
            return fetchByKeyword("big data OR 빅데이터", bdRepo, BigdataArticle::new);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch Bigdata news", e);
        }
    }

    public List<SecurityArticle> fetchSecurityKoreanNews() {
        try {
            return fetchByKeyword("security OR cyber OR 보안", secRepo, SecurityArticle::new);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch Security news", e);
        }
    }

    public List<HardwareArticle> fetchHardwareKoreanNews() {
        try {
            return fetchByKeyword("hardware OR 하드웨어", hwRepo, HardwareArticle::new);
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
            "%s?q=%s&from=%s&to=%s&sortBy=relevancy&pageSize=100&language=ko&apiKey=%s",
            config.getApiUrl(),
            URLEncoder.encode(keyword, StandardCharsets.UTF_8),
            config.getFromDate(),
            config.getToDate(),
            config.getApiKey()
        );
        log.info("API Request URL: {}", url);
        String response = restTemplate.getForObject(url, String.class);
        log.info("API Response: {}", response);
        JsonNode root = mapper.readTree(response);
        if (root.has("status") && !"ok".equals(root.get("status").asText())) {
            log.error("API Error - Code: {}, Message: {}",
                root.path("code").asText(),
                root.path("message").asText());
            throw new RuntimeException("NewsAPI error: " + root.path("message").asText());
        }
        JsonNode articles = root.path("articles");

        log.info("FetchByKeyword 키워드='{}', 검색기간={} ~ {}, 기사 건수={}",
            keyword,
            config.getFromDate(),
            config.getToDate(),
            articles.size());

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
                if (a.getUrl() != null && a.getUrl().length() <= 255 && !aiRepo.existsByUrl(a.getUrl())) list.add(entity);
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
                if (b.getUrl() != null && b.getUrl().length() <= 255 && !bdRepo.existsByUrl(b.getUrl())) list.add(entity);
            } else if (entity instanceof SecurityArticle) {
                SecurityArticle s = (SecurityArticle) entity;
                s.setTitle(n.path("title").asText());
                s.setDescription(n.path("description").asText());
                s.setUrl(n.path("url").asText());
                s.setUrlOriginal(n.path("url").asText());
                s.setAuthor(n.path("author").asText(null));
                String p = n.path("publishedAt").asText();
                if (!p.isEmpty()) s.setPublishedAt(ZonedDateTime.parse(p).toLocalDateTime());
                s.setRecommendedAt(LocalDateTime.now());
                s.setCategory(keyword);
                if (s.getUrl() != null && s.getUrl().length() <= 255 && !secRepo.existsByUrl(s.getUrl())) list.add(entity);
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
                if (h.getUrl() != null && h.getUrl().length() <= 255 && !hwRepo.existsByUrl(h.getUrl())) list.add(entity);
            }
        }
        return repo.saveAll(list);
    }

    public void fetchAllCategories() {
        try {
            fetchByKeyword("artificial intelligence OR AI OR 인공지능", aiRepo, AiArticle::new);
            fetchByKeyword("big data OR 빅데이터", bdRepo, BigdataArticle::new);
            fetchByKeyword("cybersecurity OR security OR 보안", secRepo, SecurityArticle::new);
            fetchByKeyword("hardware OR 하드웨어", hwRepo, HardwareArticle::new);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch news categories", e);
        }
    }

    // 한글 포함 여부 체크 함수
    private boolean containsKorean(String text) {
        return text != null && text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
    }

    /**
     * 기존 데이터베이스의 기사들에 키워드 값을 업데이트하는 메서드
     */
    public void updateExistingKeywords() {
        // AI 기사 업데이트
        List<AiArticle> aiArticles = aiRepo.findAll();
        for (AiArticle article : aiArticles) {
            if (article.getKeywords() == null) {
                article.setKeywords(article.getCategory());
                aiRepo.save(article);
            }
        }

        // 빅데이터 기사 업데이트
        List<BigdataArticle> bdArticles = bdRepo.findAll();
        for (BigdataArticle article : bdArticles) {
            if (article.getKeywords() == null) {
                article.setKeywords(article.getCategory());
                bdRepo.save(article);
            }
        }

        // 보안 기사 업데이트
        List<SecurityArticle> secArticles = secRepo.findAll();
        for (SecurityArticle article : secArticles) {
            if (article.getKeywords() == null) {
                article.setKeywords(article.getCategory());
                secRepo.save(article);
            }
        }

        // 하드웨어 기사 업데이트
        List<HardwareArticle> hwArticles = hwRepo.findAll();
        for (HardwareArticle article : hwArticles) {
            if (article.getKeywords() == null) {
                article.setKeywords(article.getCategory());
                hwRepo.save(article);
            }
        }
    }

    /**
     * Komoran을 사용해 모든 기사에 대해 title+description에서 명사 키워드를 추출하여 keywords 필드에 저장
     */
    public void extractAndSaveKeywordsForAllArticles() {
        // AI 기사
        List<AiArticle> aiArticles = aiRepo.findAll();
        for (AiArticle article : aiArticles) {
            String text = (article.getTitle() == null ? "" : article.getTitle()) + " " +
                    (article.getDescription() == null ? "" : article.getDescription());
            Set<String> keywords = komoran.analyze(text).getTokenList().stream()
                    .filter(t -> t.getPos().startsWith("NN"))
                    .map(Token::getMorph)
                    .collect(Collectors.toSet());
            String keywordStr = keywords.stream().limit(5).collect(Collectors.joining(","));
            article.setKeywords(keywordStr);
            aiRepo.save(article);
        }
        // 빅데이터 기사
        List<BigdataArticle> bdArticles = bdRepo.findAll();
        for (BigdataArticle article : bdArticles) {
            String text = (article.getTitle() == null ? "" : article.getTitle()) + " " +
                    (article.getDescription() == null ? "" : article.getDescription());
            Set<String> keywords = komoran.analyze(text).getTokenList().stream()
                    .filter(t -> t.getPos().startsWith("NN"))
                    .map(Token::getMorph)
                    .collect(Collectors.toSet());
            String keywordStr = keywords.stream().limit(5).collect(Collectors.joining(","));
            article.setKeywords(keywordStr);
            bdRepo.save(article);
        }
        // 보안 기사
        List<SecurityArticle> secArticles = secRepo.findAll();
        for (SecurityArticle article : secArticles) {
            String text = (article.getTitle() == null ? "" : article.getTitle()) + " " +
                    (article.getDescription() == null ? "" : article.getDescription());
            Set<String> keywords = komoran.analyze(text).getTokenList().stream()
                    .filter(t -> t.getPos().startsWith("NN"))
                    .map(Token::getMorph)
                    .collect(Collectors.toSet());
            String keywordStr = keywords.stream().limit(5).collect(Collectors.joining(","));
            article.setKeywords(keywordStr);
            secRepo.save(article);
        }
        // 하드웨어 기사
        List<HardwareArticle> hwArticles = hwRepo.findAll();
        for (HardwareArticle article : hwArticles) {
            String text = (article.getTitle() == null ? "" : article.getTitle()) + " " +
                    (article.getDescription() == null ? "" : article.getDescription());
            Set<String> keywords = komoran.analyze(text).getTokenList().stream()
                    .filter(t -> t.getPos().startsWith("NN"))
                    .map(Token::getMorph)
                    .collect(Collectors.toSet());
            String keywordStr = keywords.stream().limit(5).collect(Collectors.joining(","));
            article.setKeywords(keywordStr);
            hwRepo.save(article);
        }
    }
}

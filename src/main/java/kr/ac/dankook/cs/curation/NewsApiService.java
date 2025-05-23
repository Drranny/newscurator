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
import org.springframework.scheduling.annotation.Scheduled;
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
            return fetchByKeyword("hardware OR 하드웨어 OR CPU OR GPU OR 메모리 OR 반도체 OR 컴퓨터 OR 노트북 OR 서버 OR 마이크로프로세서 OR 마더보드 OR SSD OR HDD OR RAM OR ROM", hwRepo, HardwareArticle::new);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch Hardware news", e);
        }
    }

    private <T> List<T> fetchByKeyword(
            String keyword,
            org.springframework.data.jpa.repository.JpaRepository<T, Long> repo,
            java.util.function.Supplier<T> supplier
    ) throws IOException {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = String.format("%s?q=%s&from=%s&to=%s&language=ko&sortBy=publishedAt&apiKey=%s",
                config.getApiUrl(), encodedKeyword, config.getFromDate(), config.getToDate(), config.getApiKey());
        
        String response = restTemplate.getForObject(url, String.class);
        JsonNode root = mapper.readTree(response);
        JsonNode articles = root.path("articles");
        List<T> list = new ArrayList<>();
        String category = "";
        
        for (JsonNode n : articles) {
            T entity = supplier.get();
            if (entity instanceof AiArticle) {
                category = "AI";
                AiArticle a = (AiArticle) entity;
                a.setTitle(n.path("title").asText());
                a.setDescription(n.path("description").asText());
                a.setUrl(n.path("url").asText());
                a.setAuthor(n.path("author").asText(null));
                a.setUrlToImage(n.path("urlToImage").asText(null));
                String p = n.path("publishedAt").asText();
                if (!p.isEmpty()) a.setPublishedAt(ZonedDateTime.parse(p).toLocalDateTime());
                a.setRecommendedAt(LocalDateTime.now());
                a.setCategory(keyword);
                
                // 새로운 기사에 대해 키워드 추출
                String text = a.getTitle() + " " + (a.getDescription() != null ? a.getDescription() : "");
                Set<String> keywords = komoran.analyze(text).getTokenList().stream()
                        .filter(t -> t.getPos().startsWith("NN"))
                        .map(Token::getMorph)
                        .collect(Collectors.toSet());
                String keywordStr = keywords.stream().limit(5).collect(Collectors.joining(","));
                a.setKeywords(keywordStr);
                
                if (a.getUrl() != null && a.getUrl().length() <= 255 && !aiRepo.existsByUrl(a.getUrl())) list.add(entity);
            } else if (entity instanceof BigdataArticle) {
                category = "빅데이터";
                BigdataArticle b = (BigdataArticle) entity;
                b.setTitle(n.path("title").asText());
                b.setDescription(n.path("description").asText());
                b.setUrl(n.path("url").asText());
                b.setAuthor(n.path("author").asText(null));
                b.setUrlToImage(n.path("urlToImage").asText(null));
                String p = n.path("publishedAt").asText();
                if (!p.isEmpty()) b.setPublishedAt(ZonedDateTime.parse(p).toLocalDateTime());
                b.setRecommendedAt(LocalDateTime.now());
                b.setCategory(keyword);
                
                // 새로운 기사에 대해 키워드 추출
                String text = b.getTitle() + " " + (b.getDescription() != null ? b.getDescription() : "");
                Set<String> keywords = komoran.analyze(text).getTokenList().stream()
                        .filter(t -> t.getPos().startsWith("NN"))
                        .map(Token::getMorph)
                        .collect(Collectors.toSet());
                String keywordStr = keywords.stream().limit(5).collect(Collectors.joining(","));
                b.setKeywords(keywordStr);
                
                if (b.getUrl() != null && b.getUrl().length() <= 255 && !bdRepo.existsByUrl(b.getUrl())) list.add(entity);
            } else if (entity instanceof SecurityArticle) {
                category = "보안";
                SecurityArticle s = (SecurityArticle) entity;
                s.setTitle(n.path("title").asText());
                s.setDescription(n.path("description").asText());
                s.setUrl(n.path("url").asText());
                s.setUrlOriginal(n.path("url").asText());
                s.setAuthor(n.path("author").asText(null));
                s.setUrlToImage(n.path("urlToImage").asText(null));
                String p = n.path("publishedAt").asText();
                if (!p.isEmpty()) s.setPublishedAt(ZonedDateTime.parse(p).toLocalDateTime());
                s.setRecommendedAt(LocalDateTime.now());
                s.setCategory(keyword);
                
                // 새로운 기사에 대해 키워드 추출
                String text = s.getTitle() + " " + (s.getDescription() != null ? s.getDescription() : "");
                Set<String> keywords = komoran.analyze(text).getTokenList().stream()
                        .filter(t -> t.getPos().startsWith("NN"))
                        .map(Token::getMorph)
                        .collect(Collectors.toSet());
                String keywordStr = keywords.stream().limit(5).collect(Collectors.joining(","));
                s.setKeywords(keywordStr);
                
                if (s.getUrl() != null && s.getUrl().length() <= 255 && !secRepo.existsByUrl(s.getUrl())) list.add(entity);
            } else if (entity instanceof HardwareArticle) {
                category = "하드웨어";
                HardwareArticle h = (HardwareArticle) entity;
                h.setTitle(n.path("title").asText());
                h.setDescription(n.path("description").asText());
                h.setUrl(n.path("url").asText());
                h.setAuthor(n.path("author").asText(null));
                h.setUrlToImage(n.path("urlToImage").asText(null));
                String p = n.path("publishedAt").asText();
                if (!p.isEmpty()) h.setPublishedAt(ZonedDateTime.parse(p).toLocalDateTime());
                h.setRecommendedAt(LocalDateTime.now());
                h.setCategory(keyword);
                
                // 새로운 기사에 대해 키워드 추출
                String text = h.getTitle() + " " + (h.getDescription() != null ? h.getDescription() : "");
                Set<String> keywords = komoran.analyze(text).getTokenList().stream()
                        .filter(t -> t.getPos().startsWith("NN"))
                        .map(Token::getMorph)
                        .collect(Collectors.toSet());
                String keywordStr = keywords.stream().limit(5).collect(Collectors.joining(","));
                h.setKeywords(keywordStr);
                
                if (h.getUrl() != null && h.getUrl().length() <= 255 && !hwRepo.existsByUrl(h.getUrl())) list.add(entity);
            }
        }
        
        log.info("{} 카테고리 수집 결과: API 응답 기사 수={}, 중복 제외 후 저장된 기사 수={}", 
            category, articles.size(), list.size());
            
        return repo.saveAll(list);
    }

    @Scheduled(cron = "0 0 9 * * ?")  // 매일 오전 9시에 실행
    public void fetchAllCategories() {
        try {
            log.info("뉴스 수집 스케줄링 시작");
            // 30일 이상 지난 기사 삭제
            deleteOldArticles();
            // 새로운 기사 수집
            fetchByKeyword("artificial intelligence OR AI OR 인공지능", aiRepo, AiArticle::new);
            fetchByKeyword("big data OR 빅데이터", bdRepo, BigdataArticle::new);
            fetchByKeyword("cybersecurity OR security OR 보안", secRepo, SecurityArticle::new);
            fetchByKeyword("hardware OR 하드웨어 OR CPU OR GPU OR 메모리 OR 반도체 OR 컴퓨터 OR 노트북 OR 서버 OR 마이크로프로세서 OR 마더보드 OR SSD OR HDD OR RAM OR ROM", hwRepo, HardwareArticle::new);
            log.info("뉴스 수집 스케줄링 완료");
        } catch (IOException e) {
            log.error("뉴스 수집 중 오류 발생", e);
            throw new RuntimeException("Failed to fetch news categories", e);
        }
    }

    /**
     * 30일 이상 지난 기사를 자동으로 삭제하는 메서드
     */
    private void deleteOldArticles() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        log.info("30일 이상 지난 기사 삭제 시작 (기준일: {})", thirtyDaysAgo);

        // AI 기사 삭제
        List<AiArticle> oldAiArticles = aiRepo.findByPublishedAtBefore(thirtyDaysAgo);
        aiRepo.deleteAll(oldAiArticles);
        log.info("AI 기사 삭제 완료: {}건", oldAiArticles.size());

        // 빅데이터 기사 삭제
        List<BigdataArticle> oldBdArticles = bdRepo.findByPublishedAtBefore(thirtyDaysAgo);
        bdRepo.deleteAll(oldBdArticles);
        log.info("빅데이터 기사 삭제 완료: {}건", oldBdArticles.size());

        // 보안 기사 삭제
        List<SecurityArticle> oldSecArticles = secRepo.findByPublishedAtBefore(thirtyDaysAgo);
        secRepo.deleteAll(oldSecArticles);
        log.info("보안 기사 삭제 완료: {}건", oldSecArticles.size());

        // 하드웨어 기사 삭제
        List<HardwareArticle> oldHwArticles = hwRepo.findByPublishedAtBefore(thirtyDaysAgo);
        hwRepo.deleteAll(oldHwArticles);
        log.info("하드웨어 기사 삭제 완료: {}건", oldHwArticles.size());

        log.info("30일 이상 지난 기사 삭제 완료");
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
}

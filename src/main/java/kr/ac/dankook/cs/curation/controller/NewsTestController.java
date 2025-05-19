package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.NewsApiService;
import kr.ac.dankook.cs.curation.entity.AiArticle;
import kr.ac.dankook.cs.curation.entity.BigdataArticle;
import kr.ac.dankook.cs.curation.entity.SecurityArticle;
import kr.ac.dankook.cs.curation.entity.HardwareArticle;
import kr.ac.dankook.cs.curation.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 뉴스 API 테스트용 REST 컨트롤러
 * - 외부 API에서 각 카테고리별 뉴스를 가져오고 DB에 저장하는 기능 테스트 목적
 */
@Controller
@RequestMapping("/api")
public class NewsTestController {

    private final NewsApiService newsApiService;

    @Autowired
    private AiArticleService aiArticleService;
    
    @Autowired
    private BigdataArticleService bigdataArticleService;
    
    @Autowired
    private SecurityArticleService securityArticleService;
    
    @Autowired
    private HardwareArticleService hardwareArticleService;

    public NewsTestController(NewsApiService newsApiService) {
        this.newsApiService = newsApiService;
    }

    /**
     * GET /api/news/fetch/ai
     * AI 카테고리 뉴스 조회 및 저장
     */
    @GetMapping("/news/fetch/ai")
    public ResponseEntity<List<AiArticle>> fetchAi() {
        List<AiArticle> result = newsApiService.fetchAiKoreanNews();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/news/fetch/bigdata
     * 빅데이터 카테고리 뉴스 조회 및 저장
     */
    @GetMapping("/news/fetch/bigdata")
    public ResponseEntity<List<BigdataArticle>> fetchBigdata() {
        List<BigdataArticle> result = newsApiService.fetchBigdataKoreanNews();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/news/fetch/security
     * 보안 카테고리 뉴스 조회 및 저장
     */
    @GetMapping("/news/fetch/security")
    public ResponseEntity<List<SecurityArticle>> fetchSecurity() {
        List<SecurityArticle> result = newsApiService.fetchSecurityKoreanNews();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/news/fetch/hardware
     * 하드웨어 카테고리 뉴스 조회 및 저장
     */
    @GetMapping("/news/fetch/hardware")
    public ResponseEntity<List<HardwareArticle>> fetchHardware() {
        List<HardwareArticle> result = newsApiService.fetchHardwareKoreanNews();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api
     * 모든 카테고리의 뉴스를 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, List<?>>> getAllNews() {
        Map<String, List<?>> response = new HashMap<>();
        response.put("ai", aiArticleService.getAllArticles());
        response.put("bigdata", bigdataArticleService.getAllArticles());
        response.put("security", securityArticleService.getAllArticles());
        response.put("hardware", hardwareArticleService.getAllArticles());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public String getMainPage(Model model) {
        // 추천 뉴스 (각 카테고리별 최신 기사)
        List<AiArticle> aiNews = aiArticleService.getLatestArticles(3);
        List<BigdataArticle> bigdataNews = bigdataArticleService.getLatestArticles(3);
        List<SecurityArticle> securityNews = securityArticleService.getLatestArticles(3);
        List<HardwareArticle> hardwareNews = hardwareArticleService.getLatestArticles(3);
        
        // 모든 뉴스를 하나의 리스트로 합치기
        List<?> recommendedNews = List.of(
            aiNews.get(0), bigdataNews.get(0), securityNews.get(0), hardwareNews.get(0),
            aiNews.get(1), bigdataNews.get(1), securityNews.get(1), hardwareNews.get(1),
            aiNews.get(2), bigdataNews.get(2), securityNews.get(2), hardwareNews.get(2)
        );
        
        // 조회수 순위 뉴스
        List<?> topViewedNews = List.of(
            aiArticleService.getTopViewedArticles(1).get(0),
            bigdataArticleService.getTopViewedArticles(1).get(0),
            securityArticleService.getTopViewedArticles(1).get(0),
            hardwareArticleService.getTopViewedArticles(1).get(0)
        );
        
        model.addAttribute("recommendedNews", recommendedNews);
        model.addAttribute("topViewedNews", topViewedNews);
        
        return "index";
    }
}

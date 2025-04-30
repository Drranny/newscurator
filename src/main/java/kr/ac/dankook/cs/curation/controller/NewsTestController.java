package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.NewsApiService;
import kr.ac.dankook.cs.curation.entity.AiArticle;
import kr.ac.dankook.cs.curation.entity.BigdataArticle;
import kr.ac.dankook.cs.curation.entity.SecurityArticle;
import kr.ac.dankook.cs.curation.entity.HardwareArticle;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 뉴스 API 테스트용 REST 컨트롤러
 * - 외부 API에서 각 카테고리별 뉴스를 가져오고 DB에 저장하는 기능 테스트 목적
 */
@RestController
@RequestMapping("/api/news")
public class NewsTestController {

    private final NewsApiService newsApiService;

    public NewsTestController(NewsApiService newsApiService) {
        this.newsApiService = newsApiService;
    }

    /**
     * GET /api/news/fetch/ai
     * AI 카테고리 뉴스 조회 및 저장
     */
    @GetMapping("/fetch/ai")
    public ResponseEntity<List<AiArticle>> fetchAi() {
        List<AiArticle> result = newsApiService.fetchAiKoreanNews();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/news/fetch/bigdata
     * 빅데이터 카테고리 뉴스 조회 및 저장
     */
    @GetMapping("/fetch/bigdata")
    public ResponseEntity<List<BigdataArticle>> fetchBigdata() {
        List<BigdataArticle> result = newsApiService.fetchBigdataKoreanNews();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/news/fetch/security
     * 보안 카테고리 뉴스 조회 및 저장
     */
    @GetMapping("/fetch/security")
    public ResponseEntity<List<SecurityArticle>> fetchSecurity() {
        List<SecurityArticle> result = newsApiService.fetchSecurityKoreanNews();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/news/fetch/hardware
     * 하드웨어 카테고리 뉴스 조회 및 저장
     */
    @GetMapping("/fetch/hardware")
    public ResponseEntity<List<HardwareArticle>> fetchHardware() {
        List<HardwareArticle> result = newsApiService.fetchHardwareKoreanNews();
        return ResponseEntity.ok(result);
    }
}

package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.NewsApiService;
import kr.ac.dankook.cs.curation.entity.RecommendedArticle;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 뉴스 API 테스트용 REST 컨트롤러
 * - 실제 뉴스 데이터를 외부 API에서 가져오고 DB에 저장하는 기능 테스트 목적
 */
@RestController
@RequestMapping("/api/news") // 모든 엔드포인트는 /api/news로 시작
public class NewsTestController {

    private final NewsApiService newsApiService;

    // 생성자를 통해 NewsApiService 주입
    public NewsTestController(NewsApiService newsApiService) {
        this.newsApiService = newsApiService;
    }

    /**
     * GET /api/news/fetch
     * - 외부 AI 카테고리 뉴스 API 호출
     * - 가져온 기사 데이터를 DB에 저장 후 반환
     *
     * @return 저장된 RecommendedArticle 리스트
     */
    @GetMapping("/fetch")
    public ResponseEntity<List<RecommendedArticle>> fetchAndSave() {
        List<RecommendedArticle> result = newsApiService.fetchAiKoreanNews();
        return ResponseEntity.ok(result); // JSON 형태로 기사 리스트 반환
    }
}

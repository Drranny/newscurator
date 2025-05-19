package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.NewsApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fetch")
public class FetchController {

    private final NewsApiService newsApiService;

    public FetchController(NewsApiService newsApiService) {
        this.newsApiService = newsApiService;
    }

    /**
     * GET /api/fetch/all
     * 네 가지 키워드를 즉시 조회하여 DB에 저장
     */
    @GetMapping("/all")
    public ResponseEntity<String> fetchAllNow() {
        try {
            // fetchAllCategories 는 내부에서 AI, 빅데이터, 보안, 하드웨어를 순차 조회
            newsApiService.fetchAllCategories();
            return ResponseEntity.ok("모든 키워드 뉴스 즉시 조회 및 저장 완료");
        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("뉴스 조회 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * GET /api/fetch/update-keywords
     * 기존 기사들의 키워드 값을 업데이트
     */
    @GetMapping("/update-keywords")
    public ResponseEntity<String> updateExistingKeywords() {
        try {
            newsApiService.updateExistingKeywords();
            return ResponseEntity.ok("기존 기사들의 키워드 값 업데이트 완료");
        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("키워드 업데이트 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * GET /api/fetch/extract-keywords
     * Komoran으로 모든 기사에 대해 키워드 추출 및 저장
     */
    @GetMapping("/extract-keywords")
    public ResponseEntity<String> extractKeywords() {
        try {
            newsApiService.extractAndSaveKeywordsForAllArticles();
            return ResponseEntity.ok("모든 기사에 대해 키워드 추출 및 저장 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("키워드 추출 중 오류: " + e.getMessage());
        }
    }
}

package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.entity.AiArticle;
import kr.ac.dankook.cs.curation.service.AiArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 추천 뉴스 기사(RecommendedArticle)에 대한 REST API 컨트롤러
 * - CRUD 기능 제공
 */
@RestController
@RequestMapping("/api/recommended") // API 기본 경로: /api/recommended
public class AiArticleController {

    private final AiArticleService service;

    // 생성자 주입 방식
    public AiArticleController(AiArticleService service) {
        this.service = service;
    }

    /**
     * POST /api/recommended
     * 새로운 기사 저장 (중복 시 저장 안 함)
     * @param article 요청 본문에 포함된 기사 객체
     * @return 저장된 기사 정보 반환
     */
    @PostMapping
    public ResponseEntity<AiArticle> addArticle(@RequestBody AiArticle article) {
        AiArticle saved = service.saveIfNotExists(article);
        return ResponseEntity.ok(saved); // HTTP 200 + 저장된 객체
    }

    /**
     * GET /api/recommended
     * 모든 기사 목록 조회
     * @return 기사 리스트 반환
     */
    @GetMapping
    public ResponseEntity<List<AiArticle>> getAllArticles() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * GET /api/recommended/{id}
     * 특정 ID의 기사 조회
     * @param id 조회할 기사 ID
     * @return 존재 시 기사 반환, 없으면 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<AiArticle> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()); // 404 처리
    }

    /**
     * DELETE /api/recommended/{id}
     * 특정 ID의 기사 삭제
     * @param id 삭제할 기사 ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build(); // 삭제 성공 시 204 반환
    }
}

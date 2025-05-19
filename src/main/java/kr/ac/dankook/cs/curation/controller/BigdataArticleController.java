package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.entity.BigdataArticle;
import kr.ac.dankook.cs.curation.service.BigdataArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 빅데이터 뉴스 기사(BigdataArticle)에 대한 REST API 컨트롤러
 * - CRUD 기능 제공
 */
@RestController
@RequestMapping("/api/bigdata")
public class BigdataArticleController {

    private final BigdataArticleService service;

    // 생성자 주입
    public BigdataArticleController(BigdataArticleService service) {
        this.service = service;
    }

    /**
     * POST /api/bigdata
     * 새로운 빅데이터 기사 저장 (중복 시 저장 안 함)
     * @param article 요청 본문에 포함된 BigdataArticle 객체
     * @return 저장된 기사 정보 반환
     */
    @PostMapping
    public ResponseEntity<BigdataArticle> addArticle(@RequestBody BigdataArticle article) {
        BigdataArticle saved = service.saveIfNotExists(article);
        return ResponseEntity.ok(saved);
    }

    /**
     * GET /api/bigdata
     * 모든 빅데이터 기사 목록 조회
     * @return 기사 리스트 반환
     */
    @GetMapping
    public ResponseEntity<List<BigdataArticle>> getAllArticles() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * GET /api/bigdata/{id}
     * 특정 ID의 기사 조회
     * @param id 조회할 기사 ID
     * @return 존재 시 기사 반환, 없으면 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<BigdataArticle> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/bigdata/{id}
     * 특정 ID의 기사 삭제
     * @param id 삭제할 기사 ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

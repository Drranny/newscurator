package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.entity.SecurityArticle;
import kr.ac.dankook.cs.curation.service.SecurityArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 보안 뉴스 기사(SecurityArticle)에 대한 REST API 컨트롤러
 * - CRUD 및 키워드 검색 기능 제공
 */
@RestController
@RequestMapping("/api/security")
public class SecurityArticleController {

    private final SecurityArticleService service;

    // 생성자 주입
    public SecurityArticleController(SecurityArticleService service) {
        this.service = service;
    }

    /**
     * POST /api/security
     * 새로운 보안 기사 저장 (중복 URL인 경우 저장하지 않음)
     * @param article 요청 본문에 포함된 SecurityArticle 객체
     * @return 저장된 SecurityArticle 객체
     */
    @PostMapping
    public ResponseEntity<SecurityArticle> addArticle(@RequestBody SecurityArticle article) {
        SecurityArticle saved = service.saveIfNotExists(article);
        return ResponseEntity.ok(saved);
    }

    /**
     * GET /api/security
     * 모든 보안 기사 목록 조회
     * @return SecurityArticle 리스트
     */
    @GetMapping
    public ResponseEntity<List<SecurityArticle>> getAllArticles() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * GET /api/security/{id}
     * 특정 ID의 보안 기사 조회
     * @param id 조회할 기사 ID
     * @return 존재 시 해당 SecurityArticle, 없으면 404 반환
     */
    @GetMapping("/{id}")
    public ResponseEntity<SecurityArticle> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/security/{id}
     * 특정 ID의 보안 기사 삭제
     * @param id 삭제할 기사 ID
     * @return 삭제 성공 시 HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/security/search?keyword={keyword}
     * 제목에 키워드가 포함된 보안 기사 검색 (대소문자 무시)
     * @param keyword 검색할 키워드
     * @return 매칭된 SecurityArticle 리스트
     */
    @GetMapping("/search")
    public ResponseEntity<List<SecurityArticle>> searchByTitle(@RequestParam String keyword) {
        List<SecurityArticle> list = service.findByTitleContainingIgnoreCase(keyword);
        return ResponseEntity.ok(list);
    }
}

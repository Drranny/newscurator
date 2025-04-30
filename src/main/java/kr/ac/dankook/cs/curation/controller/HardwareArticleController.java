package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.entity.HardwareArticle;
import kr.ac.dankook.cs.curation.service.HardwareArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 하드웨어 뉴스 기사(HardwareArticle)에 대한 REST API 컨트롤러
 * - CRUD 및 키워드 검색 기능 제공
 */
@RestController
@RequestMapping("/api/hardware")
public class HardwareArticleController {

    private final HardwareArticleService service;

    // 생성자 주입
    public HardwareArticleController(HardwareArticleService service) {
        this.service = service;
    }

    /**
     * POST /api/hardware
     * 새로운 하드웨어 기사 저장 (중복 URL인 경우 저장하지 않음)
     * @param article 요청 본문에 포함된 HardwareArticle 객체
     * @return 저장된 HardwareArticle 객체
     */
    @PostMapping
    public ResponseEntity<HardwareArticle> addArticle(@RequestBody HardwareArticle article) {
        HardwareArticle saved = service.saveIfNotExists(article);
        return ResponseEntity.ok(saved);
    }

    /**
     * GET /api/hardware
     * 모든 하드웨어 기사 목록 조회
     * @return HardwareArticle 리스트
     */
    @GetMapping
    public ResponseEntity<List<HardwareArticle>> getAllArticles() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * GET /api/hardware/{id}
     * 특정 ID의 하드웨어 기사 조회
     * @param id 조회할 기사 ID
     * @return 존재 시 해당 HardwareArticle, 없으면 404 반환
     */
    @GetMapping("/{id}")
    public ResponseEntity<HardwareArticle> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/hardware/{id}
     * 특정 ID의 하드웨어 기사 삭제
     * @param id 삭제할 기사 ID
     * @return 삭제 성공 시 HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/hardware/search?keyword={keyword}
     * 제목에 키워드가 포함된 하드웨어 기사 검색 (대소문자 무시)
     * @param keyword 검색할 키워드
     * @return 매칭된 HardwareArticle 리스트
     */
    @GetMapping("/search")
    public ResponseEntity<List<HardwareArticle>> searchByTitle(@RequestParam String keyword) {
        List<HardwareArticle> list = service.findByTitleContainingIgnoreCase(keyword);
        return ResponseEntity.ok(list);
    }
}

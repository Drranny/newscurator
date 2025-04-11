package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.entity.RecommendedArticle;
import kr.ac.dankook.cs.curation.service.RecommendedArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommended")
public class RecommendedArticleController {

    private final RecommendedArticleService service;

    public RecommendedArticleController(RecommendedArticleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RecommendedArticle> addArticle(@RequestBody RecommendedArticle article) {
        RecommendedArticle saved = service.saveIfNotExists(article);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<RecommendedArticle>> getAllArticles() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecommendedArticle> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

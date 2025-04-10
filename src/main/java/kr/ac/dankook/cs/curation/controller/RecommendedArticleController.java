package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.model.RecommendedArticle;
import kr.ac.dankook.cs.curation.service.RecommendedArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class RecommendedArticleController {

    private final RecommendedArticleService service;

    @Autowired
    public RecommendedArticleController(RecommendedArticleService service) {
        this.service = service;
    }

    // 모든 추천 기사 가져오기
    @GetMapping
    public List<RecommendedArticle> getAllArticles() {
        return service.getAllRecommendedArticles();
    }

    // 기사 저장
    @PostMapping
    public RecommendedArticle addArticle(@RequestBody RecommendedArticle article) {
        return service.saveRecommendedArticle(article);
    }

    // 특정 ID의 기사 가져오기
    @GetMapping("/{id}")
    public RecommendedArticle getArticle(@PathVariable Long id) {
        return service.getArticleById(id).orElse(null);
    }
}

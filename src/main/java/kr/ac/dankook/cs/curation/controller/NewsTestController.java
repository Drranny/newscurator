package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.NewsApiService;
import kr.ac.dankook.cs.curation.entity.RecommendedArticle;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsTestController {

    private final NewsApiService newsApiService;

    public NewsTestController(NewsApiService newsApiService) {
        this.newsApiService = newsApiService;
    }

    @GetMapping("/fetch")
    public ResponseEntity<List<RecommendedArticle>> fetchAndSave() {
        List<RecommendedArticle> result = newsApiService.fetchAiKoreanNews();
        return ResponseEntity.ok(result);
    }
}

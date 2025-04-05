package kr.ac.dankook.cs.curation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import kr.ac.dankook.cs.curation.model.NewsArticle;
import kr.ac.dankook.cs.curation.NewsArticleRepository;
import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsArticleRepository newsArticleRepository;

    @Autowired
    public NewsController(NewsArticleRepository newsArticleRepository) {
        this.newsArticleRepository = newsArticleRepository;
    }

    @GetMapping("/ai/korean")
    public List<NewsArticle> getAiKoreanNews() {
        return newsArticleRepository.findAll(); // 또는 필요한 필터링 로직 추가
    }
}
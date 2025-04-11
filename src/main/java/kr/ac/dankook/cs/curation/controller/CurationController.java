package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.model.RecommendedArticle;
import kr.ac.dankook.cs.curation.repository.RecommendedArticleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class CurationController {

    private final RecommendedArticleRepository recommendedArticleRepository;

    public CurationController(RecommendedArticleRepository recommendedArticleRepository) {
        this.recommendedArticleRepository = recommendedArticleRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/articles/ai")
    public String showAiArticles(Model model) {
        List<RecommendedArticle> aiArticles = recommendedArticleRepository.findByTitleContainingIgnoreCase("AI");
        model.addAttribute("articles", aiArticles);
        return "keywords"; // keywords.html 템플릿으로 이동
    }

    @GetMapping("/articles/{id}")
    public String showArticleDetail(@PathVariable Long id, Model model) {
        RecommendedArticle article = recommendedArticleRepository.findById(id).orElseThrow();
        model.addAttribute("article", article);
        return "detail"; // templates/detail.html
    }
}

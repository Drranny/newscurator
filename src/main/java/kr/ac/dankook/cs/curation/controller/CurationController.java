package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.entity.RecommendedArticle;
import kr.ac.dankook.cs.curation.repository.RecommendedArticleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CurationController {

    private final RecommendedArticleRepository recommendedArticleRepository;

    public CurationController(RecommendedArticleRepository recommendedArticleRepository) {
        this.recommendedArticleRepository = recommendedArticleRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index"; // templates/index.html 렌더링
    }

    @GetMapping("/articles/search")
    public String searchArticles(@RequestParam String keyword, Model model) {
        List<RecommendedArticle> articles = recommendedArticleRepository
                .findByTitleContainingIgnoreCase(keyword);
        model.addAttribute("articles", articles);
        model.addAttribute("keyword", keyword);
        return "keywords";
    }


    @GetMapping("/articles/{id}")
    public String showArticleDetail(@PathVariable Long id, Model model) {
        return recommendedArticleRepository.findById(id)
            .map(article -> {
                model.addAttribute("article", article);
                return "detail";
            })
            .orElse("not-found"); // templates/not-found.html
    }

}

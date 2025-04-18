package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.entity.RecommendedArticle;
import kr.ac.dankook.cs.curation.repository.RecommendedArticleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 뉴스 큐레이션 기능을 제공하는 웹 컨트롤러
 * - 메인 화면, 키워드 검색, 뉴스 상세 보기 기능을 제공
 */
@Controller
public class CurationController {

    private final RecommendedArticleRepository recommendedArticleRepository;

    // 생성자를 통해 Repository 주입
    public CurationController(RecommendedArticleRepository recommendedArticleRepository) {
        this.recommendedArticleRepository = recommendedArticleRepository;
    }

    /**
     * 루트 경로 ("/") 요청 시 index.html 페이지 렌더링
     */
    @GetMapping("/")
    public String home() {
        return "index"; // templates/index.html 렌더링
    }

    /**
     * 키워드로 기사 검색
     * @param keyword 검색어
     * @param model   템플릿에 전달할 데이터
     * @return keywords.html 페이지 렌더링
     */
    @GetMapping("/articles/search")
    public String searchArticles(@RequestParam String keyword, Model model) {
        // 대소문자 구분 없이 제목에 키워드가 포함된 기사 목록 조회
        List<RecommendedArticle> articles = recommendedArticleRepository
                .findByTitleContainingIgnoreCase(keyword);
        model.addAttribute("articles", articles); // 검색 결과
        model.addAttribute("keyword", keyword);   // 검색어 유지용
        return "keywords"; // templates/keywords.html 렌더링
    }

    /**
     * 특정 기사 상세 보기
     * @param id    기사 ID
     * @param model 템플릿에 전달할 데이터
     * @return detail.html 또는 not-found.html 페이지 렌더링
     */
    @GetMapping("/articles/{id}")
    public String showArticleDetail(@PathVariable Long id, Model model) {
        return recommendedArticleRepository.findById(id)
            .map(article -> {
                model.addAttribute("article", article); // 상세 데이터 추가
                return "detail"; // templates/detail.html 렌더링
            })
            .orElse("not-found"); // ID 없을 경우 templates/not-found.html
    }

}

package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.entity.AiArticle;
import kr.ac.dankook.cs.curation.entity.BigdataArticle;
import kr.ac.dankook.cs.curation.entity.SecurityArticle;
import kr.ac.dankook.cs.curation.entity.HardwareArticle;
import kr.ac.dankook.cs.curation.repository.AiArticleRepository;
import kr.ac.dankook.cs.curation.repository.BigdataArticleRepository;
import kr.ac.dankook.cs.curation.repository.SecurityArticleRepository;
import kr.ac.dankook.cs.curation.repository.HardwareArticleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 뉴스 큐레이션 기능을 제공하는 웹 컨트롤러
 * - 메인 화면, 카테고리별 키워드 검색, 뉴스 상세 보기 기능을 제공
 */
@Controller
public class CurationController {

    private final AiArticleRepository aiRepo;
    private final BigdataArticleRepository bdRepo;
    private final SecurityArticleRepository secRepo;
    private final HardwareArticleRepository hwRepo;

    public CurationController(
            AiArticleRepository aiRepo,
            BigdataArticleRepository bdRepo,
            SecurityArticleRepository secRepo,
            HardwareArticleRepository hwRepo) {
        this.aiRepo = aiRepo;
        this.bdRepo = bdRepo;
        this.secRepo = secRepo;
        this.hwRepo = hwRepo;
    }

    /** 메인 페이지 */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /** AI 기사 검색 */
    @GetMapping("/articles/ai/search")
    public String searchAi(@RequestParam String keyword, Model model) {
        List<AiArticle> articles = aiRepo.findByTitleContainingIgnoreCase(keyword);
        model.addAttribute("articles", articles);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", "AI");
        return "keywords";
    }

    /** 빅데이터 기사 검색 */
    @GetMapping("/articles/bigdata/search")
    public String searchBigdata(@RequestParam String keyword, Model model) {
        List<BigdataArticle> articles = bdRepo.findByTitleContainingIgnoreCase(keyword);
        model.addAttribute("articles", articles);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", "Bigdata");
        return "keywords";
    }

    /** 보안 기사 검색 */
    @GetMapping("/articles/security/search")
    public String searchSecurity(@RequestParam String keyword, Model model) {
        List<SecurityArticle> articles = secRepo.findByTitleContainingIgnoreCase(keyword);
        model.addAttribute("articles", articles);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", "Security");
        return "keywords";
    }

    /** 하드웨어 기사 검색 */
    @GetMapping("/articles/hardware/search")
    public String searchHardware(@RequestParam String keyword, Model model) {
        List<HardwareArticle> articles = hwRepo.findByTitleContainingIgnoreCase(keyword);
        model.addAttribute("articles", articles);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", "Hardware");
        return "keywords";
    }

    /** AI 기사 상세 */
    @GetMapping("/articles/ai/{id}")
    public String detailAi(@PathVariable Long id, Model model) {
        return aiRepo.findById(id)
                .map(a -> { model.addAttribute("article", a); return "detail"; })
                .orElse("not-found");
    }

    /** 빅데이터 기사 상세 */
    @GetMapping("/articles/bigdata/{id}")
    public String detailBigdata(@PathVariable Long id, Model model) {
        return bdRepo.findById(id)
                .map(a -> { model.addAttribute("article", a); return "detail"; })
                .orElse("not-found");
    }

    /** 보안 기사 상세 */
    @GetMapping("/articles/security/{id}")
    public String detailSecurity(@PathVariable Long id, Model model) {
        return secRepo.findById(id)
                .map(a -> { model.addAttribute("article", a); return "detail"; })
                .orElse("not-found");
    }

    /** 하드웨어 기사 상세 */
    @GetMapping("/articles/hardware/{id}")
    public String detailHardware(@PathVariable Long id, Model model) {
        return hwRepo.findById(id)
                .map(a -> { model.addAttribute("article", a); return "detail"; })
                .orElse("not-found");
    }
}

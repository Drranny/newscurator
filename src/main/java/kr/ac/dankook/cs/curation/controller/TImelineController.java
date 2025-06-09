package kr.ac.dankook.cs.curation.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import kr.ac.dankook.cs.curation.entity.Article;
import kr.ac.dankook.cs.curation.entity.AiArticle;
import kr.ac.dankook.cs.curation.entity.BigdataArticle;
import kr.ac.dankook.cs.curation.entity.SecurityArticle;
import kr.ac.dankook.cs.curation.entity.HardwareArticle;
import kr.ac.dankook.cs.curation.service.NewsService;
import kr.ac.dankook.cs.curation.service.AiArticleService;
import kr.ac.dankook.cs.curation.service.BigdataArticleService;
import kr.ac.dankook.cs.curation.service.SecurityArticleService;
import kr.ac.dankook.cs.curation.service.HardwareArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;

@Controller
public class TImelineController {
    
    @Autowired
    private NewsService newsService;
    @Autowired
    private AiArticleService aiArticleService;
    @Autowired
    private BigdataArticleService bigdataArticleService;
    @Autowired
    private SecurityArticleService securityArticleService;
    @Autowired
    private HardwareArticleService hardwareArticleService;
    
    /**
     * 타임라인 페이지
     */
    @GetMapping("/timeline")
    public String timeline(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model
    ) {
        try {
            // 기본 날짜 설정 (최근 7일)
            if (startDate == null || endDate == null) {
                LocalDate today = LocalDate.now();
                LocalDate weekAgo = today.minusDays(7);
                startDate = weekAgo.toString();
                endDate = today.toString();
            }
            
            // 타임라인 뉴스 데이터 가져오기
            List<Article> timelineNews = getTimelineNews(keyword, category, startDate, endDate);
            
            // 시간 순으로 정렬 (최신순)
            timelineNews = timelineNews.stream()
                .sorted((a, b) -> b.getPublishedAt().compareTo(a.getPublishedAt()))
                .collect(Collectors.toList());
            
            // 오늘 뉴스 개수 계산
            long todayNewsCount = getTodayNewsCount(timelineNews);
            
            // 활성 키워드 추출
            Set<String> activeKeywords = getActiveKeywords(timelineNews);
            
            // 조회 기간 계산
            String timeRange = calculateTimeRange(startDate, endDate);
            
            // 모델에 데이터 추가
            model.addAttribute("timelineNews", timelineNews);
            model.addAttribute("todayNewsCount", todayNewsCount);
            model.addAttribute("activeKeywords", activeKeywords);
            model.addAttribute("timeRange", timeRange);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("selectedKeyword", keyword);
            model.addAttribute("selectedCategory", category);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isLoggedIn = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
            model.addAttribute("isLoggedIn", isLoggedIn);
            
            return "timeline";
            
        } catch (Exception e) {
            // 에러 발생 시 로그 출력 및 기본 페이지 반환
            e.printStackTrace();
            model.addAttribute("timelineNews", List.of());
            model.addAttribute("todayNewsCount", 0);
            model.addAttribute("activeKeywords", Set.of());
            model.addAttribute("timeRange", "7일");
            model.addAttribute("startDate", LocalDate.now().minusDays(7).toString());
            model.addAttribute("endDate", LocalDate.now().toString());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isLoggedIn = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
            model.addAttribute("isLoggedIn", isLoggedIn);
            
            return "timeline";
        }
    }
    
    /**
     * 타임라인 뉴스 데이터 조회
     */
    private List<Article> getTimelineNews(String keyword, String category, String startDate, String endDate) {
        List<Article> allNews = new ArrayList<>();
        // 각 카테고리별로 Article 변환해서 합치기
        allNews.addAll(aiArticleService.findAll().stream().map(this::convertAiToArticle).toList());
        allNews.addAll(bigdataArticleService.findAll().stream().map(this::convertBigdataToArticle).toList());
        allNews.addAll(securityArticleService.findAll().stream().map(this::convertSecurityToArticle).toList());
        allNews.addAll(hardwareArticleService.findAll().stream().map(this::convertHardwareToArticle).toList());
        // 기존 필터링(카테고리, 키워드, 날짜 등) 로직 그대로 사용
        if (category != null && !category.isEmpty() && !category.equals("all")) {
            allNews = allNews.stream().filter(article -> category.equalsIgnoreCase(article.getCategory())).toList();
        }
        if (keyword != null && !keyword.isEmpty() && !keyword.equals("all")) {
            String lowerKeyword = keyword.toLowerCase();
            allNews = allNews.stream().filter(article ->
                (article.getTitle() != null && article.getTitle().toLowerCase().contains(lowerKeyword)) ||
                (article.getDescription() != null && article.getDescription().toLowerCase().contains(lowerKeyword)) ||
                (article.getKeywords() != null && article.getKeywords().stream().anyMatch(k -> k.toLowerCase().contains(lowerKeyword)))
            ).toList();
        }
        if (startDate != null && endDate != null) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            allNews = allNews.stream().filter(article -> {
                if (article.getPublishedAt() == null) return false;
                LocalDate articleDate = article.getPublishedAt().toLocalDate();
                return !articleDate.isBefore(start) && !articleDate.isAfter(end);
            }).toList();
        }
        return allNews;
    }
    
    private Article convertAiToArticle(AiArticle ai) {
        Article a = new Article();
        a.setTitle(ai.getTitle());
        a.setDescription(ai.getDescription());
        a.setPublishedAt(ai.getPublishedAt());
        a.setCategory("AI");
        a.setKeywords(splitKeywords(ai.getKeywords()));
        a.setSourceUrl(ai.getUrl());
        a.setImageUrl(ai.getUrlToImage());
        a.setSourceName(ai.getSourceName());
        return a;
    }
    private Article convertBigdataToArticle(BigdataArticle bd) {
        Article a = new Article();
        a.setTitle(bd.getTitle());
        a.setDescription(bd.getDescription());
        a.setPublishedAt(bd.getPublishedAt());
        a.setCategory("데이터");
        a.setKeywords(splitKeywords(bd.getKeywords()));
        a.setSourceUrl(bd.getUrl());
        a.setImageUrl(bd.getUrlToImage());
        a.setSourceName(bd.getSourceName());
        return a;
    }
    private Article convertSecurityToArticle(SecurityArticle sec) {
        Article a = new Article();
        a.setTitle(sec.getTitle());
        a.setDescription(sec.getDescription());
        a.setPublishedAt(sec.getPublishedAt());
        a.setCategory("보안");
        a.setKeywords(splitKeywords(sec.getKeywords()));
        a.setSourceUrl(sec.getUrl());
        a.setImageUrl(sec.getUrlToImage());
        a.setSourceName(sec.getSourceName());
        return a;
    }
    private Article convertHardwareToArticle(HardwareArticle hw) {
        Article a = new Article();
        a.setTitle(hw.getTitle());
        a.setDescription(hw.getDescription());
        a.setPublishedAt(hw.getPublishedAt());
        a.setCategory("하드웨어");
        a.setKeywords(splitKeywords(hw.getKeywords()));
        a.setSourceUrl(hw.getUrl());
        a.setImageUrl(hw.getUrlToImage());
        a.setSourceName(hw.getSourceName());
        return a;
    }
    private List<String> splitKeywords(String keywords) {
        if (keywords == null || keywords.isBlank()) return List.of();
        return Arrays.stream(keywords.split(",|;| ")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
    
    /**
     * 오늘 뉴스 개수 계산
     */
    private long getTodayNewsCount(List<Article> timelineNews) {
        LocalDate today = LocalDate.now();
        
        return timelineNews.stream()
            .filter(article -> article.getPublishedAt().toLocalDate().equals(today))
            .count();
    }
    
    /**
     * 활성 키워드 추출
     */
    private Set<String> getActiveKeywords(List<Article> timelineNews) {
        return timelineNews.stream()
            .filter(article -> article.getKeywords() != null)
            .flatMap(article -> article.getKeywords().stream())
            .collect(Collectors.toSet());
    }
    
    /**
     * 조회 기간 계산
     */
    private String calculateTimeRange(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            long daysBetween = end.toEpochDay() - start.toEpochDay() + 1;
            
            if (daysBetween == 1) {
                return "1일";
            } else if (daysBetween == 7) {
                return "7일";
            } else if (daysBetween == 30) {
                return "30일";
            } else {
                return daysBetween + "일";
            }
        } catch (Exception e) {
            return "7일";
        }
    }
    
    /**
     * AJAX로 타임라인 데이터 업데이트 (필요시 사용)
     */
    @GetMapping("/api/timeline/refresh")
    public String refreshTimeline(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model
    ) {
        // 동일한 로직으로 데이터 조회
        return timeline(keyword, category, startDate, endDate, model);
    }
}

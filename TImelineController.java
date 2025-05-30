package kr.ac.dankook.cs.curation.controller;

import kr.ac.dankook.cs.curation.entity.AiArticle;
import kr.ac.dankook.cs.curation.entity.BigdataArticle;
import kr.ac.dankook.cs.curation.entity.SecurityArticle;
import kr.ac.dankook.cs.curation.entity.HardwareArticle;
import kr.ac.dankook.cs.curation.service.AiArticleService;
import kr.ac.dankook.cs.curation.service.BigdataArticleService;
import kr.ac.dankook.cs.curation.service.SecurityArticleService;
import kr.ac.dankook.cs.curation.service.HardwareArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 타임라인 기능을 제공하는 웹 컨트롤러
 * - 뉴스 기사의 시간순 정렬 및 필터링 기능 제공
 * - 카테고리별, 키워드별, 날짜별 필터링 지원
 */
@Controller
public class TImelineController {
    
    private static final Logger log = LoggerFactory.getLogger(TImelineController.class);
    
    private final AiArticleService aiArticleService;
    private final BigdataArticleService bigdataArticleService;
    private final SecurityArticleService securityArticleService;
    private final HardwareArticleService hardwareArticleService;

    public TImelineController(
            AiArticleService aiArticleService,
            BigdataArticleService bigdataArticleService,
            SecurityArticleService securityArticleService,
            HardwareArticleService hardwareArticleService) {
        this.aiArticleService = aiArticleService;
        this.bigdataArticleService = bigdataArticleService;
        this.securityArticleService = securityArticleService;
        this.hardwareArticleService = hardwareArticleService;
    }
    
    /**
     * GET /timeline
     * 타임라인 페이지 렌더링
     * @param keyword 검색 키워드 (선택)
     * @param category 카테고리 필터 (선택)
     * @param startDate 시작 날짜 (선택, 기본값: 7일 전)
     * @param endDate 종료 날짜 (선택, 기본값: 오늘)
     * @param model 뷰에 전달할 데이터 모델
     * @return 타임라인 뷰 이름
     */
    @GetMapping("/timeline")
    public String timeline(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model
    ) {
        log.info("타임라인 페이지 요청 - 키워드: {}, 카테고리: {}, 시작일: {}, 종료일: {}", 
            keyword, category, startDate, endDate);
            
        try {
            // 로그인 정보 처리
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                model.addAttribute("login_id", auth.getName());
                model.addAttribute("isLoggedIn", true);
            } else {
                model.addAttribute("isLoggedIn", false);
            }
            
            // 기본 날짜 설정 (최근 7일)
            if (startDate == null || endDate == null) {
                LocalDate today = LocalDate.now();
                LocalDate weekAgo = today.minusDays(7);
                startDate = weekAgo.toString();
                endDate = today.toString();
                log.debug("기본 날짜 설정 - 시작일: {}, 종료일: {}", startDate, endDate);
            }
            
            // 타임라인 뉴스 데이터 가져오기
            List<Object> timelineNews = getTimelineNews(keyword, category, startDate, endDate);
            log.debug("조회된 뉴스 기사 수: {}", timelineNews.size());
            
            // 시간 순으로 정렬 (최신순)
            timelineNews = timelineNews.stream()
                .sorted((a, b) -> {
                    LocalDateTime dateA = getPublishedAt(a);
                    LocalDateTime dateB = getPublishedAt(b);
                    if (dateA == null || dateB == null) return 0;
                    return dateB.compareTo(dateA);
                })
                .collect(Collectors.toList());
            
            // 오늘 뉴스 개수 계산
            long todayNewsCount = getTodayNewsCount(timelineNews);
            log.debug("오늘의 뉴스 기사 수: {}", todayNewsCount);
            
            // 활성 키워드 추출
            Set<String> activeKeywords = getActiveKeywords(timelineNews);
            log.debug("활성 키워드 수: {}", activeKeywords.size());
            
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
            
            log.info("타임라인 페이지 렌더링 완료");
            return "timeline";
            
        } catch (Exception e) {
            log.error("타임라인 페이지 로딩 중 오류 발생", e);
            // 에러 발생 시 기본값으로 모델 설정
            model.addAttribute("timelineNews", List.of());
            model.addAttribute("todayNewsCount", 0);
            model.addAttribute("activeKeywords", Set.of());
            model.addAttribute("timeRange", "7일");
            model.addAttribute("startDate", LocalDate.now().minusDays(7).toString());
            model.addAttribute("endDate", LocalDate.now().toString());
            model.addAttribute("error", "페이지 로딩 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            model.addAttribute("isLoggedIn", false);
            
            return "timeline";
        }
    }
    
    /**
     * 타임라인 뉴스 데이터 조회
     * @param keyword 검색 키워드
     * @param category 카테고리 필터
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 필터링된 기사 리스트
     */
    private List<Object> getTimelineNews(String keyword, String category, String startDate, String endDate) {
        List<Object> allNews = new ArrayList<>();
        
        try {
            // 카테고리별로 기사 가져오기
            if (category == null || category.isEmpty() || category.equals("all")) {
                log.debug("모든 카테고리의 기사 조회");
                allNews.addAll(aiArticleService.findAll());
                allNews.addAll(bigdataArticleService.findAll());
                allNews.addAll(securityArticleService.findAll());
                allNews.addAll(hardwareArticleService.findAll());
            } else {
                log.debug("카테고리 '{}'의 기사 조회", category);
                switch (category.toLowerCase()) {
                    case "ai":
                        allNews.addAll(aiArticleService.findAll());
                        break;
                    case "데이터":
                        allNews.addAll(bigdataArticleService.findAll());
                        break;
                    case "보안":
                        allNews.addAll(securityArticleService.findAll());
                        break;
                    case "하드웨어":
                        allNews.addAll(hardwareArticleService.findAll());
                        break;
                }
            }
            
            // 키워드 필터링
            if (keyword != null && !keyword.isEmpty() && !keyword.equals("all")) {
                String lowerKeyword = keyword.toLowerCase();
                log.debug("키워드 '{}'로 필터링", keyword);
                allNews = allNews.stream().filter(article ->
                    (getTitle(article) != null && getTitle(article).toLowerCase().contains(lowerKeyword)) ||
                    (getDescription(article) != null && getDescription(article).toLowerCase().contains(lowerKeyword)) ||
                    (getKeywords(article) != null && getKeywords(article).stream().anyMatch(k -> k.toLowerCase().contains(lowerKeyword)))
                ).toList();
            }
            
            // 날짜 필터링
            if (startDate != null && endDate != null) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                log.debug("날짜 범위 {} ~ {}로 필터링", startDate, endDate);
                allNews = allNews.stream().filter(article -> {
                    LocalDateTime publishedAt = getPublishedAt(article);
                    if (publishedAt == null) return false;
                    LocalDate articleDate = publishedAt.toLocalDate();
                    return !articleDate.isBefore(start) && !articleDate.isAfter(end);
                }).toList();
            }
            
            log.debug("필터링 후 남은 기사 수: {}", allNews.size());
            return allNews;
            
        } catch (Exception e) {
            log.error("뉴스 데이터 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 기사의 제목 가져오기
     */
    private String getTitle(Object article) {
        try {
            if (article instanceof AiArticle) return ((AiArticle) article).getTitle();
            if (article instanceof BigdataArticle) return ((BigdataArticle) article).getTitle();
            if (article instanceof SecurityArticle) return ((SecurityArticle) article).getTitle();
            if (article instanceof HardwareArticle) return ((HardwareArticle) article).getTitle();
        } catch (Exception e) {
            log.warn("기사 제목 조회 중 오류 발생", e);
        }
        return null;
    }

    /**
     * 기사의 설명 가져오기
     */
    private String getDescription(Object article) {
        try {
            if (article instanceof AiArticle) return ((AiArticle) article).getDescription();
            if (article instanceof BigdataArticle) return ((BigdataArticle) article).getDescription();
            if (article instanceof SecurityArticle) return ((SecurityArticle) article).getDescription();
            if (article instanceof HardwareArticle) return ((HardwareArticle) article).getDescription();
        } catch (Exception e) {
            log.warn("기사 설명 조회 중 오류 발생", e);
        }
        return null;
    }

    /**
     * 기사의 발행일 가져오기
     */
    private LocalDateTime getPublishedAt(Object article) {
        try {
            if (article instanceof AiArticle) return ((AiArticle) article).getPublishedAt();
            if (article instanceof BigdataArticle) return ((BigdataArticle) article).getPublishedAt();
            if (article instanceof SecurityArticle) return ((SecurityArticle) article).getPublishedAt();
            if (article instanceof HardwareArticle) return ((HardwareArticle) article).getPublishedAt();
        } catch (Exception e) {
            log.warn("기사 발행일 조회 중 오류 발생", e);
        }
        return null;
    }

    /**
     * 기사의 키워드 가져오기
     */
    private List<String> getKeywords(Object article) {
        try {
            String keywords = null;
            if (article instanceof AiArticle) keywords = ((AiArticle) article).getKeywords();
            if (article instanceof BigdataArticle) keywords = ((BigdataArticle) article).getKeywords();
            if (article instanceof SecurityArticle) keywords = ((SecurityArticle) article).getKeywords();
            if (article instanceof HardwareArticle) keywords = ((HardwareArticle) article).getKeywords();
            
            if (keywords == null || keywords.isBlank()) return List.of();
            return Arrays.stream(keywords.split(",|;| ")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        } catch (Exception e) {
            log.warn("기사 키워드 조회 중 오류 발생", e);
            return List.of();
        }
    }
    
    /**
     * 오늘 뉴스 개수 계산
     */
    private long getTodayNewsCount(List<Object> timelineNews) {
        try {
            LocalDate today = LocalDate.now();
            return timelineNews.stream()
                .filter(article -> {
                    LocalDateTime publishedAt = getPublishedAt(article);
                    return publishedAt != null && publishedAt.toLocalDate().equals(today);
                })
                .count();
        } catch (Exception e) {
            log.warn("오늘의 뉴스 개수 계산 중 오류 발생", e);
            return 0;
        }
    }
    
    /**
     * 활성 키워드 추출
     */
    private Set<String> getActiveKeywords(List<Object> timelineNews) {
        try {
            return timelineNews.stream()
                .flatMap(article -> getKeywords(article).stream())
                .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("활성 키워드 추출 중 오류 발생", e);
            return Set.of();
        }
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
            log.warn("조회 기간 계산 중 오류 발생", e);
            return "7일";
        }
    }
    
    /**
     * GET /api/timeline/refresh
     * AJAX로 타임라인 데이터 업데이트
     */
    @GetMapping("/api/timeline/refresh")
    public String refreshTimeline(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model
    ) {
        log.info("타임라인 새로고침 요청 - 키워드: {}, 카테고리: {}, 시작일: {}, 종료일: {}", 
            keyword, category, startDate, endDate);
        return timeline(keyword, category, startDate, endDate, model);
    }
}
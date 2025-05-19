package kr.ac.dankook.cs.curation.scheduler;

import kr.ac.dankook.cs.curation.NewsApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

@Component
@EnableScheduling
public class NewsScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(NewsScheduler.class);
    private final NewsApiService newsApiService;

    public NewsScheduler(NewsApiService newsApiService) {
        this.newsApiService = newsApiService;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void scheduleNewsFetch() {
        log.info("뉴스 자동 수집 시작 - 실행시각: {}", LocalDateTime.now());
        try {
            newsApiService.fetchAllCategories();
            log.info("뉴스 자동 수집 완료");
        } catch (Exception e) {
            log.error("뉴스 자동 수집 중 오류 발생: {}", e.getMessage());
        }
    }

    @PostConstruct
    public void initializeNewsFetch() {
        log.info("서버 시작 시 초기 뉴스 수집 실행");
        scheduleNewsFetch();
    }
} 
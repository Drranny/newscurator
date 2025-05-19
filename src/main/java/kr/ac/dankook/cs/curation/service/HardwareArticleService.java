package kr.ac.dankook.cs.curation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.dankook.cs.curation.entity.HardwareArticle;
import kr.ac.dankook.cs.curation.repository.HardwareArticleRepository;

/**
 * 하드웨어 뉴스 기사에 대한 비즈니스 로직 서비스
 */
@Service
public class HardwareArticleService {
    @Autowired
    private HardwareArticleRepository hardwareArticleRepository;

    public HardwareArticle saveIfNotExists(HardwareArticle article) {
        Optional<HardwareArticle> existing = hardwareArticleRepository.findByUrl(article.getUrl());
        if (existing.isPresent()) {
            return existing.get();
        }
        article.setRecommendedAt(LocalDateTime.now());
        return hardwareArticleRepository.save(article);
    }

    public List<HardwareArticle> findAll() {
        return hardwareArticleRepository.findAll();
    }

    public Optional<HardwareArticle> findById(Long id) {
        return hardwareArticleRepository.findById(id);
    }

    public void deleteById(Long id) {
        hardwareArticleRepository.deleteById(id);
    }
    
    public List<HardwareArticle> findByTitleContainingIgnoreCase(String keyword) {
        return hardwareArticleRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public List<HardwareArticle> getAllArticles() {
        return hardwareArticleRepository.findAll();
    }

    public List<HardwareArticle> getLatestArticles(int limit) {
        return hardwareArticleRepository.findTopByOrderByPublishedAtDesc(limit);
    }

    public List<HardwareArticle> getTopViewedArticles(int limit) {
        return hardwareArticleRepository.findTopByOrderByViewsDesc(limit);
    }

    public HardwareArticle saveArticle(HardwareArticle article) {
        return hardwareArticleRepository.save(article);
    }
}

package kr.ac.dankook.cs.curation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.dankook.cs.curation.entity.SecurityArticle;
import kr.ac.dankook.cs.curation.repository.SecurityArticleRepository;

/**
 * 보안 뉴스 기사에 대한 비즈니스 로직 서비스
 */
@Service
public class SecurityArticleService {
    @Autowired
    private SecurityArticleRepository securityArticleRepository;

    public SecurityArticle saveIfNotExists(SecurityArticle article) {
        Optional<SecurityArticle> existing = securityArticleRepository.findByUrl(article.getUrl());
        if (existing.isPresent()) {
            return existing.get();
        }
        article.setRecommendedAt(LocalDateTime.now());
        return securityArticleRepository.save(article);
    }

    public List<SecurityArticle> findAll() {
        return securityArticleRepository.findAll();
    }

    public Optional<SecurityArticle> findById(Long id) {
        return securityArticleRepository.findById(id);
    }

    public void deleteById(Long id) {
        securityArticleRepository.deleteById(id);
    }
    
    public List<SecurityArticle> findByTitleContainingIgnoreCase(String keyword) {
        return securityArticleRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public List<SecurityArticle> getAllArticles() {
        return securityArticleRepository.findAll();
    }

    public List<SecurityArticle> getLatestArticles(int limit) {
        return securityArticleRepository.findTopByOrderByPublishedAtDesc(limit);
    }

    public List<SecurityArticle> getTopViewedArticles(int limit) {
        return securityArticleRepository.findTopByOrderByViewsDesc(limit);
    }

    public SecurityArticle saveArticle(SecurityArticle article) {
        return securityArticleRepository.save(article);
    }
}

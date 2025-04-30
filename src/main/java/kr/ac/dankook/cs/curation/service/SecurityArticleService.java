package kr.ac.dankook.cs.curation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import kr.ac.dankook.cs.curation.entity.SecurityArticle;
import kr.ac.dankook.cs.curation.repository.SecurityArticleRepository;

/**
 * 보안 뉴스 기사에 대한 비즈니스 로직 서비스
 */
@Service
public class SecurityArticleService {
    private final SecurityArticleRepository repository;

    public SecurityArticleService(SecurityArticleRepository repository) {
        this.repository = repository;
    }

    public SecurityArticle saveIfNotExists(SecurityArticle article) {
        Optional<SecurityArticle> existing = repository.findByUrl(article.getUrl());
        if (existing.isPresent()) {
            return existing.get();
        }
        article.setRecommendedAt(LocalDateTime.now());
        return repository.save(article);
    }

    public List<SecurityArticle> findAll() {
        return repository.findAll();
    }

    public Optional<SecurityArticle> findById(Long id) {
        return repository.findById(id);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    public List<SecurityArticle> findByTitleContainingIgnoreCase(String keyword) {
        return repository.findByTitleContainingIgnoreCase(keyword);
    }
}

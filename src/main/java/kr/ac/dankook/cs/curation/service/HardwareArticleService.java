package kr.ac.dankook.cs.curation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import kr.ac.dankook.cs.curation.entity.HardwareArticle;
import kr.ac.dankook.cs.curation.repository.HardwareArticleRepository;

/**
 * 하드웨어 뉴스 기사에 대한 비즈니스 로직 서비스
 */
@Service
public class HardwareArticleService {
    private final HardwareArticleRepository repository;

    public HardwareArticleService(HardwareArticleRepository repository) {
        this.repository = repository;
    }

    public HardwareArticle saveIfNotExists(HardwareArticle article) {
        Optional<HardwareArticle> existing = repository.findByUrl(article.getUrl());
        if (existing.isPresent()) {
            return existing.get();
        }
        article.setRecommendedAt(LocalDateTime.now());
        return repository.save(article);
    }

    public List<HardwareArticle> findAll() {
        return repository.findAll();
    }

    public Optional<HardwareArticle> findById(Long id) {
        return repository.findById(id);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    public List<HardwareArticle> findByTitleContainingIgnoreCase(String keyword) {
        return repository.findByTitleContainingIgnoreCase(keyword);
    }
}

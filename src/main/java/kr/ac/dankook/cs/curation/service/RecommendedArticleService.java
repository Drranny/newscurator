package kr.ac.dankook.cs.curation.service;

import kr.ac.dankook.cs.curation.entity.RecommendedArticle;
import kr.ac.dankook.cs.curation.repository.RecommendedArticleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RecommendedArticleService {

    private final RecommendedArticleRepository repository;

    public RecommendedArticleService(RecommendedArticleRepository repository) {
        this.repository = repository;
    }

    public RecommendedArticle saveIfNotExists(RecommendedArticle article) {
        Optional<RecommendedArticle> existing = repository.findByUrl(article.getUrl());
        if (existing.isPresent()) {
            return existing.get(); // 이미 저장된 기사
        }
        article.setRecommendedAt(LocalDateTime.now());
        return repository.save(article);
    }

    public List<RecommendedArticle> findAll() {
        return repository.findAll();
    }

    public Optional<RecommendedArticle> findById(Long id) {
        return repository.findById(id);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

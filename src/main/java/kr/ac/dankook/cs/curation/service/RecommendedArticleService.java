package kr.ac.dankook.cs.curation.service;

import kr.ac.dankook.cs.curation.model.RecommendedArticle;
import kr.ac.dankook.cs.curation.repository.RecommendedArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecommendedArticleService {

    private final RecommendedArticleRepository repository;

    @Autowired
    public RecommendedArticleService(RecommendedArticleRepository repository) {
        this.repository = repository;
    }

    // 기사 저장
    public RecommendedArticle saveRecommendedArticle(RecommendedArticle article) {
        return repository.save(article);
    }

    // 모든 기사 가져오기
    public List<RecommendedArticle> getAllRecommendedArticles() {
        return repository.findAll();
    }

    // ID로 기사 검색
    public Optional<RecommendedArticle> getArticleById(Long id) {
        return repository.findById(id);
    }
}

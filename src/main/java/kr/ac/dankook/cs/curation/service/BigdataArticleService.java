package kr.ac.dankook.cs.curation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import kr.ac.dankook.cs.curation.entity.BigdataArticle;
import kr.ac.dankook.cs.curation.repository.BigdataArticleRepository;

/**
 * 빅데이터 뉴스 기사에 대한 비즈니스 로직 서비스
 */
@Service
public class BigdataArticleService {
    private final BigdataArticleRepository repository;

    public BigdataArticleService(BigdataArticleRepository repository) {
        this.repository = repository;
    }

    /**
     * 동일한 URL의 기사가 없을 경우에만 저장
     */
    public BigdataArticle saveIfNotExists(BigdataArticle article) {
        Optional<BigdataArticle> existing = repository.findByUrl(article.getUrl());
        if (existing.isPresent()) {
            return existing.get();
        }
        article.setRecommendedAt(LocalDateTime.now());
        return repository.save(article);
    }

    /** 전체 목록 조회 */
    public List<BigdataArticle> findAll() {
        return repository.findAll();
    }

    /** ID로 단건 조회 */
    public Optional<BigdataArticle> findById(Long id) {
        return repository.findById(id);
    }

    /** ID로 삭제 */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
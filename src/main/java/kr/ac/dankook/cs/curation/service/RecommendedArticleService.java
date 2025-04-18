package kr.ac.dankook.cs.curation.service;

import kr.ac.dankook.cs.curation.entity.RecommendedArticle;
import kr.ac.dankook.cs.curation.repository.RecommendedArticleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 추천 뉴스 기사에 대한 비즈니스 로직을 처리하는 서비스 클래스
 * - 중복 방지 저장, 조회, 삭제 등의 기능 제공
 */
@Service
public class RecommendedArticleService {

    private final RecommendedArticleRepository repository;

    // 생성자 주입
    public RecommendedArticleService(RecommendedArticleRepository repository) {
        this.repository = repository;
    }

    /**
     * 동일한 URL의 기사가 없을 경우에만 저장
     * - 추천 시각도 현재 시각으로 자동 설정
     * @param article 저장할 기사
     * @return 저장된 또는 기존에 존재하는 기사
     */
    public RecommendedArticle saveIfNotExists(RecommendedArticle article) {
        Optional<RecommendedArticle> existing = repository.findByUrl(article.getUrl());
        if (existing.isPresent()) {
            return existing.get(); // 이미 저장되어 있으면 해당 기사 반환
        }
        article.setRecommendedAt(LocalDateTime.now()); // 추천된 시각 설정
        return repository.save(article); // 새로 저장
    }

    /**
     * 모든 추천 기사 목록 반환
     */
    public List<RecommendedArticle> findAll() {
        return repository.findAll();
    }

    /**
     * ID로 추천 기사 단건 조회
     */
    public Optional<RecommendedArticle> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * ID로 추천 기사 삭제
     */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

package kr.ac.dankook.cs.curation.repository;

import kr.ac.dankook.cs.curation.entity.RecommendedArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendedArticleRepository extends JpaRepository<RecommendedArticle, Long> {
    
    // 중복 저장 방지용
    boolean existsByUrl(String url);

    // 검색용: 제목에 키워드 포함 (대소문자 무시)
    List<RecommendedArticle> findByTitleContainingIgnoreCase(String keyword);

    // 필요 시 URL로 기사 전체 찾기
    Optional<RecommendedArticle> findByUrl(String url);
}

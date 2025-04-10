package kr.ac.dankook.cs.curation.repository;

import kr.ac.dankook.cs.curation.model.RecommendedArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedArticleRepository extends JpaRepository<RecommendedArticle, Long> {
    // 필요한 쿼리 메소드 작성 (예: 기사 제목으로 검색)
}

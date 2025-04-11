package kr.ac.dankook.cs.curation.repository;

import kr.ac.dankook.cs.curation.model.RecommendedArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendedArticleRepository extends JpaRepository<RecommendedArticle, Long> {
    List<RecommendedArticle> findByTitleContainingIgnoreCase(String keyword);
}

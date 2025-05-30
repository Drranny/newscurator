package kr.ac.dankook.cs.curation.repository;

import kr.ac.dankook.cs.curation.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    /**
     * 카테고리별 뉴스 기사 조회
     */
    List<Article> findByCategory(String category);
    
    /**
     * 제목이나 설명에 키워드가 포함된 뉴스 기사 검색
     */
    List<Article> findByTitleContainingOrDescriptionContaining(String title, String description);
} 
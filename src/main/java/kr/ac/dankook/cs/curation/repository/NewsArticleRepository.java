package kr.ac.dankook.cs.curation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import kr.ac.dankook.cs.curation.model.NewsArticle;
import java.util.List;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    List<NewsArticle> findByTitleContainingIgnoreCase(String keyword);
    // 필요한 추가적인 쿼리 메서드 정의 가능
}
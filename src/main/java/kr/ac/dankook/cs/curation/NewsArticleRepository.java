package kr.ac.dankook.cs.curation;

import org.springframework.data.jpa.repository.JpaRepository;
import kr.ac.dankook.cs.curation.model.NewsArticle;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    // 필요한 추가적인 쿼리 메서드 정의 가능
}

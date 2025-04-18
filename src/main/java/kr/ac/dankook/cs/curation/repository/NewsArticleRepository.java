package kr.ac.dankook.cs.curation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import kr.ac.dankook.cs.curation.entity.NewsArticle;

import java.util.List;

/**
 * NewsArticle 엔티티에 대한 JPA 리포지토리 인터페이스
 * - 기본적인 CRUD 기능 제공 (JpaRepository 상속)
 * - 사용자 정의 쿼리 메서드도 정의 가능
 */
@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    /**
     * 제목에 특정 키워드가 포함된 뉴스 기사 검색 (대소문자 무시)
     * @param keyword 검색할 키워드
     * @return 해당 키워드를 포함하는 뉴스 기사 리스트
     */
    List<NewsArticle> findByTitleContainingIgnoreCase(String keyword);
}

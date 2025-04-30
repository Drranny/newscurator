package kr.ac.dankook.cs.curation.repository;

import kr.ac.dankook.cs.curation.entity.SecurityArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SecurityArticle 엔티티용 JPA 리포지토리
 * - 보안 뉴스 기사에 대한 CRUD 및 검색 기능 제공
 */
@Repository
public interface SecurityArticleRepository extends JpaRepository<SecurityArticle, Long> {

    /**
     * 특정 URL을 가진 보안 뉴스가 이미 존재하는지 확인
     * - 중복 저장 방지에 사용
     * @param url 뉴스 URL
     * @return 존재 여부 (true/false)
     */
    boolean existsByUrl(String url);

    /**
     * 제목에 특정 키워드가 포함된 보안 뉴스 검색
     * - 대소문자 무시
     * @param keyword 검색 키워드
     * @return 매칭된 기사 목록
     */
    List<SecurityArticle> findByTitleContainingIgnoreCase(String keyword);

    /**
     * 특정 URL을 가진 보안 기사 단건 조회
     * - existsByUrl과 다르게 실제 객체를 가져옴
     * @param url 기사 URL
     * @return Optional 형태로 기사 반환
     */
    Optional<SecurityArticle> findByUrl(String url);
}

package kr.ac.dankook.cs.curation.login.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import kr.ac.dankook.cs.curation.login.data.User;
import kr.ac.dankook.cs.curation.login.data.UserArticle;
import java.util.List;

@Repository
public interface UserArticleRepository extends JpaRepository<UserArticle, Long> {
    // 사용자별 읽은 기사 목록 조회
    List<UserArticle> findByUser(User user);
    
    // 특정 기사를 읽었는지 확인
    UserArticle findByUserAndTitleAndArticleCategory(User user, String title, String articleCategory);
}

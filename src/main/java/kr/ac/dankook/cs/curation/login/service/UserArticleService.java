package kr.ac.dankook.cs.curation.login.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.ac.dankook.cs.curation.login.data.User;
import kr.ac.dankook.cs.curation.login.data.UserArticle;
import kr.ac.dankook.cs.curation.login.repository.UserArticleRepository;

@Service
@Transactional
public class UserArticleService {
    @Autowired
    private UserArticleRepository userArticleRepository;

    // 기사 읽기 기록
    public void recordArticleRead(User user, String title, String articleCategory, String keywords) {
        UserArticle userArticle = userArticleRepository.findByUserAndTitleAndArticleCategory(user, title, articleCategory);

        if (userArticle == null) {
            // 새로운 읽기 기록 생성
            userArticle = new UserArticle();
            userArticle.setUser(user);
            userArticle.setTitle(title);
            userArticle.setArticleCategory(articleCategory);
            userArticle.setKeywords(keywords);
        } else {
            // 기존 기록 업데이트
            userArticle.setReadCount(userArticle.getReadCount() + 1);
        }

        userArticleRepository.save(userArticle);
    }

    // 체류 시간 업데이트
    public void updateStayTime(User user, String title, String articleCategory, Long stayTime) {
        UserArticle userArticle = userArticleRepository
            .findByUserAndTitleAndArticleCategory(user, title, articleCategory);
        
        if (userArticle != null) {
            userArticle.setTotalStayTime(userArticle.getTotalStayTime() + stayTime);
            userArticleRepository.save(userArticle);
        }
    }

    // 사용자의 읽은 기사 목록 조회
    public List<UserArticle> getUserReadArticles(User user) {
        return userArticleRepository.findByUser(user);
    }
}
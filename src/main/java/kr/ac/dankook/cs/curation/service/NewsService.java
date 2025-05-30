package kr.ac.dankook.cs.curation.service;

import kr.ac.dankook.cs.curation.entity.Article;
import kr.ac.dankook.cs.curation.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NewsService {
    
    @Autowired
    private ArticleRepository articleRepository;
    
    /**
     * 모든 뉴스 기사 조회
     */
    public List<Article> getAllNews() {
        return articleRepository.findAll();
    }
    
    /**
     * 카테고리별 뉴스 기사 조회
     */
    public List<Article> getNewsByCategory(String category) {
        return articleRepository.findByCategory(category);
    }
    
    /**
     * 키워드로 뉴스 기사 검색
     */
    public List<Article> searchNewsByKeyword(String keyword) {
        return articleRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
    }
    
    /**
     * 뉴스 기사 저장
     */
    public Article saveArticle(Article article) {
        return articleRepository.save(article);
    }
    
    /**
     * 여러 뉴스 기사 저장
     */
    public List<Article> saveAllArticles(List<Article> articles) {
        return articleRepository.saveAll(articles);
    }
} 
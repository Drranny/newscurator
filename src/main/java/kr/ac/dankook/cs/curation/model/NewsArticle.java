package kr.ac.dankook.cs.curation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity // 데이터베이스 연동 시
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String url;
    private String author;
    private LocalDateTime publishedAt;
    private String sourceName; // NewsAPI 응답의 source 필드 내 name 값

    // 필요에 따라 NewsAPI 응답의 다른 필드도 추가
}
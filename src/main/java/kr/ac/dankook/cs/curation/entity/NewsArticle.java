package kr.ac.dankook.cs.curation.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 뉴스 기사 정보를 나타내는 JPA 엔티티 클래스
 * - DB 테이블명: news_article
 * - Lombok의 @Data를 사용하여 getter/setter, toString, equals, hashCode 자동 생성
 */
@Entity
@Table(name = "news_article")
@Data
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment 방식 ID 생성
    private Long id;

    private String author;       // 기사 작성자
    private String title;        // 기사 제목
    private String description;  // 기사 내용 요약
    private String url;          // 원문 URL

    @Column(name = "published_at")
    private LocalDateTime publishedAt; // 기사 발행 일시

    @Column(name = "source_name")
    private String sourceName;         // 출처 이름 (예: Yonhap, ZDNet 등)
}

package kr.ac.dankook.cs.curation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 보안 뉴스 기사 정보를 나타내는 엔티티 클래스
 * - 뉴스 데이터 + 추천 관련 정보 저장
 * - DB 테이블명: security_articles
 */
@Entity
@Table(name = "security_articles")
@Data // Lombok: getter, setter, toString, equals, hashCode 자동 생성
public class SecurityArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 기사 제목

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String url; // 원문 URL (중복 방지)

    @Column(columnDefinition = "TEXT")
    private String urlOriginal; // 원본 URL (변환 전)

    @Column(columnDefinition = "TEXT")
    private String description; // 기사 요약

    private String author; // 작성자 (null 가능)

    @Column(name = "published_at")
    private LocalDateTime publishedAt; // 기사 발행 시점

    @Column(name = "recommended_at", nullable = false)
    private LocalDateTime recommendedAt; // 추천된 시점

    @Column(columnDefinition = "TEXT")
    private String sourceName; // 출처 이름

    @Column(columnDefinition = "TEXT")
    private String reason; // 추천 이유

    private String category; // 기사 카테고리

    @Column(columnDefinition = "TEXT")
    private String keywords; // 관련 키워드

    @CreationTimestamp
    private LocalDateTime createdAt; // 엔티티 생성 시각

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 엔티티 수정 시각

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int views = 0; // 조회수 (기본값 0)
}

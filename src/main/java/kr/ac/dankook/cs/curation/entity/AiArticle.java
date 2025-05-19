package kr.ac.dankook.cs.curation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 추천된 뉴스 기사 정보를 나타내는 엔티티 클래스
 * - 뉴스 데이터 + 추천 관련 정보 저장
 * - DB 테이블명: recommended_articles
 */
@Entity
@Table(name = "Ai_articles")
@Data // Lombok: getter, setter, toString, equals, hashCode 자동 생성
public class AiArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment 기본키
    private Long id;

    @Column(nullable = false)
    private String title; // 기사 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 기사 요약 (긴 내용 가능)

    @Column(nullable = false, unique = true, columnDefinition = "TEXT") // 중복 URL 저장 방지
    private String url; // 원문 URL

    private String author; // 작성자 (null 가능)

    private LocalDateTime publishedAt; // 기사 발행 시점

    private String sourceName; // 출처 이름

    @Column(nullable = false)
    private LocalDateTime recommendedAt; // 추천된 시점 (AI 혹은 수동 추천 시간)

    @Column(columnDefinition = "TEXT")
    private String reason; // 추천 이유 (AI 분석 기반 설명 등)

    private String category; // 기사 카테고리 (예: AI, 보안, 하드웨어 등)

    @Column(columnDefinition = "TEXT")
    private String keywords; // 관련 키워드들 (쉼표 또는 JSON 배열 형태로 저장 가능)

    @CreationTimestamp
    private LocalDateTime createdAt; // 엔티티 최초 생성 시 자동 저장

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 엔티티 갱신 시 자동 업데이트

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int views = 0; // 조회수 (기본값 0)
}

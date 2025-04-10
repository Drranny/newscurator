package kr.ac.dankook.cs.curation.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommended_articles")
@Data
public class RecommendedArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true, length = 2083)
    private String url;

    private String author;

    private LocalDateTime publishedAt;

    private String sourceName;

    @Column(nullable = false)
    private LocalDateTime recommendedAt;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private String category;

    @Column(columnDefinition = "TEXT")
    private String keywords;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

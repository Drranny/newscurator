package kr.ac.dankook.cs.curation.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "news_article")
@Data
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author;
    private String title;
    private String description;
    private String url;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "source_name")
    private String sourceName;
}
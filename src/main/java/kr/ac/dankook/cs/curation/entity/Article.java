package kr.ac.dankook.cs.curation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "articles")
public class Article {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column
    private String category;
    
    @ElementCollection
    @CollectionTable(name = "article_keywords", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "keyword")
    private List<String> keywords;
    
    @Column(name = "source_url")
    private String sourceUrl;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "source_name")
    private String sourceName;
} 
package kr.ac.dankook.cs.curation.login.data;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_article")
@Data
public class UserArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String title; 

    @Column(name = "article_category")
    private String articleCategory;  // AI, 빅데이터, 보안, 하드웨어 중 하나

    private String keywords;

    @Column(name = "total_stay_time")
    private Long totalStayTime = 0L;  // 총 체류 시간 (밀리초 단위)

    @Column(name = "read_count")
    private int readCount = 1;
}

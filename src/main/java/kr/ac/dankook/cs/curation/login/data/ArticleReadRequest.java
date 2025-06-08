package kr.ac.dankook.cs.curation.login.data;

import lombok.Data;

@Data
public class ArticleReadRequest {
    private String title;
    private String articleCategory;
    private String keywords;
}
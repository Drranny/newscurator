package kr.ac.dankook.cs.curation.login.data;

import lombok.Data;

@Data
public class StayTimeRequest {
    private String title;
    private String articleCategory;
    private Long stayTime;
}
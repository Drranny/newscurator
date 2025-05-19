package kr.ac.dankook.cs.curation.login.data;

import lombok.Builder;

@Builder
public record LoginRequest ( // 로그인용 데이터 모델 - Model(DTO) 클래스
    String userId,
    String password
) {
}

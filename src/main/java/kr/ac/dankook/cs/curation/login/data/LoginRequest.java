package kr.ac.dankook.cs.curation.login.data;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest ( // 로그인용 데이터 모델 - Model(DTO) 클래스
    @NotBlank String userId,
    @NotBlank String password
) {
}

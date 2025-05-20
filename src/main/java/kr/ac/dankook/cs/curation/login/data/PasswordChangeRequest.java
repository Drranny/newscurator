package kr.ac.dankook.cs.curation.login.data;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(
    @NotBlank String userId,
    @NotBlank String oldPassword,      // 기존 비밀번호
    @NotBlank String newPassword,
    @NotBlank String confirmPassword
) { }
package kr.ac.dankook.cs.curation.login.data;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank
        String userId,

        @NotBlank
        String password,

        @NotBlank
        String confirmPassword,

        @NotBlank
        String email,

        @NotBlank
        String name
) {
}

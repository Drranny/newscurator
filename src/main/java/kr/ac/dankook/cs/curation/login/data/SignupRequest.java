package kr.ac.dankook.cs.curation.login.data;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
public record SignupRequest(
        @NotBlank(message = "아이디를 입력해주세요")
        String userId,

        @NotBlank(message = "비밀번호를 입력해주세요")
        String password,

        @NotBlank(message = "비밀번호를 다시 입력해주세요")
        String confirmPassword,

        @NotBlank(message = "이메일을 입력해주세요")
        String email,

        @NotBlank(message = "이름을 입력해주세요")
        String name
) {
}

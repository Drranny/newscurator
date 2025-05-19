package ac.kr.dankook.project.login.data;

public record PasswordChangeRequest(
    String userId,
    String newPassword,
    String confirmPassword
) {
}

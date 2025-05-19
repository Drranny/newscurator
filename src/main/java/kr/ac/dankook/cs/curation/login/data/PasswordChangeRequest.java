package kr.ac.dankook.cs.curation.login.data;

public record PasswordChangeRequest(
    String userId,
    String newPassword,
    String confirmPassword
) {
}

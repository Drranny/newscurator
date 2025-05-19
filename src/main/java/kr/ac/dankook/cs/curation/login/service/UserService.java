package kr.ac.dankook.cs.curation.login.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import kr.ac.dankook.cs.curation.login.data.PasswordChangeRequest;
import kr.ac.dankook.cs.curation.login.data.PasswordResetRequest;
import kr.ac.dankook.cs.curation.login.data.SignupRequest;
import kr.ac.dankook.cs.curation.login.data.User;
import kr.ac.dankook.cs.curation.login.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// 실질적으로 회원가입을 처리하는 서비스 클래스
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // 회원가입 메소드
    @Transactional
    public boolean joinMembership(SignupRequest request) {
        if (userRepository.findByLoginId(request.userId()).isPresent()) {
            System.out.println("이미 존재하는 아이디입니다.");
            return false;
        }

         if (!request.password().equals(request.confirmPassword())) {
            System.out.println("비밀번호가 일치하지 않습니다.");
            return false;
    }

        // 사용자 객체 생성 (userNum은 자동 생성)
       User user = User.builder()
                .loginId(request.userId())
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);

        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        user.setEmailVerifyCode(code);
        user.setEmailVerifyCodeExpiry(LocalDateTime.now().plusMinutes(10));  // 10분 유효
        userRepository.save(user);

        // 2) 코드 발송
        emailService.sendVerificationCode(user, code);

        return true;
    }

    /** 인증 코드 재발송 */
    @Transactional
    public void resendVerificationCode(String loginId) {
        User user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("가입되지 않은 사용자"));
        String newCode = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        user.setEmailVerifyCode(newCode);
        user.setEmailVerifyCodeExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        emailService.sendVerificationCode(user, newCode);
    }

    @Transactional
    public void changePassword(PasswordChangeRequest passwordChangeRequest) {
        System.out.println("Attempting to changePassword user: " + passwordChangeRequest.userId());
        Optional<User> memberOpt = userRepository.findByLoginId(passwordChangeRequest.userId());
        if (memberOpt.isPresent()) {
            System.out.println("User ID found: " + passwordChangeRequest.userId());
            System.out.println("Encoding password for user: " + passwordChangeRequest.userId());
            User existingMember  = memberOpt.get();
            existingMember.changePassword(passwordChangeRequest.newPassword(), passwordEncoder);
            userRepository.save(existingMember);

            System.out.println("User successfully changed newPassword: " + passwordChangeRequest.userId());
        }
        else {
            System.out.println("changePassword:: User not found: " + passwordChangeRequest.userId());
        }
    }

    @Transactional
    public void deleteByLoginId(String userId) {
        System.out.println("Attempting to delete userId=" + userId);
        userRepository.deleteByLoginId(userId);
        System.out.println("\n\n\n~~~~~Delete successful! userId=" + userId);
    }

     @Transactional
    public boolean resetPassword(PasswordResetRequest req) {
        var userOpt = userRepository.findByLoginId(req.userId());
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        // 이메일 일치+인증여부 확인
        if (!user.getEmail().equals(req.email()) || !user.isEmailVerified()) {
            return false;
        }

        // 임시 비밀번호 생성 (6자리 숫자 예시)
        String tempPw = String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
        // 엔티티 메서드로 암호화+설정
        user.changePassword(tempPw, passwordEncoder);
        userRepository.save(user);

        // 메일 발송
        emailService.sendTemporaryPassword(user, tempPw);
        return true;
    }
}   

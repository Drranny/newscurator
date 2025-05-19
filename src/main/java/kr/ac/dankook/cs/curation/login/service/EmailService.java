package ac.kr.dankook.project.login.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import ac.kr.dankook.project.login.data.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(User user, String code) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setFrom("wjddks327@gmail.com");
        mail.setSubject("회원가입 인증 코드 안내");
        mail.setText("아래 6자리 코드를 입력하여 이메일 인증을 완료하세요:\n\n" + code + "\n\n(10분 이내에 사용)");
        mailSender.send(mail);
    }

    public void sendTemporaryPassword(User user, String tempPassword) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setFrom("wjddks327@gmail.com");
        mail.setSubject("비밀번호 재설정 안내");
        mail.setText(
            "임시 비밀번호를 발급해 드렸습니다.\n\n" +
            "임시 비밀번호: " + tempPassword + "\n\n" +
            "로그인 후 반드시 비밀번호 변경을 해 주세요."
        );
        mailSender.send(mail);
    }
}
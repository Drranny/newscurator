package ac.kr.dankook.project.service;

import java.util.Date;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ac.kr.dankook.project.Data.LoginDTO;
import ac.kr.dankook.project.Data.RegisterDTO;
import ac.kr.dankook.project.Data.User;
import ac.kr.dankook.project.repository.UserRepository;
import ac.kr.dankook.project.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// 실질적으로 회원가입을 처리하는 서비스 클래스
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtProvider;

    // 회원가입 메소드
    public void joinMembership(RegisterDTO request) {
    request = checkAviliability(request);
        // 사용자 객체 생성 (userNum은 자동 생성)
        User user = new User(
            request.getId(),
            request.getEmail(),  
            request.getNickname(),   
            passwordEncoder.encode(request.getPassword()));

        // 해당 데이터를 DB에 업데이트
        userRepository.save(user);
    }

    // 로그인 메소드
    @Transactional
    public Map<String,String> login(LoginDTO request) {
        User u = userRepository.findById(request.getId())
            .orElseThrow(() -> new RuntimeException("가입되지 않은 사용자"));

        if (!passwordEncoder.matches(request.getPassword(), u.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        String access  = jwtProvider.createAccessToken(u.getId());
        String refresh = jwtProvider.createRefreshToken(u.getId());

        // User 엔티티에 리프레시 토큰과 만료시간 저장
        Date expiry = new Date(System.currentTimeMillis() + jwtProvider.getRefreshExpMs());
        u.setRefreshToken(refresh);
        u.setRefreshTokenExpiry(expiry);
        userRepository.save(u);

        return Map.of(
            "accessToken", access,
            "refreshToken", refresh
        );
    }
    
    // 회원가입 시, 겹치면 안되는 부분 중복확인
    public RegisterDTO checkAviliability(RegisterDTO request) {
        if (userRepository.findById(request.getId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        return request;
    }

    // Access Token 재발급
    public String refresh(String incomingRefresh) {
        // 1) JWT 서명·만료 검사
        if (!jwtProvider.validateToken(incomingRefresh)) {
            throw new RuntimeException("리프레시 토큰이 유효하지 않습니다");
        }
        // 2) DB의 User에서 일치 여부 확인
        User u = userRepository.findByRefreshToken(incomingRefresh)
            .orElseThrow(() -> new RuntimeException("유효하지 않은 리프레시 토큰"));
        if (u.getRefreshTokenExpiry().before(new Date())) {
            throw new RuntimeException("리프레시 토큰이 만료되었습니다");
        }
        // 3) 새 Access Token 발급
        return jwtProvider.createAccessToken(u.getId());
    }
}   

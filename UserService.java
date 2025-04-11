package ac.kr.dankook.project;

import org.springframework.stereotype.Service;

// 실질적으로 회원가입을 처리하는 서비스 클래스
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 회원가입 메소드
    public void joinMembership(RegisterDTO request) {
    request = checkAviliability(request);
        // 사용자 객체 생성 (userNum은 자동 생성)
        User user = new User(
            request.getId(),
            request.getEmail(),  
            request.getNickname(),   
            request.getPassword()
        );

        // 해당 데이터를 DB에 업데이트
        userRepository.save(user);
    }

    // 로그인 메소드
    public boolean login(LoginDTO request) {
        return userRepository.findById(request.getId())
                .map(user -> user.getPassword().equals(request.getPassword())) //Id있으면 password 맞는지 검사
                .orElse(false);
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
}
package ac.kr.dankook.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입 - 데이터 입력이 필요하므로 PostMapping 사용
    @PostMapping("/users")
    public ResponseEntity<String> newUser(@RequestBody RegisterDTO request) { //입력된 JSON형태 데이터를 DTO로 변환
        try {
            userService.joinMembership(request); //입력된 데이터 중복확인 및 회원가입
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 정상적으로 완료되었습니다"); //HTTP 응답 반환(Create상태 - 메시지)
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("회원가입 실패: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO request) {
        boolean result = userService.login(request); //입력된 정보의 id와 pw가 존재 - true
        if (result) {
            return ResponseEntity.status(HttpStatus.OK).body("로그인 성공!"); //HTTP 응답 반환(정상 처리상태 - 메시지)
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) //HTTP 응답 반환(인증 실패상태 - 메시지)
                                 .body("로그인 실패: 아이디 또는 비밀번호 오류");
        }
    }
}
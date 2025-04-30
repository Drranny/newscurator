package ac.kr.dankook.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import ac.kr.dankook.project.Data.LoginDTO;
import ac.kr.dankook.project.Data.RegisterDTO;
import ac.kr.dankook.project.service.UserService;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입 - 데이터 입력이 필요하므로 PostMapping 사용
    @PostMapping("/users")
    public ResponseEntity<String> joinUser(@RequestBody RegisterDTO request) { //입력된 JSON형태 데이터를 RegisterDTO로 변환
        try {
            userService.joinMembership(request); //입력된 데이터 중복확인 및 회원가입
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 정상적으로 완료되었습니다"); // HTTP 201(Created) 상태와 함께 메시지를 본문에 반환
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 실패: " + e.getMessage()); // 예외 발생 - HTTP 400(Bad Request) 상태와 함께 실패 원인 메시지를 본문에 반환
        }
    }

    // 로그인 
    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> signIn(@RequestBody LoginDTO request) { // 입력된 JSON 형태의 로그인 정보를 LoginDTO로 변환
        try {
            Map<String,String> tokens = userService.login(request); // 받은 정보의 유저를 찾아, access-refresh 토큰 발급 및 저장

            return ResponseEntity.ok(tokens); //HTTP 200(OK) 상태와 함께 JWT 동작을 위한 토큰 정보를 본문에 반환
        } catch (RuntimeException e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage())); // 로그인 실패 - HTTP 401(Unauthorized) 상태와 실패 원인 메시지를 본문에 반환
        }
    }

    // 액세스 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<Map<String,String>> refresh(@RequestBody Map<String,String> body) {
        String newAccess = userService.refresh(body.get("refreshToken"));

        return ResponseEntity.ok(Map.of("accessToken", newAccess));
    }
}

package ac.kr.dankook.project.login.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import ac.kr.dankook.project.login.data.LoginRequest;
import ac.kr.dankook.project.login.data.PasswordChangeRequest;
import ac.kr.dankook.project.login.data.PasswordResetRequest;
import ac.kr.dankook.project.login.data.SignupRequest;
import ac.kr.dankook.project.login.data.User;
import ac.kr.dankook.project.login.repository.UserRepository;
import ac.kr.dankook.project.login.security.JwtTokenProvider;
import ac.kr.dankook.project.login.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:8080", allowedHeaders = "*")
@RequiredArgsConstructor
public class UserController {

    private static final String FRONT_BASE_URL = "http://localhost:8080";

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtProvider;

    /** 회원가입 → 201 Created + verifyUrl 포함 */
    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> signup(@RequestBody @Valid SignupRequest req) {
        boolean created = userService.joinMembership(req);
        if (!created) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", "이미 존재하는 아이디·이메일이거나 비밀번호 불일치"));
        }

        String verifyUrl = FRONT_BASE_URL + "/auth/verify";

        return ResponseEntity
        .created(URI.create(verifyUrl))
        .body(Map.of(
            "message",   "회원가입 성공! 이메일 인증을 완료해주세요.",
            "verifyUrl", verifyUrl
        ));
    }

    /** 로그인 → 미인증 시 재발송+URL, 그 외 인증매니저→JWT 발급 */
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        
        Optional<User> opt = userRepository.findByLoginId(req.userId());
        if (opt.isEmpty()) {
            // 가입되지 않은 사용자
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "가입되지 않은 사용자입니다."));
    }
    
    User user = opt.get();

        // 1) 이메일 미인증 → 코드 재발송 + verify URL
        if (!user.isEmailVerified()) {
            userService.resendVerificationCode(req.userId());
            String verifyUrl = FRONT_BASE_URL + "/auth/verify";

            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "error",     "이메일 인증이 필요합니다. 인증 코드를 재발송했습니다.",
                    "verifyUrl", verifyUrl
                ));
        }

        // 2) 패스워드 검증
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    req.userId(),
                    req.password()
                )
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (BadCredentialsException ex) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "아이디 또는 비밀번호가 올바르지 않습니다."));
        }

        // 3) JWT 발급 및 RefreshToken 저장
        String accessToken  = jwtProvider.createAccessToken(req.userId());
        String refreshToken = jwtProvider.createRefreshToken(req.userId());
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(new Date(System.currentTimeMillis() + jwtProvider.getRefreshExpMs()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "accessToken",  accessToken,
            "refreshToken", refreshToken
        ));
    }

     @PostMapping("/verify")
    public String verifyEmail(@RequestParam String userId, @RequestParam String code, Model model) {

        Optional<User> optUser = userRepository.findByLoginId(userId);
        if (optUser.isEmpty()) {
            model.addAttribute("error", "가입되지 않은 사용자입니다.");
            model.addAttribute("userId", userId);
            return "verify";
        }
        
        User user = optUser.get();

        // 2) 코드 만료 체크
        if (user.getEmailVerifyCodeExpiry().isBefore(LocalDateTime.now())) {
            userService.resendVerificationCode(userId);
            model.addAttribute("error", "인증 코드가 만료되어 재발송했습니다.");
            return "verify";
        }

        // 3) 코드 불일치
        if (!code.equals(user.getEmailVerifyCode())) {
            model.addAttribute("error", "인증 코드가 일치하지 않습니다.");
            return "verify";
        }

        // 4) 인증 성공
        user.setEmailVerified(true);
        user.setEmailVerifyCode(null);
        user.setEmailVerifyCodeExpiry(null);
        userRepository.save(user);

        model.addAttribute("message", "이메일 인증이 완료되었습니다!");
        model.addAttribute("loginUrl", FRONT_BASE_URL + "/auth/login");
        return "login";    
    }

    /** 비밀번호 변경 */
    @PostMapping("/changepw")
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestBody @Valid PasswordChangeRequest req) {

        if (!req.newPassword().equals(req.confirmPassword())) {
            return ResponseEntity
                .badRequest()
                .body(Map.of("error", "Passwords do not match"));
        }

        userService.changePassword(req);
        return ResponseEntity.ok(Map.of(
            "message", "비밀번호가 변경되었습니다.",
        "loginUrl",   FRONT_BASE_URL + "/auth/login"));
    }

    /** 회원탈퇴 */
    @DeleteMapping("/delete")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@RequestBody @Valid LoginRequest req) {
        String loginId = req.userId(); 
        userService.deleteByLoginId(loginId);
        return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
    }

     @PostMapping("/resetpw")
    @ResponseBody
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid PasswordResetRequest req) {

        boolean ok = userService.resetPassword(req);
        if (!ok) {
            return ResponseEntity
                .badRequest()
                .body(Map.of("error",
                    "아이디/이메일 불일치 또는 이메일 인증이 필요합니다."));
        }

        return ResponseEntity.ok(Map.of(
            "message",    "임시 비밀번호를 이메일로 발송했습니다.",
            "loginUrl",   FRONT_BASE_URL + "/auth/login"
        ));
    }

    /** 뷰 렌더링 */
    @GetMapping("/login")
    public String loginPage() {
        return "login";    // templates/login.html
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";   // templates/signup.html
    }

    @GetMapping("/changepw")
    public String changepwPage() {
        return "changepw"; // templates/changepw.html
    }

    @GetMapping("/delete")
    public String deletePage() {
        return "delete"; // templates/delete.html
    }

    @GetMapping("/resetpw")
    public String forgotpwPage() {
        return "resetpw";  // templates/resetpw.html
    }

    @GetMapping("/verify")
    public String verifyPage() {
        return "verify";   // templates/verify.html
    }
}
package kr.ac.dankook.cs.curation.login.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.ac.dankook.cs.curation.login.data.LoginRequest;
import kr.ac.dankook.cs.curation.login.data.PasswordChangeRequest;
import kr.ac.dankook.cs.curation.login.data.PasswordResetRequest;
import kr.ac.dankook.cs.curation.login.data.SignupRequest;
import kr.ac.dankook.cs.curation.login.data.User;
import kr.ac.dankook.cs.curation.login.repository.UserRepository;
import kr.ac.dankook.cs.curation.login.security.JwtTokenProvider;
import kr.ac.dankook.cs.curation.login.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
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

    @PostMapping("/signup")
    public String signup( 
        @Valid @ModelAttribute SignupRequest req,
        BindingResult br,
        RedirectAttributes ra) {
    // 1) 빈칸 검증 실패
    if (br.hasErrors()) {
        // *모든* 칸이 비었든 일부만 비었든, 뭉뚱그려 하나의 메시지
        ra.addFlashAttribute("errorMsg", "모든 항목을 빠짐없이 입력해 주세요.");
        return "redirect:/auth/signup";
    }
        boolean created = userService.joinMembership(req);
        if (!created) {
            ra.addFlashAttribute("errorMsg", "회원가입에 실패했습니다. 아이디가 중복되었거나 비밀번호가 일치하지 않습니다.");
            return "redirect:/auth/signup";
        }
        return "redirect:/auth/login";
    }

    /** 로그인 → PRG 패턴 적용 */
    @PostMapping("/login")
    public String login(
            @RequestParam String userId,
            @RequestParam String password,
            RedirectAttributes redirectAttrs,
            HttpServletResponse response
    ) {
        Optional<User> opt = userRepository.findByLoginId(userId);
        if (opt.isEmpty()) {
            redirectAttrs.addFlashAttribute("errorMsg", "가입되지 않은 사용자입니다.");
            return "redirect:/auth/login";
        }
        User user = opt.get();

        if (!user.isEmailVerified()) {
            userService.resendVerificationCode(userId);
            redirectAttrs.addFlashAttribute("errorMsg", "이메일 인증이 필요합니다. 인증 코드를 재발송했습니다.");
            return "redirect:/auth/verify";
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userId, password)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (BadCredentialsException ex) {
            redirectAttrs.addFlashAttribute("errorMsg", "비밀번호가 일치하지 않습니다.");
            return "redirect:/auth/login";
        }

        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(new Date(System.currentTimeMillis() + jwtProvider.getRefreshExpMs()));
        userRepository.save(user);

        ResponseCookie acCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true).path("/")
                .maxAge(jwtProvider.getAccessExpMs() / 1000)
                .build();
        ResponseCookie rcCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true).path("/")
                .maxAge(jwtProvider.getRefreshExpMs() / 1000)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, acCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, rcCookie.toString());

        return "redirect:/";
    }

    /** 이메일 인증 → PRG 패턴 적용 */
    @PostMapping("/verify")
    public String verifyEmail(
            @RequestParam String userId,
            @RequestParam String code,
            RedirectAttributes redirectAttrs
    ) {
        Optional<User> optUser = userRepository.findByLoginId(userId);
        if (optUser.isEmpty()) {
            redirectAttrs.addFlashAttribute("errorMsg", "가입되지 않은 사용자입니다.");
            redirectAttrs.addFlashAttribute("userId", userId);
            return "redirect:/auth/verify";
        }
        User user = optUser.get();

        if (user.getEmailVerifyCodeExpiry().isBefore(LocalDateTime.now())) {
            userService.resendVerificationCode(userId);
            redirectAttrs.addFlashAttribute("errorMsg", "인증 코드가 만료되어 재발송했습니다.");
            return "redirect:/auth/verify";
        }

        if (!code.equals(user.getEmailVerifyCode())) {
            redirectAttrs.addFlashAttribute("errorMsg", "인증 코드가 일치하지 않습니다.");
            return "redirect:/auth/verify";
        }

        user.setEmailVerified(true);
        user.setEmailVerifyCode(null);
        user.setEmailVerifyCodeExpiry(null);
        userRepository.save(user);

        return "redirect:/auth/login";
    }

    @GetMapping("/verify/resend")
    public String resendCode(@RequestParam String userId, RedirectAttributes ra) {
        userService.resendVerificationCode(userId);
        ra.addFlashAttribute("errorMsg", "인증 코드를 재전송했습니다.");
        
        ra.addFlashAttribute("userId", userId);
        return "redirect:/auth/verify";
    }

    /** 비밀번호 변경 */
    @PostMapping("/changepw")
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestBody @Valid PasswordChangeRequest req) {

        if (!req.newPassword().equals(req.confirmPassword())) {
            return ResponseEntity
                .badRequest()
                .body(Map.of("error", "비밀번호가 일치하지 않습니다"));
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

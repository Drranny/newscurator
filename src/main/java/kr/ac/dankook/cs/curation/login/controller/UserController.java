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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // 1) 현재 인증 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            // 2) DB에서 해당 사용자의 refreshToken 초기화
            Optional<User> optUser = userRepository.findByLoginId(auth.getName());
            if (optUser.isPresent()) {
                User user = optUser.get();
                user.setRefreshToken(null);
                user.setRefreshTokenExpiry(null);
                userRepository.save(user);
            }
            // 3) 시큐리티 컨텍스트 무효화
            SecurityContextHolder.clearContext();
        }

        // 4) accessToken, refreshToken 쿠키 삭제
        ResponseCookie deleteAccess = ResponseCookie.from("accessToken", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)       // 즉시 만료
                .build();
        ResponseCookie deleteRefresh = ResponseCookie.from("refreshToken", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());

        // 5) 홈(index) 페이지로 리다이렉트
        return "redirect:/";
    }

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
    public String changePassword(
        @Valid @ModelAttribute("passwordChangeRequest")
          PasswordChangeRequest req,
        BindingResult br,
        RedirectAttributes ra
    ) {
        // 1) 필수 입력 체크
        if (br.hasErrors()) {
            ra.addFlashAttribute("errorMsg", "모든 항목을 빠짐없이 입력해 주세요.");
            ra.addFlashAttribute("passwordChangeRequest", req);
            return "redirect:/auth/changepw";
        }
        // 2) 새 비밀번호 일치 체크
        if (!req.newPassword().equals(req.confirmPassword())) {
            ra.addFlashAttribute("errorMsg", "새 비밀번호와 확인이 일치하지 않습니다.");
            ra.addFlashAttribute("passwordChangeRequest", req);
            return "redirect:/auth/changepw";
        }
        // 3) 사용자 조회
        var opt = userRepository.findByLoginId(req.userId());
        if (opt.isEmpty()) {
            ra.addFlashAttribute("errorMsg", "사용자를 찾을 수 없습니다.");
            return "redirect:/auth/changepw";
        }
        var user = opt.get();
        // 4) 기존 비밀번호 확인
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            ra.addFlashAttribute("errorMsg", "기존 비밀번호가 일치하지 않습니다.");
            ra.addFlashAttribute("passwordChangeRequest", req);
            return "redirect:/auth/changepw";
        }
        // 5) 새 비밀번호 ≠ 기존 비밀번호
        if (passwordEncoder.matches(req.newPassword(), user.getPassword())) {
            ra.addFlashAttribute("errorMsg", "새 비밀번호는 기존 비밀번호와 달라야 합니다.");
            ra.addFlashAttribute("passwordChangeRequest", req);
            return "redirect:/auth/changepw";
        }
        // 6) 변경 실행
        user.changePassword(req.newPassword(), passwordEncoder);
        userRepository.save(user);

        ra.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
        return "redirect:/auth/login";
    }

    /** 회원탈퇴 */
    @PostMapping("/delete")
    public String deleteUser(
        @Valid @ModelAttribute("loginRequest") LoginRequest req,
        BindingResult br,
        RedirectAttributes ra ) {

            if (br.hasErrors()) {
                ra.addFlashAttribute("errorMsg", "아이디와 비밀번호를 입력해 주세요.");
                return "redirect:/auth/delete";
            }
        
            var opt = userRepository.findByLoginId(req.userId());
            if (opt.isEmpty()) {
                ra.addFlashAttribute("errorMsg", "가입되지 않은 사용자입니다.");
                return "redirect:/auth/delete";
            }
            if (!passwordEncoder.matches(req.password(), opt.get().getPassword())) {
                ra.addFlashAttribute("errorMsg", "비밀번호가 일치하지 않습니다.");
                return "redirect:/auth/delete";
            }

            userService.deleteByLoginId(req.userId());
            ra.addFlashAttribute("message", "회원탈퇴가 완료되었습니다.");
            return "redirect:/";
        }

    @PostMapping("/resetpw")
    public String resetPassword(
        @Valid @ModelAttribute("passwordResetRequest")
          PasswordResetRequest req,
        BindingResult br,
        RedirectAttributes ra
    ) {
        // 1) 빈칸/형식 검증
        if (br.hasErrors()) {
            ra.addFlashAttribute("errorMsg", "아이디와 이메일을 올바르게 입력해 주세요.");
            ra.addFlashAttribute("passwordResetRequest", req);
            return "redirect:/auth/resetpw";
        }

        // 2) 실제 초기화 시도
        boolean ok = userService.resetPassword(req);
        if (!ok) {
            ra.addFlashAttribute("errorMsg", "아이디/이메일 불일치 또는 이메일 인증이 필요합니다.");
            ra.addFlashAttribute("passwordResetRequest", req);
            return "redirect:/auth/resetpw";
        }

        // 3) 성공
        ra.addFlashAttribute("message", "임시 비밀번호를 이메일로 발송했습니다.");
        return "redirect:/auth/login";
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
    public String changepwPage(Model model) {
        // 폼 바인딩을 위해 빈 DTO를 미리 제공
        model.addAttribute("passwordChangeRequest",
        new PasswordChangeRequest("", "", "", ""));
        return "changepw";
    }

    @GetMapping("/delete")
    public String deletePage(Model m) {
        m.addAttribute("loginRequest", new LoginRequest("", ""));
        return "delete";  // templates/delete.html
    }

    @GetMapping("/resetpw")
    public String resetpwPage(Model model) {
        model.addAttribute("passwordResetRequest",
            new PasswordResetRequest("", ""));
        return "resetpw";
    }

    @GetMapping("/verify")
    public String verifyPage() {
        return "verify";   // templates/verify.html
    }
}

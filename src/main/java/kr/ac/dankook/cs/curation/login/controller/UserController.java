package kr.ac.dankook.cs.curation.login.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.swing.Spring;

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

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    // 로그아웃 기능
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
            // 3) 시큐리티 컨텍스트 초기화
            SecurityContextHolder.clearContext();
        }

        // 4) accessToken, refreshToken 쿠키 삭제
        ResponseCookie deleteAccess = ResponseCookie.from("accessToken", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)       
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

    // 회원가입
    @PostMapping("/signup")
    public String signup( 
        @Valid @ModelAttribute SignupRequest req,
        BindingResult br,
        RedirectAttributes ra) {

    // 1) 내용을 적지 않은 칸이 있을 때 오류 메시지 출력
    if (br.hasErrors()) {
        ra.addFlashAttribute("errorMsg", "모든 항목을 빠짐없이 입력해 주세요.");
        return "redirect:/auth/signup";
    }
    // 2) 모든 칸에 정보가 적혔을 때 아이디 중복 및 비밀번호 일치 확인
        boolean created = userService.joinMembership(req); 
        if (!created) {
            ra.addFlashAttribute("errorMsg", "회원가입에 실패했습니다. 아이디가 중복되었거나 비밀번호가 일치하지 않습니다.");
            return "redirect:/auth/signup";
        }

    // 3) 문제 없이 회원가입 시 로그인 화면으로 리다이렉트
        return "redirect:/auth/login";
    }

    // 로그인
    @PostMapping("/login")
    public String login(
            @RequestParam String userId,
            @RequestParam String password,
            RedirectAttributes redirectAttrs,
            HttpServletResponse response
    ) {
        //입력된 id로 검색
        Optional<User> opt = userRepository.findByLoginId(userId);
        
        // 해당 id로 회원가입 되어있는지 확인
        if (opt.isEmpty()) {
            redirectAttrs.addFlashAttribute("errorMsg", "가입되지 않은 사용자입니다.");
            return "redirect:/auth/login";
        }
        // 되어있으면 정보를 가져옴
        User user = opt.get();

        // 이메일 인증이 완료되었는지 확인
        if (!user.isEmailVerified()) {
            userService.resendVerificationCode(userId);
            redirectAttrs.addFlashAttribute("errorMsg", "이메일 인증이 필요합니다. 인증 코드를 재발송했습니다.");
            return "redirect:/auth/verify";
        }

        try {
            // Spring Security를 통한 아이디-비밀번호 인증 시도
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userId, password)
            );

            // 인증 성공 시 인증 정보를 SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (BadCredentialsException ex) {
            redirectAttrs.addFlashAttribute("errorMsg", "비밀번호가 일치하지 않습니다.");
            return "redirect:/auth/login";
        }

        //access, refresh 토큰 발급
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

    //이메일 인증
    @PostMapping("/verify")
    public String verifyEmail(
            @RequestParam String userId,
            @RequestParam String code,
            RedirectAttributes redirectAttrs
    ) {
        // 입력된 id로 사용자 검색
        Optional<User> optUser = userRepository.findByLoginId(userId);

        // 해당 유저가 없으면 에러메시지
        if (optUser.isEmpty()) {
            redirectAttrs.addFlashAttribute("errorMsg", "가입되지 않은 사용자입니다.");
            redirectAttrs.addFlashAttribute("userId", userId);
            return "redirect:/auth/verify";
        }
        // 있으면 정보를 가져옴
        User user = optUser.get();

        // 설정한 시간이 넘었으면 코드 재발송
        if (user.getEmailVerifyCodeExpiry().isBefore(LocalDateTime.now())) {
            userService.resendVerificationCode(userId);
            redirectAttrs.addFlashAttribute("errorMsg", "인증 코드가 만료되어 재발송했습니다.");
            return "redirect:/auth/verify";
        }

        // 인증코드 불일치 시 에러메시지
        if (!code.equals(user.getEmailVerifyCode())) {
            redirectAttrs.addFlashAttribute("errorMsg", "인증 코드가 일치하지 않습니다.");
            return "redirect:/auth/verify";
        }

        // 인증 성공 시 emailVerified를 true로 변경 및 나머지 이메일 관련 필드 null
        user.setEmailVerified(true);
        user.setEmailVerifyCode(null);
        user.setEmailVerifyCodeExpiry(null);
        userRepository.save(user);

        return "redirect:/auth/login";
    }

    // 비밀번호 변경
    @PostMapping("/changepw")
    public String changePassword(
        @Valid @ModelAttribute("passwordChangeRequest")
          PasswordChangeRequest req,
        BindingResult br,
        RedirectAttributes ra
    ) {
        // 필수 입력 체크
        if (br.hasErrors()) {
            ra.addFlashAttribute("errorMsg", "모든 항목을 빠짐없이 입력해 주세요.");
            ra.addFlashAttribute("passwordChangeRequest", req);
            return "redirect:/auth/changepw";
        }
        // 새 비밀번호 일치 체크
        if (!req.newPassword().equals(req.confirmPassword())) {
            ra.addFlashAttribute("errorMsg", "새 비밀번호와 확인이 일치하지 않습니다.");
            ra.addFlashAttribute("passwordChangeRequest", req);
            return "redirect:/auth/changepw";
        }
        // 사용자 조회
        var opt = userRepository.findByLoginId(req.userId());
        if (opt.isEmpty()) {
            ra.addFlashAttribute("errorMsg", "사용자를 찾을 수 없습니다.");
            return "redirect:/auth/changepw";
        }
        var user = opt.get();
        // 기존 비밀번호 확인
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            ra.addFlashAttribute("errorMsg", "기존 비밀번호가 일치하지 않습니다.");
            ra.addFlashAttribute("passwordChangeRequest", req);
            return "redirect:/auth/changepw";
        }
        // 새 비밀번호 ≠ 기존 비밀번호
        if (passwordEncoder.matches(req.newPassword(), user.getPassword())) {
            ra.addFlashAttribute("errorMsg", "새 비밀번호는 기존 비밀번호와 달라야 합니다.");
            ra.addFlashAttribute("passwordChangeRequest", req);
            return "redirect:/auth/changepw";
        }
        // 변경 실행
        user.changePassword(req.newPassword(), passwordEncoder);
        userRepository.save(user);

        ra.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
        return "redirect:/auth/login";
    }

    // 회원탈퇴 
    @PostMapping("/delete")
    public String deleteUser(
        @Valid @ModelAttribute("loginRequest") LoginRequest req,
        BindingResult br,
        RedirectAttributes ra ) {

            // 필수 입력 체크
            if (br.hasErrors()) {
                ra.addFlashAttribute("errorMsg", "아이디와 비밀번호를 입력해 주세요.");
                return "redirect:/auth/delete";
            }    
            var opt = userRepository.findByLoginId(req.userId());

            // 사용자 조회
            if (opt.isEmpty()) {
                ra.addFlashAttribute("errorMsg", "가입되지 않은 사용자입니다.");
                return "redirect:/auth/delete";
            }

            // 사용자 비밀번호 체크
            if (!passwordEncoder.matches(req.password(), opt.get().getPassword())) {
                ra.addFlashAttribute("errorMsg", "비밀번호가 일치하지 않습니다.");
                return "redirect:/auth/delete";
            }

            // 탈퇴 실행
            userService.deleteByLoginId(req.userId());
            ra.addFlashAttribute("message", "회원탈퇴가 완료되었습니다.");
            return "redirect:/";
        }

    // 비밀번호 초기화
    @PostMapping("/resetpw")
    public String resetPassword(
        @Valid @ModelAttribute("passwordResetRequest")
          PasswordResetRequest req,
        BindingResult br,
        RedirectAttributes ra
    ) {
        // 필수 입력 체크
        if (br.hasErrors()) {
            ra.addFlashAttribute("errorMsg", "아이디와 이메일을 올바르게 입력해 주세요.");
            ra.addFlashAttribute("passwordResetRequest", req);
            return "redirect:/auth/resetpw";
        }

        // 실제 초기화 시도
        boolean ok = userService.resetPassword(req);
        if (!ok) {
            ra.addFlashAttribute("errorMsg", "아이디/이메일 불일치 또는 이메일 인증이 필요합니다.");
            ra.addFlashAttribute("passwordResetRequest", req);
            return "redirect:/auth/resetpw";
        }

        // 초기화 성공 시 메시지 출력
        ra.addFlashAttribute("message", "임시 비밀번호를 이메일로 발송했습니다.");
        return "redirect:/auth/login";
    }

    // 뷰 렌더링 
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

package kr.ac.dankook.cs.curation.login.security;

import kr.ac.dankook.cs.curation.login.service.UserDetailService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtProvider;
    private final UserDetailService userDetailService;

    // 특정 쿠키 추출 메서드
    private String getCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
                                    throws ServletException, IOException {
        try {
            // accessToken 쿠키 확인
            String accessToken = getCookie(req, "accessToken");
            if (accessToken != null && jwtProvider.validateToken(accessToken)) {
                // 정상 토큰 → 바로 인증 컨텍스트 세팅
                String userId = jwtProvider.getUserId(accessToken);
                UserDetails ud = userDetailService.loadUserByUsername(userId);
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        ud, null, ud.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

            } else {
                // accessToken 만료 또는 없음 → refreshToken 쿠키 확인
                String refreshToken = getCookie(req, "refreshToken");
                if (refreshToken != null && jwtProvider.validateToken(refreshToken)) {
                    // 재발급
                    String newAccess = jwtProvider.refresh(refreshToken);

                    // 새 accessToken 쿠키로 갱신
                    ResponseCookie acCookie = ResponseCookie.from("accessToken", newAccess)
                        .httpOnly(true)
                        .path("/")
                        .maxAge(jwtProvider.getAccessExpMs() / 1000)
                        .build();
                    res.setHeader(HttpHeaders.SET_COOKIE, acCookie.toString());

                    // 인증 컨텍스트 세팅
                    String userId = jwtProvider.getUserId(newAccess);
                    UserDetails ud = userDetailService.loadUserByUsername(userId);
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            ud, null, ud.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                // refreshToken 없거나 만료되면 그냥 통과
            }
        } catch (JwtException | IllegalArgumentException ex) {
            // 토큰 파싱·검증 에러 시 SecurityContext 초기화
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(req, res);
    }
}

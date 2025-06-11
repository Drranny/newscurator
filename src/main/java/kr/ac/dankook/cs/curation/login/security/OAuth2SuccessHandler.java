package kr.ac.dankook.cs.curation.login.security;

import kr.ac.dankook.cs.curation.login.data.User;
import kr.ac.dankook.cs.curation.login.repository.UserRepository;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

// OAuth2 로그인 성공 시 동작하는 핸들러
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtProvider;
    @Override
public void onAuthenticationSuccess(
    HttpServletRequest  req,
    HttpServletResponse res,
    Authentication      auth
) throws IOException {
     // 인증된 사용자 정보(OAuth2User)에서 이메일과 토큰 정보 추출
    DefaultOAuth2User oauthUser = (DefaultOAuth2User) auth.getPrincipal();
    String email = oauthUser.getAttribute("email");
    String accessToken  = (String) oauthUser.getAttributes().get("accessToken");
    String refreshToken = (String) oauthUser.getAttributes().get("refreshToken");

    // JWT 토큰을 HttpOnly 쿠키로 설정하여 클라이언트에 전달
    ResponseCookie ac = ResponseCookie.from("accessToken", accessToken).httpOnly(true).path("/").maxAge(jwtProvider.getAccessExpMs()/1000).build();
    ResponseCookie rc = ResponseCookie.from("refreshToken", refreshToken).httpOnly(true).path("/").maxAge(jwtProvider.getRefreshExpMs()/1000).build();
    res.addHeader(HttpHeaders.SET_COOKIE, ac.toString());
    res.addHeader(HttpHeaders.SET_COOKIE, rc.toString());

    res.sendRedirect("/");
}
}

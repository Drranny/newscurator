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
    DefaultOAuth2User oauthUser = (DefaultOAuth2User) auth.getPrincipal();
    String email = oauthUser.getAttribute("email");


        String accessToken  = (String) oauthUser.getAttributes().get("accessToken");
        String refreshToken = (String) oauthUser.getAttributes().get("refreshToken");

       ResponseCookie ac = ResponseCookie.from("accessToken", accessToken)
        .httpOnly(true).path("/").maxAge(jwtProvider.getAccessExpMs()/1000).build();
    ResponseCookie rc = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true).path("/").maxAge(jwtProvider.getRefreshExpMs()/1000).build();
    res.addHeader(HttpHeaders.SET_COOKIE, ac.toString());
    res.addHeader(HttpHeaders.SET_COOKIE, rc.toString());

    // 3) 홈으로 리다이렉트
    res.sendRedirect("/");
}
}
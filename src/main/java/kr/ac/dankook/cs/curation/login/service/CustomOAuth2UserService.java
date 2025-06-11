package kr.ac.dankook.cs.curation.login.service;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import kr.ac.dankook.cs.curation.login.data.OAuthAttributes;
import kr.ac.dankook.cs.curation.login.data.User;
import kr.ac.dankook.cs.curation.login.repository.UserRepository;
import kr.ac.dankook.cs.curation.login.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider  jwtProvider;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // OAuth2 공급자에서 사용자 정보 조회
        OAuth2User oauthUser = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google" or "kakao"
        String userNameAttr    = userRequest.getClientRegistration()
                                            .getProviderDetails()
                                            .getUserInfoEndpoint()
                                            .getUserNameAttributeName();

        // oauth2 관련 정보를 저장할 객체
        OAuthAttributes attrs = OAuthAttributes.of(registrationId, userNameAttr, oauthUser.getAttributes());

        // 입력된 정보로 검색 - 없으면 생성
        User user = userRepository.findByLoginId(attrs.getEmail())
            .orElseGet(() -> userRepository.save(attrs.toEntity()));

        // 토큰 발급
        String accessToken  = jwtProvider.createAccessToken(user.getLoginId());
        String refreshToken = jwtProvider.createRefreshToken(user.getLoginId());

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(new Date(System.currentTimeMillis() + jwtProvider.getRefreshExpMs()));
        userRepository.save(user);

        // OAuth2User attributes 맵에 JWT 토큰 추가
        Map<String,Object> attributes = new HashMap<>(oauthUser.getAttributes());
        attributes.put("accessToken",  accessToken);
        attributes.put("refreshToken", refreshToken);

        // Spring Security에 사용할 DefaultOAuth2User 반환
        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            attrs.getNameAttributeKey()
        );
    }
}

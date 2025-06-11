package kr.ac.dankook.cs.curation.login.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;       
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import kr.ac.dankook.cs.curation.login.service.CustomOAuth2UserService;
import kr.ac.dankook.cs.curation.login.service.UserDetailService;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final UserDetailService userdetailService;

     // AuthenticationManager는 login 시 인증에 사용
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    // 패스워드 인코더로 사용할 빈 등록
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
         http
            .cors(Customizer.withDefaults()) // cors 설정을 WebMvcConfigurer로 위임
            .csrf(AbstractHttpConfigurer::disable) // crsf disable for REST
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
            // 인증 없이 접근 가능한 경로
                .requestMatchers(
                    "/",
                    "/auth/**",         
                    "/oauth2/**",        
                    "/api/fetch/**",
		            "/timeline",

                    "/js/**",
                    "/logo/**",
                    "/memes/**"
                ).permitAll()
                .anyRequest().authenticated()

            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/auth/login") // OAuth2 로그인 진입 시 사용할 로그인 페이지 경로 지정
              .failureUrl("/auth/login?error")  // OAuth2 실패 시 '?error' 붙여 리다이렉트
                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService)) // OAuth2 로그인 성공 후 사용자 정보 조회 서비스 설정 (jwt 발급)
                .successHandler(oAuth2SuccessHandler) // 사용자 정보 조회 및 JWT 발급 후 성공 처리 핸들러
            )
            .userDetailsService(userdetailService) 
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);  // 요청마다 JWT(accessToken, refreshToken) 확인 → 인증 정보 설정
        return http.build();
    }
}

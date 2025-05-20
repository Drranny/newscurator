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
            //.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/auth/**",         // 로그인·회원가입·검증 모두 허용
                    "/oauth2/**",        // OAuth2 엔드포인트
                    "/api/fetch/**",

                    "/js/**",
                    "/logo/**",
                    "/memes/**"
                ).permitAll()
                .anyRequest().authenticated()

            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/auth/login")         // (1) 로그인 화면 URL
              .failureUrl("/auth/login?error")  // (2) OAuth2 실패 시 '?error' 붙여 리다이렉트
                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
            )
            .userDetailsService(userdetailService) // 명시적으로 설정
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

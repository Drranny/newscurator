package ac.kr.dankook.project.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig { //SpringSecurity를 이용한 보안 설정 모음 클래스입니다.

    private final JwtTokenProvider jwtProvider;

    // 1) 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // 2) AuthenticationManager 노출 (필요시)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 3) 보안 필터 체인
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

      return 
      http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth.requestMatchers("/auth/users", "/auth/login", "/auth/refresh")
        .permitAll())
        .addFilterBefore(new JwtTokenFilter(jwtProvider),UsernamePasswordAuthenticationFilter.class)
        .build();
    }
}

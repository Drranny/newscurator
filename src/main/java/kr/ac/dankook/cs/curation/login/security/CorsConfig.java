package ac.kr.dankook.project.login.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**") // 모든 엔드포인트에 대해
            .allowedOriginPatterns("http://localhost:8080") // http://localhost:8080 도메인 허용
            .allowedMethods("*") // 모든 HTTP 메소드 허용
            .allowedHeaders("*") // 모든 헤더 허용
            .allowCredentials(true)  // 자격 증명 허용 쿠기전달 비허용
            .maxAge(3600);
    }
}

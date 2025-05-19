package ac.kr.dankook.project.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtProvider; 

    // 모든 HTTP 요청마다 단 한 번 실행되는 필터 메서드.
    // OncePerRequestFilter 덕분에 같은 요청에 중복 실행되지 않습니다.
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain ) throws ServletException, IOException {

        String token = jwtProvider.resolveToken(req); //토큰만 저장

        if (token != null && jwtProvider.validateToken(token)) { //유효성 있는 토큰이 존재하면

            String userId = jwtProvider.getUserId(token);
            Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, java.util.List.of()); // JWT검증이므로 비밀번호 검증을 따로 하지 않음 / 권한 없으므로 빈 리스트
            SecurityContextHolder.getContext().setAuthentication(auth); //현재 실행중인 보안 컨텍스트에 인증 정보 저장

        }

        chain.doFilter(req, res);
    }
}

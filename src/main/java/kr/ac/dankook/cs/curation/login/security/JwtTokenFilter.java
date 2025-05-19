package ac.kr.dankook.project.login.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import ac.kr.dankook.project.login.service.UserDetailService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtProvider; 
    private final UserDetailService userDetailService;
    
    // 모든 HTTP 요청마다 단 한 번 실행되는 필터 메서드.
    // OncePerRequestFilter 덕분에 같은 요청에 중복 실행되지 않습니다.
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        
    String token = jwtProvider.resolveToken(req);
    if (token != null) {
        try {
            if (jwtProvider.validateToken(token)) {
                // 정상 토큰
                String username = jwtProvider.getUserId(token);
                UserDetails userDetails = userDetailService.loadUserByUsername(username);
                Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

            } else {
                // 토큰 만료 → 재발급 시도
                String refresh = jwtProvider.resolveRefreshToken(req);
                    if (refresh != null && jwtProvider.validateToken(refresh)) {
                        // 유효한 Refresh 토큰으로 새 Access 발급
                        String newAccess = jwtProvider.refresh(refresh);

                        String username = jwtProvider.getUserId(newAccess);
                        UserDetails userDetails = userDetailService.loadUserByUsername(username);
                        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        // 새 Access를 응답 헤더에 추가
                        res.setHeader("Authorization", "Bearer " + newAccess);
                    } else {
                        // 리프레시도 실패
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                      "토큰이 만료되어 재발급에 실패했습니다.");
                        return;
                }
            }
        } catch (RuntimeException  ex) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
            return;
        }
    }
    chain.doFilter(req, res);
}
}

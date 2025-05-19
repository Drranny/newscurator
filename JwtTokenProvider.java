package ac.kr.dankook.project.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.accessExpirationMs}")
    private long accessExpMs;

    @Value("${jwt.refreshExpirationMs}")
    private long refreshExpMs;

    private Key key;

    @PostConstruct
    public void init() {
        // Base64로 인코딩된 secretKey를 디코드해서 HMAC-SHA 키로 사용
        byte[] decoded = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(decoded);
    }

     //  Access Token 생성
     public String createAccessToken(String userId) {
        Date now = new Date();
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + accessExpMs))
            .signWith(key)
            .compact();
    }

    //  Refresh Token 생성
    public String createRefreshToken(String userId) {
        Date now = new Date();
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + refreshExpMs))
            .signWith(key)
            .compact();
    }

    //  토큰에서 사용자 식별자 추출
    public String getUserId(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    //  토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jws<Claims> c = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return !c.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    //  HTTP 헤더에서 “Bearer {token}” 부분만 꺼내오기
    public String resolveToken(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            return h.substring(7);
        }
        return null;
    }

     // 리프레시 만료 기간 접근자
     public long getRefreshExpMs() {
        return refreshExpMs;
    }
}

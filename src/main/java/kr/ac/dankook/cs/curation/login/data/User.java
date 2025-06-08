package kr.ac.dankook.cs.curation.login.data;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@Getter
@ToString
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usernum;

    @Column(unique = true, nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    private String refreshToken;
    private Date refreshTokenExpiry;

    @Column(nullable = false)
    private boolean emailVerified = false;

    private String emailVerifyCode;

    private LocalDateTime emailVerifyCodeExpiry;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserArticle> readArticles;
    
    @Builder
    public User(String loginId, String email, String name, String password) {
        this.loginId = loginId;
        this.email = email;
        this.name = name;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return emailVerified;
    }

    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public void setRefreshTokenExpiry(Date refreshTokenExpiry) { this.refreshTokenExpiry = refreshTokenExpiry; }

    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public void setEmailVerifyCode(String emailVerifyCode) { this.emailVerifyCode = emailVerifyCode; }
    
    public void setEmailVerifyCodeExpiry(LocalDateTime emailVerifyCodeExpiry) { this.emailVerifyCodeExpiry = emailVerifyCodeExpiry; }

    public void changePassword(String password, PasswordEncoder encoder) {
        this.password = encoder.encode(password);
    }
}

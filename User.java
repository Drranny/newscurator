package ac.kr.dankook.project.Data;

import java.util.Date;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

// 실질적으로 DB에 연동해서 사용할 Model(DAO)
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    // 데이터베이스 이용 시 사용할 변수 (회원가입 순서대로 생성 - 0,1,2...)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usernum;

    // 로그인 id / password를 제외하고 중복 불가
    @Column(unique = true, nullable = false)
    private String id;

    // 회원가입 시 사용할 email
    @Column(unique = true, nullable = false)
    private String email;

    // 해당 사이트에서 이용할 닉네임
    @Column(unique = true, nullable = false)
    private String nickname;

    // 로그인 PW
    @Column(nullable = false)
    private String password;

    private String refreshToken;          // refreshToken은 수명이 길기 때문에 따로 관리
    private Date refreshTokenExpiry;

    // 모든 필드를 가지는 Constructor
    public User(String id, String email, String nickname, String password) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.password = password;  
    }

    // Getter & Setter, userNum은 변경할 수 없도록 Getter만 생성
    public Long getUsernum() { return usernum; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id;}

    public String getUsername() { return email; }
    public void setUsername(String username) { this.email = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public Date getRefreshTokenExpiry() { return refreshTokenExpiry; }
    public void setRefreshTokenExpiry(Date refreshTokenExpiry) { this.refreshTokenExpiry = refreshTokenExpiry; }
}

package kr.ac.dankook.cs.curation.login.data;

import java.util.Map;

public class OAuthAttributes {
    private String nameAttributeKey;
    private String name;
    private String email;
    private String provider;     // ex: "google", "kakao", "naver"

    public OAuthAttributes(String nameAttributeKey, String name, String email, String provider) {
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.provider = provider;
    }

    public static OAuthAttributes of(String registrationId,
                                     String userNameAttributeName,
                                     Map<String,Object> attributes) {
        switch (registrationId) {
          case "kakao": return ofKakao(registrationId, userNameAttributeName, attributes);
          default:      return ofGoogle(registrationId, userNameAttributeName, attributes);
        }
    }

    private static OAuthAttributes ofGoogle(String provider,
                                            String userNameAttr,
                                            Map<String,Object> attrs) {
        return new OAuthAttributes(
          userNameAttr,
          (String) attrs.get("name"),
          (String) attrs.get("email"),
          "google"
        );
    }

    private static OAuthAttributes ofKakao(String provider,
                                           String userNameAttr,
                                           Map<String,Object> attrs) {

        Map<String,Object> kakaoAccount = (Map<String,Object>) attrs.get("kakao_account");
        Map<String,Object> profile      = (Map<String,Object>) kakaoAccount.get("profile");

        return new OAuthAttributes(
          userNameAttr,
          (String) profile.get("nickname"),
          (String) kakaoAccount.get("email"),
          "kakao"
        );
    }

    public User toEntity() {
        // User 생성자: loginId, email, name, password
        User u = new User(
          this.email,    // 로그인 아이디 로 email 사용
          this.email,
          this.name + "_" + this.provider,
          "0000"           // 소셜 로그인은 패스워드 불필요
        );

        u.setEmailVerified(true);
        return u;
    }

    public String getNameAttributeKey()             { return nameAttributeKey; }
    public String getName()                         { return name; }
    public String getEmail()                        { return email; }
    public String getProvider()                     { return provider; }
}

package ac.kr.dankook.example;

// 회원가입용 데이터 모델 - Model(DTO) 클래스
public class RegisterDTO {
    
    // Usernum을 제외하고 User와 같은 필드를 가짐
    private String id;
    private String email;
    private String nickname;
    private String password;

    // Getter & Setter
    public String getId() { return id; }

    public String getEmail() { return email; }

    public String getNickname() { return nickname; }

    public String getPassword() { return password; }
}

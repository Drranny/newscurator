package ac.kr.dankook.example;

// 로그인용 데이터 모델 - Model(DTO) 클래스
public class LoginDTO {
    private String id;
    private String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

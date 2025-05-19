package ac.kr.dankook.project.login.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ac.kr.dankook.project.login.data.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // null이 들어있을 수 있기 때문에 Optional 사용 (쿼리 메소드로 findBy 뒤에 오는 필드 이름으로 조회)
    // 사용할 쿼리메소드 구현

    Optional<User> findByLoginId(String id);       // 사용자 id로 조회
    Optional<User> findByRefreshToken(String token); // RefreshToek으로 조회
    void deleteByLoginId(String loginId);
}

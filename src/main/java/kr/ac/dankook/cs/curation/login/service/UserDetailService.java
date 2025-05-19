package kr.ac.dankook.cs.curation.login.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import kr.ac.dankook.cs.curation.login.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    // username으로 사용자 정보를 가져오는 메소드
    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByLoginId(username)
                .orElseThrow(() -> new IllegalArgumentException((username)));
    }
}

package kr.ac.dankook.cs.curation.login.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import kr.ac.dankook.cs.curation.login.data.ArticleReadRequest;
import kr.ac.dankook.cs.curation.login.data.StayTimeRequest;
import kr.ac.dankook.cs.curation.login.data.User;
import kr.ac.dankook.cs.curation.login.service.UserArticleService;

@RestController
@RequestMapping("/user-articles")
public class UserArticleController {
    @Autowired
    private UserArticleService userArticleService;

    // 기사 읽기 기록 API
    @PostMapping("/record")
    public ResponseEntity<Void> recordArticleRead(
        @RequestBody ArticleReadRequest request,
        Authentication authentication) {
            
            // 인증되지 않은 사용자 접근 차단
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 인증된 사용자 정보 추출
            User user = (User) authentication.getPrincipal();

            // 사용자가 읽은 기사 정보 기록 처리
            userArticleService.recordArticleRead(
                user,
                request.getTitle(),
                request.getArticleCategory(),
                request.getKeywords()
            );

            // 처리 완료 응답 반환
            return ResponseEntity.ok().build();
    }

    // 체류 시간 업데이트 API (현재는 사용x) - 내부 로직으로 새 탭을 여는게 아니라, api로 받은 url를 직접 사용해서 측정 불가
    @PostMapping("/update-stay-time")
    public ResponseEntity<Void> updateStayTime(
        @RequestBody StayTimeRequest request,
        Authentication authentication) {

            if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

            User user = (User) authentication.getPrincipal();
            userArticleService.updateStayTime(

                user,
                request.getTitle(),
                request.getArticleCategory(),
                request.getStayTime()
            );

            return ResponseEntity.ok().build();
    }
}

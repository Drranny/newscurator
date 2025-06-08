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
                
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User user = (User) authentication.getPrincipal();

            userArticleService.recordArticleRead(
                user,
                request.getTitle(),
                request.getArticleCategory(),
                request.getKeywords()
            );

            return ResponseEntity.ok().build();
    }

    // 체류 시간 업데이트 API
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

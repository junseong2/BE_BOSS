package com.onshop.shop.domain.user.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.domain.user.dto.UserUpdateRequestDTO;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.domain.user.service.UserService;
import com.onshop.shop.global.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * UserController는 사용자 관련 요청을 처리하는 REST 컨트롤러입니다.
 * 인증, 회원정보 조회 및 수정, 판매자 전환, 비밀번호 관련 기능 등을 제공합니다.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 로그인한 사용자의 기본 정보를 반환합니다.
     * @param token JWT 쿠키
     * @return 사용자 정보 Map 또는 오류 메시지
     */
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@CookieValue(value = "jwt", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }

        Long userId = jwtUtil.extractUserId(token);
        User user = userService.findById(userId);

        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "유저 정보를 찾을 수 없습니다."));
        }

        return ResponseEntity.ok(Map.of(
                "userId", user.getUserId().toString(),
                "userName", user.getUsername(),
                "userEmail", Optional.ofNullable(user.getEmail()).orElse(""),
                "userPhone1", Optional.ofNullable(user.getPhone1()).orElse(""),
                "userPhone2", Optional.ofNullable(user.getPhone2()).orElse(""),
                "userPhone3", Optional.ofNullable(user.getPhone3()).orElse(""),
                "userRole", user.getRole()
        ));
    }

    /**
     * 사용자 이메일 정보를 반환합니다.
     * @param token JWT 쿠키
     * @return 사용자 이메일
     */
    @GetMapping("/user-infoemail")
    public ResponseEntity<Map<String, String>> getUserInfoemail(@CookieValue(value = "jwt", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }

        Long userId = jwtUtil.extractUserId(token);
        User user = userService.findById(userId);

        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "유저 정보를 찾을 수 없습니다."));
        }

        return ResponseEntity.ok(Map.of(
                "userId", user.getUserId().toString(),
                "userName", user.getUsername(),
                "userEmail", user.getEmail()
        ));
    }

    /**
     * 로그아웃 처리 및 JWT 쿠키 삭제
     * @param response HttpServletResponse
     * @return 로그아웃 메시지
     */
    @GetMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    /**
     * 회원정보 수정 요청 처리
     * @param userUpdateRequest 수정 정보 DTO
     * @param token JWT 쿠키
     * @return 결과 메시지
     */
    @PutMapping("/update-userinfo")
    public ResponseEntity<String> updateUserInfo(@RequestBody UserUpdateRequestDTO userUpdateRequest,
                                                 @CookieValue(value = "jwt", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 없습니다.");
        }

        Long userIdFromJwt = jwtUtil.extractUserId(token);

        if (!userUpdateRequest.getUserId().equals(userIdFromJwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("권한이 없습니다.");
        }

        User user = userService.findById(userUpdateRequest.getUserId());
        if (user == null) {
            return ResponseEntity.badRequest().body("유저를 찾을 수 없습니다.");
        }

        userService.updateUserInfo(userUpdateRequest, user);
        return ResponseEntity.ok("회원 정보가 수정되었습니다!");
    }

    /**
     * 일반 사용자를 판매자로 전환하고 알림 이메일을 발송합니다.
     */
    @PatchMapping("/users/{userId}/update-seller")
    public ResponseEntity<?> promoteToSeller(@PathVariable Long userId, @RequestParam String storename) {
        userService.promoteToSellerAndNotify(userId, storename);
        return ResponseEntity.ok("SELLER 승격 및 메일 발송 완료");
    }

    /**
     * 판매자 신청을 거절하고 알림 이메일을 발송합니다.
     */
    @PatchMapping("/users/{userId}/reject-seller")
    public ResponseEntity<?> rejectSeller(@PathVariable Long userId, @RequestParam String storename) {
        userService.rejectSellerAndNotify(userId, storename);
        return ResponseEntity.ok("SELLER 거절 처리 및 메일 발송 완료");
    }

    /**
     * 사용자 전화번호를 문자열로 반환합니다.
     */
    @GetMapping("/users/{userId}/phone")
    public ResponseEntity<String> getUserPhones(@PathVariable Long userId) {
        String userPhones = userService.getUserPhones(userId);
        return ResponseEntity.ok(userPhones);
    }

    /**
     * 회원 탈퇴 처리
     */
    @DeleteMapping("/usersout/{userId}")
    public ResponseEntity<String> goodByUser(@PathVariable Long userId) {
        userService.deleteById(userId);
        return ResponseEntity.ok("회원 탈퇴 완료");
    }

    /**
     * 현재 비밀번호 일치 여부 확인
     */
    @PostMapping("/check-current-password")
    public ResponseEntity<Map<String, String>> checkCurrentPassword(@RequestBody Map<String, String> request,
                                                                     @CookieValue(value = "jwt", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인 정보가 없습니다."));
        }

        Long userId = jwtUtil.extractUserId(token);
        Map<String, String> messageMap = userService.verifyPassword(token, userId);

        if (messageMap.containsKey("404")) {
            return ResponseEntity.status(404).body(Map.of("error", messageMap.get("404")));
        }
        if (messageMap.containsKey("401")) {
            return ResponseEntity.status(401).body(Map.of("error", messageMap.get("401")));
        }

        return ResponseEntity.ok(Map.of("message", "현재 비밀번호가 확인되었습니다."));
    }

    /**
     * 비밀번호 변경 처리
     */
    @PatchMapping("/update-password")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestBody Map<String, String> request,
                                                               @CookieValue(value = "jwt", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인 정보가 없습니다."));
        }

        Long userId = jwtUtil.extractUserId(token);
        Map<String, String> messageMap = userService.updatePassword(request, userId);

        if (messageMap.containsKey("404")) {
            return ResponseEntity.status(404).body(Map.of("error", messageMap.get("404")));
        }
        if (messageMap.containsKey("401")) {
            return ResponseEntity.status(401).body(Map.of("error", messageMap.get("401")));
        }

        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }

    /**
     * 이메일로 비밀번호 재설정
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        userService.resetPassword(email, password);
        return ResponseEntity.ok("비밀번호 재설정 완료");
    }
}

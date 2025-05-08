package com.onshop.shop.domain.auth.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.domain.auth.dto.LoginRequestDTO;
import com.onshop.shop.domain.auth.dto.SignupRequestDTO;
import com.onshop.shop.domain.auth.service.AuthService;
import com.onshop.shop.domain.user.dto.EmailAuthRequestDTO;
import com.onshop.shop.domain.user.dto.EmailVerificationRequestDTO;
import com.onshop.shop.domain.user.dto.ForgetReqeustDTO;
import com.onshop.shop.domain.user.dto.ForgetResponseDTO;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * 인증 및 사용자 로그인/회원가입 관련 요청을 처리하는 컨트롤러입니다.
 * 
 * <p>기능 목록:
 * <ul>
 *   <li>로컬 회원가입 및 로그인</li>
 *   <li>소셜 로그인 (네이버, 카카오)</li>
 *   <li>이메일 인증</li>
 *   <li>아이디/비밀번호 찾기</li>
 * </ul>
 * 
 * @author 
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Value("${frontend.url}")
    private String frontendUrl;

    //TODO: U
    /**
     * 로컬 유저 회원가입을 처리합니다.
     *
     * @param user 회원가입할 사용자 정보 (JSON Body)
     * @param session 현재 세션 객체
     * @return 회원가입 성공 시 200 OK, 실패 시 400 Bad Request
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignupRequestDTO request, HttpSession session) {
        try {
            authService.registerUser(request);
            return ResponseEntity.ok("회원가입 성공!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 실패: " + e.getMessage());
        }
    }

    /**
     * 로컬 로그인 요청을 처리합니다.
     *
     * @param loginRequest 로그인 요청 DTO (email, password)
     * @param response HTTP 응답 객체 (쿠키 추가용)
     * @return 로그인 성공 시 쿠키를 응답에 추가
     */
    @PostMapping("/locallogin")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {
        Cookie cookie = authService.login(loginRequest);
        response.addCookie(cookie);
        return ResponseEntity.created(null).build();
    }

    /**
     * 네이버 로그인 인증 경로로 리디렉션합니다.
     *
     * @return 302 리디렉션 응답 (네이버 로그인 페이지 URL)
     */
    @GetMapping("/naver")
    public ResponseEntity<String> naverLoginRedirect() {
        String redirectUrl = authService.createNaverLoginRedirectUrl();
        return ResponseEntity.status(302).header("Location", redirectUrl).build();
    }

    /**
     * 네이버 로그인 콜백을 처리합니다.
     *
     * @param code 네이버에서 전달한 인증 코드
     * @param state 상태값
     * @param response 응답 객체 (쿠키 저장용)
     * @return 로그인 성공 후 프론트엔드 URL로 리디렉션
     */
    @GetMapping("/naver/callback")
    public ResponseEntity<String> naverCallback(@RequestParam String code, @RequestParam String state,
                                                HttpServletResponse response) throws IOException {
        Cookie cookie = authService.naverLoginCallback(code, state);
        response.addCookie(cookie);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, frontendUrl)
                .build();
    }

    /**
     * 카카오 로그인 인증 경로로 리디렉션합니다.
     *
     * @return 302 리디렉션 응답 (카카오 로그인 페이지 URL)
     */
    @GetMapping("/kakao")
    public ResponseEntity<String> kakaoLoginRedirect() {
        String redirectUrl = authService.createKakaoLoginRedirectUrl();
        return ResponseEntity.status(302).header(HttpHeaders.LOCATION, redirectUrl).build();
    }

    /**
     * 카카오 로그인 콜백을 처리합니다.
     *
     * @param code 카카오에서 전달한 인증 코드
     * @param response 응답 객체 (쿠키 저장용)
     * @return 로그인 성공 후 프론트엔드 홈으로 리디렉션
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<String> kakaoCallback(@RequestParam String code, HttpServletResponse response)
            throws IOException {
        Cookie cookie = authService.kakaoLoginCallback(code, code);
        response.addCookie(cookie);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "http://localhost:5173")
                .build();
    }

    /**
     * 회원가입 시 이메일 인증번호를 발송합니다.
     *
     * @param emailRequestDTO 이메일 주소 정보
     * @return 인증 코드 전송 성공 여부
     */
    @PostMapping("/email/send-code")
    public ResponseEntity<?> sendVerificationCode(@Valid @RequestBody EmailAuthRequestDTO emailRequestDTO) {
        log.info("email:{}", emailRequestDTO);
        try {
            authService.sendVerificationCode(emailRequestDTO.getEmail());
        } catch (MessagingException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이메일 전송 실패");
        }
        return ResponseEntity.ok("인증 코드 전송 성공");
    }

    /**
     * 이메일 인증번호 검증을 처리합니다.
     *
     * @param verificationRequestDTO 이메일 + 인증번호 정보
     * @return 검증 성공 여부
     */
    @PostMapping("/email/code-verify")
    public ResponseEntity<?> emailVerification(@Valid @RequestBody EmailVerificationRequestDTO verificationRequestDTO) {
        boolean isVer = authService.emailVerification(verificationRequestDTO);
        if (isVer) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body("success");
    }

    /**
     * 비밀번호 재설정용 이메일 인증 코드를 발송합니다.
     *
     * @param emailRequestDTO 이메일 요청 DTO
     * @return 인증 코드 발송 결과
     */
    @PostMapping("/email/password/send-code")
    public ResponseEntity<?> sendAuthCode(@Valid @RequestBody EmailAuthRequestDTO emailRequestDTO) {
        log.info("email:{}", emailRequestDTO);
        try {
            authService.sendAuthCode(emailRequestDTO.getEmail());
            return ResponseEntity.ok("인증 코드 전송 성공");
        } catch (MessagingException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이메일 전송 중 오류 발생");
        }
    }

    /**
     * 이름과 전화번호로 이메일(아이디)를 찾습니다.
     *
     * @param forgetReqeustDTO 사용자 이름과 전화번호
     * @return 이메일 정보 DTO
     */
    @PostMapping("/auth/find-email")
    public ResponseEntity<?> findEmail(@Valid @RequestBody ForgetReqeustDTO forgetReqeustDTO) {
        ForgetResponseDTO forgetResponseDTO = authService.findUserEmail(forgetReqeustDTO);
        return ResponseEntity.ok(forgetResponseDTO);
    }

    /**
     * 비밀번호 찾기 요청을 처리합니다. (TODO: 구현 필요)
     *
     * @param forgetReqeustDTO 사용자 정보
     * @return 현재는 null 반환
     */
    public ResponseEntity<?> findPassword(@Valid @RequestBody ForgetReqeustDTO forgetReqeustDTO) {
        return ResponseEntity.ok(null);
    }
}

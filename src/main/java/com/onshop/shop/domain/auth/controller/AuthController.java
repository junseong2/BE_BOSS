package com.onshop.shop.domain.auth.controller;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.domain.address.entity.Address;
import com.onshop.shop.domain.auth.dto.LoginRequestDTO;
import com.onshop.shop.domain.auth.service.AuthService;
import com.onshop.shop.domain.user.dto.EmailAuthRequestDTO;
import com.onshop.shop.domain.user.dto.EmailVerificationRequestDTO;
import com.onshop.shop.domain.user.dto.ForgetReqeustDTO;
import com.onshop.shop.domain.user.dto.ForgetResponseDTO;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.domain.user.enums.UserRole;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
	
	   private final AuthService authService;
	   
	   @Value("${frontend.url}")
	   private String frontendUrl;
	
	
	   // 로컬유저 회원가입
	   @PostMapping("/signup") 
	   public ResponseEntity<?> signUp(@RequestBody User user, HttpSession session) {
	       try {
	           if (user.getAddresses() == null) {
	               user.setAddresses(new ArrayList<>()); // addresses가 null이면 초기화
	           }

	           user.setRole(UserRole.CUSTOMER);

	           BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	           String encodedPassword = passwordEncoder.encode(user.getPassword());
	           user.setPassword(encodedPassword); // 암호화된 비밀번호 저장

	           // 각 주소에 user 객체 연결
	           for (Address addr : user.getAddresses()) {
	               addr.setUser(user); // 
	           }

	           authService.registerUser(user);

	           return ResponseEntity.ok("회원가입 성공!");
	       } catch (Exception e) {
	           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 실패: " + e.getMessage());
	       }
	   }


	   // 로컬 로그인
	   @PostMapping("/locallogin") 
	   public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {
		   
		   Cookie cookie = authService.login(loginRequest);
		   
		   response.addCookie(cookie);
		   return (ResponseEntity<?>) ResponseEntity.created(null);
	   }

	   
	   // 네이버 로그인 인증 경로로 리디렉트
	   @GetMapping("/naver")
	   public ResponseEntity<String> naverLoginRedirect() {
		   String redirectUrl = authService.createNaverLoginRedirectUrl();
	      return ResponseEntity.status(302).header("Location", redirectUrl).build();
	   }
	   
	   
	   // 네이버 로그인 콜백
	   @GetMapping("/naver/callback")
	   public ResponseEntity<String> naverCallback(@RequestParam String code, @RequestParam String state,
	                                               HttpServletResponse response) throws IOException {
	         
		   Cookie cookie = authService.naverLoginCallback(code, state);

		   response.addCookie(cookie);
		   return ResponseEntity.status(HttpStatus.FOUND)
	                   .header(HttpHeaders.LOCATION, frontendUrl)
	                   .build();
	   }

	   @GetMapping("/kakao")
	   public ResponseEntity<String> kakaoLoginRedirect() {
	      // 카카오 로그인 페이지로 리다이렉트
		  String redirectUrl = authService.createKakaoLoginRedirectUrl();
		  
	      return ResponseEntity.status(302) // 302 상태 코드로 리다이렉트
	            .header(HttpHeaders.LOCATION, redirectUrl) // 카카오 로그인 페이지로 리다이렉트
	            .build();
	   }

	   
	   @GetMapping("/kakao/callback")
	   public ResponseEntity<String> kakaoCallback(@RequestParam String code, HttpServletResponse response)
	         throws IOException {
		   Cookie cookie = authService.kakaoLoginCallback(code, code);

		   response.addCookie(cookie);
		   
	      // 카카오 로그인 후 홈 페이지로 리다이렉트
	      return ResponseEntity.status(HttpStatus.FOUND) // 302 상태 코드 (리다이렉트)
	            .header(HttpHeaders.LOCATION, "http://localhost:5173") // 홈 페이지로 리다이렉트
	            .build();
	   }
	   
	   /** 이매일 인증 */
	   // 인증번호 발송
	   @PostMapping("/email/send-code")
	   public ResponseEntity<?> sendVerificationCode(@Valid @RequestBody EmailAuthRequestDTO emailRequestDTO) {

	      log.info("email:{}", emailRequestDTO);

	      try {
	         authService.sendVerificationCode(emailRequestDTO.getEmail());
	      } catch (MessagingException ex) {
	         return null;
	      }

	      return ResponseEntity.ok(null);
	   }

	   // 인증번호 검증
	   @PostMapping("/email/code-verify")
	   public ResponseEntity<?> emailVerification(@Valid @RequestBody EmailVerificationRequestDTO verificationRequestDTO) {

	      boolean isVer = authService.emailVerification(verificationRequestDTO);
	      if (isVer) {
	         return ResponseEntity.noContent().build();
	      }

	      return ResponseEntity.ok().body("success");

	   }
	   
	   //  비밀번호 인증용 임시 코드
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

	   // 아이디 찾기
	   @PostMapping("/auth/find-email")
	   public ResponseEntity<?> findEmail(@Valid @RequestBody ForgetReqeustDTO forgetReqeustDTO) {

	      ForgetResponseDTO forgetResponseDTO = authService.findUserEmail(forgetReqeustDTO);
	      return ResponseEntity.ok(forgetResponseDTO);
	   }

	   // 비밀번호 찾기
	   public ResponseEntity<?> findPassword(@Valid @RequestBody ForgetReqeustDTO forgetReqeustDTO) {

	      return ResponseEntity.ok(null);
	   }

	   

}

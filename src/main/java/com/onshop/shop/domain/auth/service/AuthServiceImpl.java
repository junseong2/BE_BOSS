package com.onshop.shop.domain.auth.service;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.logging.log4j.util.InternalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onshop.shop.domain.address.entity.Address;
import com.onshop.shop.domain.auth.dto.LoginRequestDTO;
import com.onshop.shop.domain.user.dto.EmailVerificationRequestDTO;
import com.onshop.shop.domain.user.dto.ForgetReqeustDTO;
import com.onshop.shop.domain.user.dto.ForgetResponseDTO;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.domain.user.enums.UserRole;
import com.onshop.shop.domain.user.service.UserService;
import com.onshop.shop.global.exception.BadRequestException;
import com.onshop.shop.global.exception.ResourceNotFoundException;
import com.onshop.shop.global.util.EmailUtils;
import com.onshop.shop.global.util.JwtUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
	
		private final UserService userService;
		private final JwtUtil jwtUtil;
		private final RestTemplate restTemplate;
	    private final RedisTemplate<String, Object> redisTemplate;
	    private final JavaMailSender javaMailSender;
	    private final EmailUtils emailUtils;
	    
	    @Value("${main.sender.email}")
	    private String senderEmail;
	
	    @Value("${naver.client.id}")
	    private String naverClientId;

	    @Value("${naver.client.secret}")
	    private String naverClientSecret;
	   
	    @Value("${naver.redirect.uri}")
	    private String naverRedirectUri;
	   
	    @Value("${kakao.client.id}")
	    private String kakaoClientId;

	    @Value("${kakao.redirect.uri}")
	    private String kakaoRedirectUri;
	   
	   

	   // 회원가입
	   @Override
	   @Transactional(propagation = Propagation.REQUIRED)
	   public void registerUser(User user) {

		   User savedUser = userService.registerUser(user); // 유저 등록

	       List<Address> addresses = user.getAddresses() != null ? user.getAddresses() : List.of();
	       log.info("주소 목록:{}",addresses);

	       if (!addresses.isEmpty()) {
	            // 로그: 주소 저장 전 상태
	    	   log.info("주소 목록 저장 전:{} ", addresses);

	            List<Address> addressEntities = addresses.stream()
	                .map(address -> {
	                    Address addressEntity = new Address();
	                    addressEntity.setUser(savedUser);
	                    addressEntity.setAddress1(address.getAddress1());
	                    addressEntity.setAddress2(address.getAddress2());
	                    addressEntity.setPost(address.getPost());
	                    addressEntity.setIsDefault(address.getIsDefault());
	                    return addressEntity;
	                }).collect(Collectors.toList());

	            // 로그: 주소 저장 후 상태
	            log.info("주소 목록 저장 후:{} ", addressEntities);
//	            addressRepository.saveAll(addressEntities);
	        }
	    }

	// 로그인 
	@Override
	public Cookie login(LoginRequestDTO loginRequestDTO) {
		
		 User user = userService.findByEmail(loginRequestDTO.getEmail());
		
	      if (user == null) {
	         throw new BadRequestException("존재하지 않는 유저이거나 유효한 이메일이 아닙니다."); 
	      }
	      
	      // 비밀번호 비교
	       BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	          if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
	              throw new BadRequestException("잘못된 비밀번호 입니다.");
	          }

	      // JWT 생성
	      String token = jwtUtil.generateToken(user.getUserId());

	      // 쿠키에 JWT 설정
	      Cookie cookie = new Cookie("jwt", token);
	      cookie.setHttpOnly(true); // 클라이언트에서 접근 불가
	      cookie.setPath("/"); // 쿠키의 유효 경로 설정
	      
	      
	      return cookie;
		
	}
	
	

	// 네이버 로그인 인증 화면 경로 리디렉트
	@Override
	public String createNaverLoginRedirectUrl() {
	      String state = generateState();
	      String redirectUrl = "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" + naverClientId
	            + "&redirect_uri=" + naverRedirectUri + "&state=" + state;
		
	      return redirectUrl;
	}

	// 네이버 로그인 콜백
	@Override
	public Cookie naverLoginCallback(String code, String state) {
	       try {
	           String accessTokenUrl = "https://nid.naver.com/oauth2.0/token";
	           String tokenRequestBody = "grant_type=authorization_code" +
	                   "&client_id=" + naverClientId +
	                   "&client_secret=" + naverClientSecret +
	                   "&code=" + code +
	                   "&state=" + state;

	           HttpHeaders headers = new HttpHeaders();
	           
	           headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	           HttpEntity<String> entity = new HttpEntity<>(tokenRequestBody, headers);
	           
	           ResponseEntity<String> tokenResponse = restTemplate.postForEntity(accessTokenUrl, entity, String.class);

	           if (tokenResponse.getBody() == null) {
	               throw new InternalException("네이버 접근 토큰 발급 실패");
	           }

	           ObjectMapper objectMapper = new ObjectMapper();
	           JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
	           String accessToken = tokenJson.get("access_token").asText();

	           String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
	           HttpHeaders userInfoHeaders = new HttpHeaders();
	           userInfoHeaders.setBearerAuth(accessToken);

	           HttpEntity<String> userInfoEntity = new HttpEntity<>(userInfoHeaders);
	           ResponseEntity<String> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userInfoEntity, String.class);

	           if (userInfoResponse.getBody() == null) {
	               throw new InternalException("네이버 로그인 유저 정보 획득 실패");
	           }

	           JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody()).path("response");
	           String naverId = userInfo.get("id").asText();
	           String userName = userInfo.has("name") ? userInfo.get("name").asText() : "네이버유저";
	           String userEmail = userInfo.has("email") ? userInfo.get("email").asText() : "";

	           
	           UserRole role = UserRole.CUSTOMER; // 유저 권한)
	           User user = userService.saveOrUpdateSocialUser(naverId, userName, userEmail, "naver", role); // 유저 정보 업데이트

	           String token = jwtUtil.generateToken(user.getUserId());
	           Cookie cookie = new Cookie("jwt", token);
	           cookie.setHttpOnly(true);
	           cookie.setSecure(true);
	           cookie.setPath("/");
	           cookie.setMaxAge(3600);
	           
	           return cookie;

	  
	       } catch (Exception e) {
	           log.error("❌ 네이버 콜백 처리 중 예외 발생: ", e);
	           throw new InternalException("네이버 로그인 중 오류 발생"); 
	       }
		
	}

	// 카카오 로그인 인증 경로 리디렉트
	@Override
	public String createKakaoLoginRedirectUrl() {
		String redirectUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + kakaoClientId
	            + "&redirect_uri=" + kakaoRedirectUri;
		return redirectUrl;
	}

	
	// 카카오 로그인 콜백
	@Override
	public Cookie kakaoLoginCallback(String code, String state) {
		
		 try {
	     // 1. 카카오 토큰 요청 URL
	      String accessTokenUrl = "https://kauth.kakao.com/oauth/token";

	      // 2. 요청 본문 생성
	      String tokenRequestBody = "grant_type=authorization_code" + "&client_id=" + kakaoClientId + "&redirect_uri="
	            + kakaoRedirectUri + "&code=" + code;

	      // 3. HTTP 헤더 설정
	      HttpHeaders headers = new HttpHeaders();
	      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	      // 4. HTTP 요청 보내기
	      HttpEntity<String> entity = new HttpEntity<>(tokenRequestBody, headers);
	      ResponseEntity<String> tokenResponse = restTemplate.postForEntity(accessTokenUrl, entity, String.class);

	      // 5. 응답이 없으면 실패 처리
	      if (tokenResponse.getBody() == null) {
	          throw new InternalException("카카오 접근 토큰 발급 실패");
	      }

	      // 6. JSON 파싱 (access_token 추출)
	      ObjectMapper objectMapper = new ObjectMapper();
	      JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
	      String accessToken = tokenJson.get("access_token").asText();

	      // 7. 사용자 정보 요청
	      String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
	      HttpHeaders userInfoHeaders = new HttpHeaders();
	      userInfoHeaders.setBearerAuth(accessToken);

	      HttpEntity<String> userInfoEntity = new HttpEntity<>(userInfoHeaders);
	      ResponseEntity<String> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userInfoEntity,
	            String.class);

	      // 8. 사용자 정보 파싱
	      if (userInfoResponse.getBody() == null) {
	    	     throw new InternalException("카카오 로그인 유저 정보 획득 실패");
	      }

	      JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody());
	      String kakaoId = userInfo.get("id").asText();
	      String userName = userInfo.path("kakao_account").path("profile").path("nickname").asText();
	      String userEmail = userInfo.path("kakao_account").path("email").asText();

	      String socialProvider = "kakao";
	      UserRole role = UserRole.CUSTOMER;

	      // 사용자 정보를 저장하거나 업데이트
	      User user = userService.saveOrUpdateSocialUser(kakaoId, userName, userEmail, socialProvider, role);

	      // JWT 생성 후 쿠키에 저장
	      String token = jwtUtil.generateToken(user.getUserId());
	      Cookie cookie = new Cookie("jwt", token);
	      cookie.setHttpOnly(true);
	      cookie.setSecure(true);
	      cookie.setPath("/");
	      cookie.setMaxAge(60 * 60); // 1시간 유효
	      
	      return cookie;
		
	    } catch (Exception e) {
	    	log.error("❌ 콜백 처리 중 예외 발생: ", e);
	    	throw new InternalException("카카오 로그인 중 오류 발생"); 
	    }
		
	}
	   
	

	 /** 이메일 인증*/
    // 이메일 인증
    @Override
    public boolean emailVerification(EmailVerificationRequestDTO verificationRequestDTO) {
        String value = (String) redisTemplate.opsForValue().get(verificationRequestDTO.getEmail());
        log.info("input code: {}, auth code: {}", verificationRequestDTO.getCode(), value);
        boolean isVer = verificationRequestDTO.getCode().equals(value);
        
        // 인증 성공 시 레디스에서 키 제거
        if(isVer){
            redisTemplate.delete(verificationRequestDTO.getEmail());
        }
        return isVer ;
    }

    // 이메일 인증 코드 발송
    @Override
    public void sendVerificationCode(String toMail) throws MessagingException {

         Boolean isUser = userService.existsByEmail(toMail);

         // TODO: 커스텀 예외 적용해야 함.
         if(isUser){
             throw new BadRequestException("이미 존재하는 이메일입니다.");
         }

        boolean isValidMx = this.validateMx(toMail.split("@")[1]);
        if(isValidMx){
            String authCode = emailUtils.createAuthCode(5);

            MimeMessage message = createMail(toMail, authCode);
            javaMailSender.send(message);

            // 이메일 인증번호 5분 간 캐싱
            redisTemplate.opsForValue().set(toMail, authCode, 300, TimeUnit.SECONDS); // 5분 간 유효

            // TODO: 커스텀 예외 적용해야 함.
        } else {
            throw new BadRequestException("해당 형식은 유효한 도메인이 아닙니다.");
        }

    }

    
    // MX 레코드 검증
    // reference: https://velog.io/@danielyang-95/%EC%9D%B4%EB%A9%94%EC%9D%BC-%EC%9C%A0%ED%9A%A8%EC%84%B1-%EA%B2%80%EC%A6%9D-by-MX-%EB%A0%88%EC%BD%94%EB%93%9C
    @Override
    public boolean validateMx(String domain) {
        try {
                Hashtable<String, String> env = new Hashtable<>();
                env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
                DirContext ictx = new InitialDirContext(env);
                Attributes attrs = ictx.getAttributes(domain, new String[]{"MX"});
                Attribute attr = attrs.get("MX");

                if (attr == null) {
                    return false;
                }

                return true;
            } catch (NamingException e) {
                return false;
            }
    }
    
	@Override
	public void sendAuthCode(String email) throws MessagingException {
	    // 1. 인증 코드 생성
	    String authCode = emailUtils.createAuthCode(5); // 5자리 인증 코드 생성

	    // 2. 인증 코드 Redis에 저장 (5분간 유효)
	    redisTemplate.opsForValue().set(email, authCode, 300, TimeUnit.SECONDS); // 5분 간 유효

	    // 3. 이메일 발송
	    MimeMessage message = createMail(email, authCode);
	    javaMailSender.send(message);

	    // 로그 추가
	    log.info("인증 코드 전송 완료: {}", email);
	}
    
    @Override
    // 이메일 포맷 설정 
    public MimeMessage createMail(String email, String authCode) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.setFrom(senderEmail);
        message.setRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("이메일 인증");
        String body =    "<html lang='ko'>" +
        	    "<head>" +
        	    "<meta charset='UTF-8'>" +
        	    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
        	    "<title>이메일 인증</title>" +
        	    "</head>" +
        	    "<body style='font-family: Arial, sans-serif; background-color: #f4f4f9; margin: 0; padding: 0; text-align: center;'>" +
        	    "<div style='width: 100%; max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1); padding: 30px; text-align: center;'>" +
        	    "<h3 style='font-size: 24px; color: #333333; margin-bottom: 20px; font-weight: bold;'>BOSS 사이트 인증 번호</h3>" +
        	    "<p style='font-size: 18px; color: #555555;'>요청하신 인증 번호는 아래와 같습니다.</p>" +
        	    "<div style='font-size: 48px; color: #ffffff; font-weight: bold; background-color: #4294F2; padding: 20px; border-radius: 10px; display: inline-block; margin: 20px 0;'>" +
        	    "<h1 style='margin: 0;'>" + authCode + "</h1>" +
        	    "</div>" +
        	    "<p style='font-size: 16px; color: #777777; margin-top: 30px;'>이메일 인증에 감사드립니다.</p>" +
        	    "<p style='font-size: 18px; color: #666666; margin-top: 10px;'>감사합니다.</p>" +
        	    "</div>" +
        	    "</body>" +
        	    "</html>";
        message.setText(body, "UTF-8", "html");

        return message;
        
    }


    /** 이메일 찾기*/
	@Override
	public ForgetResponseDTO findUserEmail(ForgetReqeustDTO forgetReqeustDTO) {
		
		ForgetResponseDTO forgetResponseDTO= userService.findByUsernameAndPassword(forgetReqeustDTO.getUsername(), forgetReqeustDTO.getPassword());
		if(forgetResponseDTO == null) {
			throw new ResourceNotFoundException("해당 유저는 존재하지 않습니다.");
		}
		
		return forgetResponseDTO;
	}

	
	
	private String generateState() {
	     // Generate a random 16-character string to be used as the state parameter
	     String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	     StringBuilder state = new StringBuilder();
	 
	     for (int i = 0; i < 16; i++) {
		         int randomIndex = (int) (Math.random() * characters.length());
		         state.append(characters.charAt(randomIndex));
		      }
		      return state.toString(); // Return the generated state
		 }
}

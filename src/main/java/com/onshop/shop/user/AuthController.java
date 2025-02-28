package com.onshop.shop.user;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;  // ✅ 추가!
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders; // 이 줄을 추가하세요.
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // javax -> jakarta로 변경

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {
	

	@Value("${naver.client.id}")
	private String naverClientId;

	@Value("${naver.client.secret}")
	private String naverClientSecret;

	@Value("${kakao.client.id}")
	private String kakaoClientId;

	@Value("${naver.redirect.uri}")
	private String naverRedirectUri;

	@Value("${kakao.redirect.uri}")
	private String kakaoRedirectUri;

	private final RestTemplate restTemplate;
	
	
	   
		// ✅ UserRepository 추가
		private final UserRepository userRepository;

		// ✅ 생성자에서 UserRepository 주입
		@Autowired
		public AuthController(RestTemplate restTemplate, UserRepository userRepository) {
		    this.restTemplate = restTemplate;
		    this.userRepository = userRepository;
		}


	@GetMapping("/naver")
	public ResponseEntity<String> naverLoginRedirect(HttpSession session) {
		String state = generateState();
		session.setAttribute("naver_state", state);
		String url = "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" + naverClientId
				+ "&redirect_uri=" + naverRedirectUri + "&state=" + state;
		return ResponseEntity.status(302).header("Location", url).build();
	}
	@GetMapping("/naver/callback")
	public void naverCallback(@RequestParam String code, @RequestParam String state,
	                          HttpSession session, HttpServletResponse response) throws IOException {
	    String savedState = (String) session.getAttribute("naver_state");

	    if (savedState == null || !savedState.equals(state)) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid state");
	        return;
	    }

	    // 🔹 Access Token 요청
	    String accessTokenUrl = "https://nid.naver.com/oauth2.0/token";
	    String tokenRequestBody = "grant_type=authorization_code"
	            + "&client_id=" + naverClientId
	            + "&client_secret=" + naverClientSecret
	            + "&code=" + code
	            + "&state=" + state;

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	    HttpEntity<String> entity = new HttpEntity<>(tokenRequestBody, headers);
	    ResponseEntity<String> tokenResponse = restTemplate.postForEntity(accessTokenUrl, entity, String.class);

	    if (tokenResponse.getBody() == null) {
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to get access token");
	        return;
	    }

	    // 🔹 JSON 파싱 (access_token 추출)
	    ObjectMapper objectMapper = new ObjectMapper();
	    JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
	    String accessToken = tokenJson.get("access_token").asText();

	    // 🔹 사용자 정보 요청
	    String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
	    HttpHeaders userInfoHeaders = new HttpHeaders();
	    userInfoHeaders.setBearerAuth(accessToken);

	    HttpEntity<String> userInfoEntity = new HttpEntity<>(userInfoHeaders);
	    ResponseEntity<String> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userInfoEntity, String.class);

	    if (userInfoResponse.getBody() == null) {
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to get user info");
	        return;
	    }

	    // 🔹 사용자 정보 파싱
	    JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody()).path("response");
	    String userId = userInfo.get("id").asText();
	    String userName = userInfo.get("name").asText();
	    String userEmail = userInfo.has("email") ? userInfo.get("email").asText() : "";
	    String social_provider = "naver";
	    System.out.println("네이버 UserName: " + userName);
	    // 🔹 사용자 정보 저장 또는 업데이트
	    
	 //   saveOrUpdateSocialUser(userId, userName, userEmail, social_provider);

	    saveOrUpdateUser(userId, userName, userEmail);

	    // 🔹 세션 저장
	    session.setAttribute("userId", userId);
	    session.setAttribute("userName", userName);
	    session.setAttribute("userEmail", userEmail);

	    System.out.println("네이버 UserName: " + userName);
	    System.out.println("네이버 UserId: " + userId);

	    // 프론트엔드로 리다이렉트
	    response.sendRedirect("http://localhost:5173");
	}




	@PostMapping("/save-redirect-url")
	public ResponseEntity<String> saveRedirectUrl(@RequestBody Map<String, String> request, HttpSession session) {
		String redirectUrl = request.get("redirectUrl");
		if (redirectUrl == null || redirectUrl.isEmpty()) {
			return ResponseEntity.badRequest().body("Redirect URL is missing");
		}

		session.setAttribute("redirectUrl", redirectUrl);
		return ResponseEntity.ok("Redirect URL saved successfully");
	}
	
	@GetMapping("/get-redirect-url")
    public ResponseEntity<Map<String, String>> getRedirectUrl(HttpSession session) {
        String redirectUrl = (String) session.getAttribute("redirectUrl");
        if (redirectUrl == null) {
            redirectUrl = "/";  // 기본값
        }
        return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
    }
	
	@GetMapping("/user-info")
	public ResponseEntity<Map<String, String>> getUserInfo(HttpSession session) {
	    String userId = (String) session.getAttribute("userId");
	    String userName = (String) session.getAttribute("userName");
        System.out.println("user-info UserName: " + userName);
        System.out.println("user-info UserId: " + userId);
	    if (userId == null || userName == null) {
	        return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
	    }

	    return ResponseEntity.ok(Map.of("userId", userId, "userName", userName));
	}

	@GetMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(HttpSession session) {
	    session.invalidate(); // 세션 무효화
	    System.out.println("로그아웃"); // 로그아웃 메시지 출력

	    Map<String, String> response = new HashMap<>();
	    response.put("message", "로그아웃 성공");

	    return ResponseEntity.ok(response);
	}


	@GetMapping("/redirect")
	public ResponseEntity<Void> redirectAfterLogin(HttpSession session) {
		String redirectUrl = (String) session.getAttribute("redirectUrl");
		if (redirectUrl == null) {
			redirectUrl = "/"; // 기본값 (홈페이지)
		}
		return ResponseEntity.status(302).header("Location", redirectUrl).build();
	}

	@GetMapping("/kakao")
	public ResponseEntity<String> kakaoLoginRedirect(HttpSession session) {
		String url = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + kakaoClientId
				+ "&redirect_uri=" + kakaoRedirectUri;
		return ResponseEntity.status(302).header("Location", url).build();
	}


    @GetMapping("/kakao/callback")
    public ResponseEntity<String> kakaoCallback(@RequestParam String code, HttpSession session, HttpServletResponse response) throws IOException {
        // 1. 카카오 토큰 요청 URL
        String accessTokenUrl = "https://kauth.kakao.com/oauth/token";

        // 2. 요청 본문 생성
        String tokenRequestBody = "grant_type=authorization_code"
                + "&client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri
                + "&code=" + code;

        // 3. HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 4. HTTP 요청 보내기
        HttpEntity<String> entity = new HttpEntity<>(tokenRequestBody, headers);
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(accessTokenUrl, entity, String.class);

        // 5. 응답이 없으면 실패 처리
        if (tokenResponse.getBody() == null) {
            return ResponseEntity.status(500).body("Failed to get access token from Kakao");
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
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userInfoEntity, String.class);

        if (userInfoResponse.getBody() == null) {
            return ResponseEntity.status(500).body("Failed to get user info from Kakao");
        }

        // 8. 사용자 정보 파싱
        JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody());
        String userId = userInfo.get("id").asText();
        String userName = userInfo.path("kakao_account").path("profile").path("nickname").asText();
        String userEmail = userInfo.path("kakao_account").path("email").asText();
        String social_provider = "kakao";
        // 사용자 정보를 저장하거나 업데이트하는 메서드 호출
        saveOrUpdateUser(userId, userName, userEmail);

        // 세션 저장
        session.setAttribute("userId", userId);
        session.setAttribute("userName", userName);
        session.setAttribute("userEmail", userEmail);
        System.out.println("카카오 UserName: " + userName);
        System.out.println("카카오 UserId: " + userId);
        // 프론트엔드로 리다이렉트
        response.sendRedirect("http://localhost:5173");

        return ResponseEntity.ok("Kakao login successful");
    }
    @Transactional
    public synchronized User saveOrUpdateUser(String socialId, String userName, String userEmail) {
        Optional<User> existingUserOpt = userRepository.findBySocialId(socialId);
        
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            existingUser.setUsername(userName);
            existingUser.setEmail(userEmail);
            System.out.println("Updating existing user: " + existingUser);

            return userRepository.save(existingUser); // 기존 사용자 업데이트
        } else {
            User newUser = new User(socialId, userName, userEmail);
            System.out.println("Saving new user: " + newUser);

            return userRepository.save(newUser); // 새 사용자 저장
        }
    }
    @Transactional
    public synchronized User saveOrUpdateSocialUser(String socialId, String userName, String userEmail,String social_provider) {
        Optional<User> existingUserOpt = userRepository.findBySocialId(socialId);
        
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            existingUser.setUsername(userName);
            existingUser.setEmail(userEmail);
            existingUser.setSocialProvider(social_provider);
          
          
            System.out.println("Updating existing user: " + existingUser);

            return userRepository.save(existingUser); // 기존 사용자 업데이트
        } else {
            User newUser = new User(socialId, userName, userEmail,social_provider);
            System.out.println("Saving new user: " + newUser);

            return userRepository.save(newUser); // 새 사용자 저장
        }
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



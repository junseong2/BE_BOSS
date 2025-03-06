package com.onshop.shop.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;  // ✅ 추가!

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders; // 이 줄을 추가하세요.
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onshop.shop.user.UserUpdateRequest;
import com.onshop.shop.user.AddressRepository;
import com.onshop.shop.user.UserRepository;

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
	public ResponseEntity<String> naverCallback(@RequestParam String code, @RequestParam String state,
	                          HttpSession session, HttpServletResponse response) throws IOException {
	    String savedState = (String) session.getAttribute("naver_state");

	    if (savedState == null || !savedState.equals(state)) {
	        return ResponseEntity.badRequest().body("Invalid state");
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
	        return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).body("Failed to get access token");
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
	        return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).body("Failed to get user info");
	    }

	    // 🔹 사용자 정보 파싱
	    JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody()).path("response");
	    String naverId = userInfo.get("id").asText(); // naverId를 String으로 사용
	    String userName = userInfo.get("name").asText();
	    String userEmail = userInfo.has("email") ? userInfo.get("email").asText() : "";
	    String social_provider = "naver";
	    String role = "costomer";

	    // 🔹 사용자 정보 저장 또는 업데이트
	    saveOrUpdateSocialUser(naverId, userName, userEmail,social_provider,role); // naverId를 String으로 사용

	    // 🔹 UserEntity 찾기
	    UserEntity user = userService.findBySocialId(naverId); // naverId를 사용하여 찾기

	    if (user == null) {
	        return ResponseEntity.status(404).body("User not found");
	    }

	    // ✅ 세션 저장 (로컬 로그인과 동일한 구조)
	    session.setAttribute("userId", user.getUserId());
	    session.setAttribute("userName", user.getUsername());
	    session.setAttribute("userEmail", user.getEmail());

	    // 프론트엔드로 리다이렉트
	    response.sendRedirect("http://localhost:5173");
	    
	    return ResponseEntity.ok("Naver login successful");
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
	    Object userIdObj = session.getAttribute("userId");
	    Object userNameObj = session.getAttribute("userName");

        //Optional<UserEntity> userOpt = userRepository.findById(userId);

        
	    System.out.println("user-info UserId: " + userIdObj);
	    System.out.println("user-info UserName: " + userNameObj);

	    // ✅ 값이 null인지 확인
	    if (userIdObj == null || userNameObj == null) {
	        return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
	    }

	    // ✅ Integer → String 변환 방지
	    String userId = userIdObj instanceof Integer ? String.valueOf(userIdObj) : userIdObj.toString();
	    String userName = userNameObj instanceof Integer ? String.valueOf(userNameObj) : userNameObj.toString();

	    return ResponseEntity.ok(Map.of("userId", userId, "userName", userName));
	}
	
	  @Autowired
	    private AddressRepository addressRepository;
	  @PutMapping("/update-userinfo")
	  public ResponseEntity<String> updateUserInfo(@RequestBody UserUpdateRequest request) {
	      System.out.println("📢 회원 정보 수정 요청 받음: " + request);
	      System.out.println("📢 회원 정보 수정 요청 받음: " + request.getUserId());

	      UserEntity user = userRepository.findById(request.getUserId()).orElse(null);
	      if (user == null) {
	          System.out.println("❌ 유저를 찾을 수 없습니다. userId: " + request.getUserId());
	          return ResponseEntity.badRequest().body("유저를 찾을 수 없습니다.");
	      }

	      // ✅ 이메일 & 비밀번호 업데이트
	      System.out.println("✅ 기존 이메일: " + user.getEmail() + " → 변경될 이메일: " + request.getEmail());
	      user.setEmail(request.getEmail());
	      user.setPassword(request.getPassword());

	      // ✅ 전화번호 업데이트
	      List<String> phones = request.getPhones();
	      System.out.println("✅ 기존 전화번호: " + user.getPhone1() + " → 변경될 전화번호 리스트: " + phones);
	      user.setPhone1(phones.size() > 0 ? phones.get(0) : null);
	      user.setPhone2(phones.size() > 1 ? phones.get(1) : null);
	      user.setPhone3(phones.size() > 2 ? phones.get(2) : null);

	      System.out.println("📌 받은 요청 데이터: " + request);

	      // ✅ 기존 주소 삭제 후 새 주소 저장
	      System.out.println("✅ 기존 주소 삭제 시도");
	      addressRepository.deleteAllByUserId(user.getUserId()); // 🔥 여기 수정
	      long remainingCount = addressRepository.countByUser(user);
	      System.out.println("🔍 삭제 후 남은 주소 개수: " + remainingCount);
	      List<AddressEntity> newAddresses = request.getAddresses().stream()
	          .limit(3) // 최대 3개 제한
	          .map(addrReq -> {
	              AddressEntity address = new AddressEntity();
	              address.setUser(user);
	              address.setAddress1(addrReq.getAddress1());
	              address.setAddress2(addrReq.getAddress2());
	              address.setPost(addrReq.getPost());
	              address.setIsDefault(addrReq.getIsDefault());
	              System.out.println("📌 요청으로 받은 주소 리스트: " + request.getAddresses());

	              
	              
	              // 🔥 디버깅 로그 추가
	           // ✅ 필드 값 확인 (디버깅 로그 추가)
	              System.out.println("✅ 새 주소 객체 생성 - address1: " + address.getAddress1() 
	                  + ", address2: " + address.getAddress2()
	                  + ", post: " + address.getPost()
	                  + ", isDefault: " + address.getIsDefault());
	              
	              return address;
	          }).toList();
	      
	      
	      System.out.println("✅ 새로 저장할 주소 개수: " + newAddresses.size());
	      addressRepository.saveAll(newAddresses);
	      addressRepository.flush(); // 🔥 즉시 반영 (JPA 변경 감지 강제)

	      List<AddressEntity> savedAddresses = addressRepository.findAll();
	      
	      System.out.println("🔍 저장된 주소 리스트: " + savedAddresses);


	      userRepository.save(user);
	      System.out.println("✅ 회원 정보 수정 완료");
	      return ResponseEntity.ok("회원 정보가 수정되었습니다!");
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

	    String kakaoId = userInfo.get("id").asText();
	    String userName = userInfo.path("kakao_account").path("profile").path("nickname").asText();
	    String userEmail = userInfo.path("kakao_account").path("email").asText();

	    String social_provider = "kakao";
	    String role = "costomer";
	    
	    // 사용자 정보를 저장하거나 업데이트
	    saveOrUpdateSocialUser(kakaoId, userName, userEmail,social_provider,role);

	    // ✅ userId로 UserEntity 찾기
	    UserEntity user = userService.findBySocialId(kakaoId); // Optional이 아니라 직접 반환

	    if (user == null) {
	        return ResponseEntity.status(404).body("User not found");
	    }

	    // ✅ 세션 저장 (로컬 로그인과 동일한 구조)
	    session.setAttribute("userId", user.getUserId()); 
	    session.setAttribute("userName", user.getUsername());
	    session.setAttribute("userEmail", user.getEmail());

	    System.out.println("카카오 UserName: " + user.getUsername());
	    System.out.println("카카오 UserId: " + user.getUserId());

	    // 프론트엔드로 리다이렉트
	    response.sendRedirect("http://localhost:5173");

	    return ResponseEntity.ok("Kakao login successful");
	}

    
    @Autowired
    private UserService userService;
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserEntity user, HttpSession session) {
        try {
            if (user.getAddresses() == null) {
                user.setAddresses(new ArrayList<>()); // ✅ addresses가 null이면 초기화
            }

            userService.registerUser(user);
            // ✅ userId 확인
            System.out.println("로컬 UserName: " +  user.getUsername());
            System.out.println("로컬 UserId: " +  user.getUserId());
      

            return ResponseEntity.ok("회원가입 성공!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 실패: " + e.getMessage());
        }
    }
    /*
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            // ✅ 유저 정보 조회
            UserEntity user = userService.findByEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword());

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 올바르지 않습니다.");
            }

            // ✅ 로그인 성공 시 세션 저장
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());

            return ResponseEntity.ok(Map.of("message", "로그인 성공!", "username", user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 실패: " + e.getMessage());
        }
    }*/
    
    /*
    
    @PostMapping("/locallogin")
    public ResponseEntity<?> login(@RequestBody UserEntity loginRequest, HttpSession session) {
        UserEntity user = userService.findByEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword());

        if (user == null) {
            return ResponseEntity.status(401).body("로그인 실패: 이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // ✅ 로그인 성공 로그 추가
        System.out.println("로그인 성공: " + user.getUsername());

        // ✅ 세션에 저장
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getUsername());  // ✅ 여기에서 저장

        return ResponseEntity.ok(Map.of("message", "로그인 성공!", "userName", user.getUsername()));
    }*/
    @PostMapping("/locallogin")
    public ResponseEntity<?> login(@RequestBody UserEntity loginRequest, HttpSession session) {
        UserEntity user = userService.findByEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword());

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "로그인 실패: 이메일 또는 비밀번호가 올바르지 않습니다."));
        }

        // ✅ 로그인 성공 시 세션 저장
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getUsername());

        return ResponseEntity.ok(Map.of("message", "로그인 성공!", "userName", user.getUsername()));
    }



    @Transactional
    public UserEntity saveOrUpdateUser(String socialId, String userName, String userEmail) {
        Optional<UserEntity> existingUserOpt = userRepository.findBySocialId(socialId);

        if (existingUserOpt.isPresent()) {
            UserEntity existingUser = existingUserOpt.get();
            existingUser.setUsername(userName);
            existingUser.setEmail(userEmail);
            System.out.println("Updating existing user: " + existingUser);

            return userRepository.save(existingUser); // ✅ 기존 사용자 업데이트
        } else {
            UserEntity newUser = new UserEntity();
            newUser.setSocialId(socialId);
            newUser.setUsername(userName);
            newUser.setEmail(userEmail);
            System.out.println("Saving new user: " + newUser);

            return userRepository.save(newUser); // ✅ 새 사용자 저장
        }
    }

    @Transactional
    public UserEntity saveOrUpdateSocialUser(String socialId, String userName, String userEmail, String socialProvider,String Role) {
        Optional<UserEntity> existingUserOpt = userRepository.findBySocialId(socialId);

        if (existingUserOpt.isPresent()) {
            UserEntity existingUser = existingUserOpt.get();
            existingUser.setUsername(userName);
            existingUser.setEmail(userEmail);
            existingUser.setSocialProvider(socialProvider);
            existingUser.setRole(Role);
            System.out.println("Updating existing user: " + existingUser);

            return userRepository.save(existingUser); // ✅ 기존 사용자 업데이트
        } else {
            UserEntity newUser = new UserEntity();
            newUser.setSocialId(socialId);
            newUser.setUsername(userName);
            newUser.setEmail(userEmail);
            newUser.setSocialProvider(socialProvider);
            newUser.setRole(Role);
            System.out.println("Saving new user: " + newUser);

            return userRepository.save(newUser); // ✅ 새 사용자 저장
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



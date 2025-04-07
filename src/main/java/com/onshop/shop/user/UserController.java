package com.onshop.shop.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
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
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onshop.shop.address.Address;
import com.onshop.shop.address.AddressRepository;
import com.onshop.shop.order.Order;
import com.onshop.shop.order.OrderRepository;
import com.onshop.shop.security.JwtUtil;
import com.onshop.shop.seller.SellerRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // ✅ 프론트엔드에서 쿠키 사용 허용

@Slf4j
public class UserController {

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

	private final RestTemplate restTemplate;// 외부 API에서 데이터를 가져오기위해 필요함

	private final UserRepository userRepository;
	private final SellerRepository sellerRepository;
	private final OrderRepository orderRepository;
	private final JwtUtil jwtUtil; // ✅ JWT 유틸 추가

	public UserController(RestTemplate restTemplate, UserRepository userRepository,SellerRepository sellerRepository,OrderRepository orderRepository,JwtUtil jwtUtil) {
		this.restTemplate = restTemplate;// 외부 API에서 데이터를 가져오기위해 필요함
		this.userRepository = userRepository;
		this.sellerRepository = sellerRepository;
		this.orderRepository = orderRepository;
		this.jwtUtil = jwtUtil;
	}

	// 쿠키를 사용
	@GetMapping("/user-info")
	public ResponseEntity<Map<String, Object>> getUserInfo(@CookieValue(value = "jwt", required = false) String token) {
		if (token == null) {
			return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
		}

		Long userId = jwtUtil.extractUserId(token); // ✅ JWT에서 userId 추출
		User user = userRepository.findById(userId).orElse(null); // ✅ 인스턴스 방식으로 호출

		if (user == null) {
			return ResponseEntity.status(404).body(Map.of("error", "유저 정보를 찾을 수 없습니다."));
		}


		return ResponseEntity.ok(Map.of("userId", user.getUserId().toString(), 
				"userName", user.getUsername(),
		        "userEmail", Optional.ofNullable(user.getEmail()).orElse(""),
				"userPassword" , user.getPassword(),
				"userPhone1", Optional.ofNullable(user.getPhone1()).orElse(""),
			    "userPhone2", Optional.ofNullable(user.getPhone2()).orElse(""),
			    "userPhone3", Optional.ofNullable(user.getPhone3()).orElse("")
				));
	}

	@GetMapping("/user-infoemail")
	public ResponseEntity<Map<String, String>> getUserInfoemail(
			@CookieValue(value = "jwt", required = false) String token) {
		if (token == null) {
			return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
		}

		Long userId = jwtUtil.extractUserId(token); // ✅ JWT에서 userId 추출
		User user = userRepository.findById(userId).orElse(null); // ✅ 인스턴스 방식으로 호출

		if (user == null) {
			return ResponseEntity.status(404).body(Map.of("error", "유저 정보를 찾을 수 없습니다."));
		}

		return ResponseEntity.ok(Map.of("userId", user.getUserId().toString(), "userName", user.getUsername(),
				"userEmail", user.getEmail()));
	}

	/* 로그아웃 시 쿠키 삭제 */
	@GetMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
		Cookie cookie = new Cookie("jwt", null);
		cookie.setHttpOnly(true);// javascript에서 접근불가
		cookie.setSecure(true);// https로만 들어오게
		cookie.setPath("/");
		cookie.setMaxAge(0); // 쿠키 만료
		response.addCookie(cookie);

		return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
	}

	@Autowired
	private AddressRepository addressRepository;

	@Transactional
	@PutMapping("/update-userinfo")
	public ResponseEntity<String> updateUserInfo(@RequestBody UserUpdateRequest request,
			@CookieValue(value = "jwt", required = false) String token) {
		System.out.println("📢 회원 정보 수정 요청 받음: " + request);
		System.out.println("📢 회원 정보 수정 요청 받음: " + request.getUserId());

		// JWT 토큰이 쿠키에 없으면 401 Unauthorized 응답
		if (token == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 없습니다.");
		}

		// JWT에서 유저 ID 추출
		Long userIdFromJwt = jwtUtil.extractUserId(token); // JWT에서 userId 추출

		// 요청된 userId와 JWT에서 추출된 userId가 다르면 권한 없음 처리
		if (!request.getUserId().equals(userIdFromJwt)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("권한이 없습니다.");
		}

		// 사용자 찾기
		User user = userRepository.findById(request.getUserId()).orElse(null);
		if (user == null) {
			System.out.println("❌ 유저를 찾을 수 없습니다. userId: " + request.getUserId());
			return ResponseEntity.badRequest().body("유저를 찾을 수 없습니다.");
		}

		// 이메일 & 비밀번호 업데이트
		System.out.println("✅ 기존 이메일: " + user.getEmail() + " → 변경될 이메일: " + request.getEmail());
		user.setEmail(request.getEmail());
		user.setPassword(request.getPassword());

		// 전화번호 업데이트
		List<String> phones = request.getPhones();
		System.out.println("✅ 기존 전화번호: " + user.getPhone1() + " → 변경될 전화번호 리스트: " + phones);
		user.setPhone1(phones.size() > 0 ? phones.get(0) : null);
		user.setPhone2(phones.size() > 1 ? phones.get(1) : null);
		user.setPhone3(phones.size() > 2 ? phones.get(2) : null);

		System.out.println("📌 받은 요청 데이터: " + request);

	    // ✅ 기존 주소 리스트
	    List<Address> existingAddresses = addressRepository.findByUser(user);

	    // ✅ 기본 주소 초기화
	    for (Address addr : existingAddresses) {
	        addr.setIsDefault(false);
	    }

	    // ✅ 프론트에서 전달된 address1 + post 조합 추출
	    Set<String> incomingKeys = request.getAddresses().stream()
	        .map(addr -> (addr.getAddress1().trim() + "::" + addr.getPost().trim()))
	        .collect(Collectors.toSet());

	    // ✅ 삭제 대상 추출
	    List<Address> toDelete = existingAddresses.stream()
	        .filter(addr -> !incomingKeys.contains(addr.getAddress1().trim() + "::" + addr.getPost().trim()))
	        .collect(Collectors.toList());

	    // ✅ User의 주소 리스트에서도 제거 (orphanRemoval을 위해)
	    for (Address addr : toDelete) {
	        user.getAddresses().remove(addr);
	    }

	    addressRepository.deleteAll(toDelete);
	    System.out.println("🗑️ 삭제된 주소 수: " + toDelete.size());

	    // ✅ 중복 아닌 새 주소만 저장하고, 기존 주소 중 기본 주소만 재설정
	    List<Address> newAddresses = request.getAddresses().stream()
	        .limit(3)
	        .filter(addrReq -> {
	            boolean exists = addressRepository.existsByUserAndAddress1AndPost(user, addrReq.getAddress1(), addrReq.getPost());

	            if (exists && Boolean.TRUE.equals(addrReq.getIsDefault())) {
	                Address existing = addressRepository.findByUserAndAddress1AndPost(user, addrReq.getAddress1(), addrReq.getPost());
	                if (existing != null) {
	                    existing.setIsDefault(true);
	                    addressRepository.save(existing);
	                }
	            }

	            return !exists;
	        })
	        .map(addrReq -> {
	            Address address = new Address();
	            address.setUser(user);
	            address.setAddress1(addrReq.getAddress1());
	            address.setAddress2(addrReq.getAddress2());
	            address.setPost(addrReq.getPost());
	            address.setIsDefault(addrReq.getIsDefault());
	            return address;
	        })
	        .collect(Collectors.toList());

	    // ✅ 기본 주소가 여러 개면 하나만 true로
	    boolean hasDefault = false;
	    for (Address addr : newAddresses) {
	        if (Boolean.TRUE.equals(addr.getIsDefault())) {
	            if (hasDefault) {
	                addr.setIsDefault(false);
	            } else {
	                hasDefault = true;
	            }
	        }
	    }

	    // ✅ 기존 주소에도 기본 주소가 없고, 새 주소도 기본이 없을 경우 → 첫 번째 새 주소를 기본으로 설정
	    boolean dbHasDefault = addressRepository.findByUser(user).stream()
	        .anyMatch(Address::getIsDefault);

	    if (!dbHasDefault && !newAddresses.isEmpty()) {
	        newAddresses.get(0).setIsDefault(true);
	        System.out.println("⚠️ 기본 주소 없음 → 첫 번째 새 주소를 기본으로 설정");
	    }

	    addressRepository.saveAll(newAddresses);
	    userRepository.save(user);

	    System.out.println("✅ 새로 저장된 주소 수: " + newAddresses.size());
	    System.out.println("✅ 회원 정보 수정 완료");

	    return ResponseEntity.ok("회원 정보가 수정되었습니다!");
	}

	
	
	@Autowired
	private UserService userService;

	@PostMapping("/signup") // 로컬유저 회원가입
	public ResponseEntity<?> signUp(@RequestBody User user, HttpSession session) {
		try {
			if (user.getAddresses() == null) {
				user.setAddresses(new ArrayList<>()); // ✅ addresses가 null이면 초기화
			}
			
			 BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		        String encodedPassword = passwordEncoder.encode(user.getPassword());
		        user.setPassword(encodedPassword); // 암호화된 비밀번호 저장
			
	        // 🛠 각 주소에 user 객체 연결
	        for (Address addr : user.getAddresses()) {
	            addr.setUser(user); // <-- 이게 핵심
	        }

			userService.registerUser(user);
			// ✅ userId 확인
			System.out.println("로컬 UserName: " + user.getUsername());
			System.out.println("로컬 UserId: " + user.getUserId());

			return ResponseEntity.ok("회원가입 성공!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 실패: " + e.getMessage());
		}
	}

	@PostMapping("/locallogin") // 로컬 로그인
	public ResponseEntity<?> login(@RequestBody User loginRequest, HttpServletResponse response) {
		User user = userService.findByEmail(loginRequest.getEmail());

		if (user == null) {
			return ResponseEntity.status(401).body(Map.of("error", "로그인 실패: 이메일 또는 비밀번호가 올바르지 않습니다."));
		}
		
		 BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
		        return ResponseEntity.status(401).body(Map.of("error", "로그인 실패: 비밀번호가 일치하지 않습니다."));
		    }

		// JWT 생성
		String token = jwtUtil.generateToken(user.getUserId());

		// 쿠키에 JWT 설정
		Cookie cookie = new Cookie("jwt", token);
		System.out.println("jwtUtil.generateToken(user.getUserId());:" + token);
		cookie.setHttpOnly(true); // 클라이언트에서 접근 불가
		cookie.setPath("/"); // 쿠키의 유효 경로 설정
		response.addCookie(cookie); // 쿠키 추가
		System.out.println("message" + "로그인 성공~!" + "userId" + user.getUserId() + "userName" + user.getUsername());
		return ResponseEntity
				.ok(Map.of("message", "로그인 성공~!", "userId", user.getUserId(), "userName", user.getUsername()));
	}

	@GetMapping("/naver")
	public ResponseEntity<String> naverLoginRedirect() {
		String state = generateState();
		String url = "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" + naverClientId
				+ "&redirect_uri=" + naverRedirectUri + "&state=" + state;
		return ResponseEntity.status(302).header("Location", url).build();
	}

	@GetMapping("/naver/callback")
	public ResponseEntity<String> naverCallback(@RequestParam String code, @RequestParam String state,
	                                            HttpServletResponse response) throws IOException {
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
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get access token");
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
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get user info");
	        }

	        JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody()).path("response");
	        String naverId = userInfo.get("id").asText();
	        String userName = userInfo.has("name") ? userInfo.get("name").asText() : "네이버유저";
	        String userEmail = userInfo.has("email") ? userInfo.get("email").asText() : "";

	        UserRole role = UserRole.CUSTOMER;
	        User user = saveOrUpdateSocialUser(naverId, userName, userEmail, "naver", role);

	        String token = jwtUtil.generateToken(user.getUserId());
	        Cookie cookie = new Cookie("jwt", token);
	        cookie.setHttpOnly(true);
	        cookie.setSecure(true);
	        cookie.setPath("/");
	        cookie.setMaxAge(3600);
	        response.addCookie(cookie);

	        return ResponseEntity.status(HttpStatus.FOUND)
	                .header(HttpHeaders.LOCATION, "http://localhost:5173/")
	                .build();
	    } catch (Exception e) {
	        log.error("❌ 네이버 콜백 처리 중 예외 발생: ", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("네이버 로그인 중 오류 발생: " + e.getMessage());
	    }

	}

	@GetMapping("/kakao")
	public ResponseEntity<String> kakaoLoginRedirect() {
		// 카카오 로그인 페이지로 리다이렉트
		String url = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + kakaoClientId
				+ "&redirect_uri=" + kakaoRedirectUri;
		return ResponseEntity.status(302) // 302 상태 코드로 리다이렉트
				.header("Location", url) // 카카오 로그인 페이지로 리다이렉트
				.build();
	}

	@GetMapping("/kakao/callback")
	public ResponseEntity<String> kakaoCallback(@RequestParam String code, HttpServletResponse response)
			throws IOException {
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
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to get access token from Kakao");
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
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get user info from Kakao");
		}

		JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody());
		String kakaoId = userInfo.get("id").asText();
		String userName = userInfo.path("kakao_account").path("profile").path("nickname").asText();
		String userEmail = userInfo.path("kakao_account").path("email").asText();

		String socialProvider = "kakao";

		UserRole role = UserRole.CUSTOMER;


		// 사용자 정보를 저장하거나 업데이트
		User user = saveOrUpdateSocialUser(kakaoId, userName, userEmail, socialProvider, role);

		// JWT 생성 후 쿠키에 저장
		String token = jwtUtil.generateToken(user.getUserId());
		Cookie cookie = new Cookie("jwt", token);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60); // 1시간 유효
		response.addCookie(cookie);

		// 카카오 로그인 후 홈 페이지로 리다이렉트
		return ResponseEntity.status(HttpStatus.FOUND) // 302 상태 코드 (리다이렉트)
				.header(HttpHeaders.LOCATION, "http://localhost:5173") // 홈 페이지로 리다이렉트
				.build();
	}

	@Transactional
	public User saveOrUpdateUser(String socialId, String userName, String userEmail) {
		Optional<User> existingUserOpt = userRepository.findBySocialId(socialId);

		if (existingUserOpt.isPresent()) {
			User existingUser = existingUserOpt.get();
			existingUser.setUsername(userName);
			existingUser.setEmail(userEmail);
			System.out.println("Updating existing user: " + existingUser);

			return userRepository.save(existingUser); // ✅ 기존 사용자 업데이트
		} else {
			User newUser = new User();
			newUser.setSocialId(socialId);
			newUser.setUsername(userName);
			newUser.setEmail(userEmail);
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

	@Transactional
	public User saveOrUpdateSocialUser(String socialId, String userName, String userEmail, String socialProvider,
			UserRole role) {
		Optional<User> existingUserOpt = userRepository.findBySocialId(socialId);

		if (existingUserOpt.isPresent()) {
			return existingUserOpt.get();
		} else {
			User newUser = new User();
			newUser.setSocialId(socialId);
			newUser.setUsername(userName);
			newUser.setEmail(userEmail);
			newUser.setSocialProvider(socialProvider);
			newUser.setRole(role);
			return userRepository.save(newUser);
		}
	}

	/** 이매일 인증 */

	// 인증번호 발송
	@PostMapping("/email/send-code")
	public ResponseEntity<?> sendVerificationCode(@Valid @RequestBody EmailAuthRequestDTO emailRequestDTO) {

		log.info("email:{}", emailRequestDTO);

		try {
			userService.sendVerificationCode(emailRequestDTO.getEmail());
		} catch (MessagingException ex) {
			return null;
		}

		return ResponseEntity.ok(null);
	}

	// 인증번호 검증
	@PostMapping("/email/code-verify")
	public ResponseEntity<?> emailVerification(@Valid @RequestBody EmailVerificationRequestDTO verificationRequestDTO) {

		boolean isVer = userService.emailVerification(verificationRequestDTO);
		if (isVer) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok().body("success");

	}

	/** TODO: 구현중 */
	// 아이디 찾기
	@PostMapping("/auth/find-email")
	public ResponseEntity<?> findEmail(@Valid @RequestBody ForgetReqeustDTO forgetReqeustDTO) {

		ForgetResponseDTO forgetResponseDTO = userService.findUserEmail(forgetReqeustDTO);
		return ResponseEntity.ok(forgetResponseDTO);
	}

	// 비밀번호 찾기
	public ResponseEntity<?> findPassword(@Valid @RequestBody ForgetReqeustDTO forgetReqeustDTO) {

		return ResponseEntity.ok(null);
	}

	@PatchMapping("/users/{userId}/update-seller")
	public ResponseEntity<?> promoteToSeller(@PathVariable Long userId, @RequestParam String storename) {
	    userService.promoteToSellerAndNotify(userId, storename);
	    return ResponseEntity.ok("SELLER 승격 및 메일 발송 완료");
	}
	
	@PatchMapping("/users/{userId}/reject-seller")
	public ResponseEntity<?> rejectSeller(@PathVariable Long userId, @RequestParam String storename) {
	    userService.rejectSellerAndNotify(userId, storename);
	    return ResponseEntity.ok("SELLER 거절 처리 및 메일 발송 완료");
	}
	
	@GetMapping("/users/{userId}/phone")
	public ResponseEntity<String> getUserPhones(@PathVariable Long userId) {
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("해당 고객을 찾을 수 없습니다"));

	    String fullPhone = String.join("-", user.getPhones());
	    return ResponseEntity.ok(fullPhone);
	}
	@Transactional
	@DeleteMapping("/usersout/{userId}")
	public ResponseEntity<String> goodByUser(@PathVariable Long userId) {
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("해당 고객을 찾을 수 없습니다"));
	    List<Order> orders = orderRepository.findByUser(user);
	    for (Order order : orders) {
	        order.setUser(null);
	    }	    orderRepository.deleteByUser(user);
	    sellerRepository.deleteByUserId(userId);
	    userRepository.deleteById(userId); 
	    return ResponseEntity.ok("회원 탈퇴 완료");
	}
	
	@PostMapping("/check-current-password")
	public ResponseEntity<Map<String, String>> checkCurrentPassword(@RequestBody Map<String, String> request, 
	                                                                  @CookieValue(value = "jwt", required = false) String token) {
	    // JWT 토큰이 없으면 로그인하지 않은 상태
	    if (token == null) {
	        return ResponseEntity.status(401).body(Map.of("error", "로그인 정보가 없습니다."));
	    }

	    // JWT에서 userId 추출
	    Long userId = jwtUtil.extractUserId(token);

	    // 사용자 정보 조회
	    User user = userRepository.findById(userId).orElse(null);
	    if (user == null) {
	        return ResponseEntity.status(404).body(Map.of("error", "유저를 찾을 수 없습니다."));
	    }

	    // 현재 비밀번호 확인
	    String currentPassword = request.get("currentPassword");
	    if (currentPassword == null || !BCrypt.checkpw(currentPassword, user.getPassword())) { // BCrypt 사용해서 비교
	        return ResponseEntity.status(400).body(Map.of("error", "현재 비밀번호가 일치하지 않습니다."));
	    }

	    return ResponseEntity.ok(Map.of("message", "현재 비밀번호가 확인되었습니다."));
	}

	@PatchMapping("/update-password")
	public ResponseEntity<Map<String, String>> updatePassword(@RequestBody Map<String, String> request, 
	                                             @CookieValue(value = "jwt", required = false) String token) {
	    // JWT 토큰이 없으면 로그인하지 않은 상태
	    if (token == null) {
	        return ResponseEntity.status(401).body(Map.of("error","로그인 정보가 없습니다."));
	    }

	    // JWT에서 userId 추출
	    Long userId = jwtUtil.extractUserId(token);

	    // 사용자 정보 조회
	    User user = userRepository.findById(userId).orElse(null);
	    if (user == null) {
	        return ResponseEntity.status(404).body(Map.of("error","유저를 찾을 수 없습니다."));
	    }

	    // 현재 비밀번호 확인
	    String currentPassword = request.get("currentPassword");
	    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	    if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
	        return ResponseEntity.status(400).body(Map.of("error","현재 비밀번호가 일치하지 않습니다."));
	    }

	    // 새 비밀번호와 비밀번호 확인이 일치하는지 확인
	    String newPassword = request.get("newPassword");
	    String confirmNewPassword = request.get("confirmNewPassword");
	    if (!newPassword.equals(confirmNewPassword)) {
	        return ResponseEntity.status(400).body(Map.of("message","새 비밀번호와 비밀번호 확인이 일치하지 않습니다."));
	    }

	    // 새 비밀번호로 변경
	    String encodedNewPassword = passwordEncoder.encode(newPassword); // 새 비밀번호 암호화
	    user.setPassword(encodedNewPassword);
	    userRepository.save(user);

	    return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
	}





}
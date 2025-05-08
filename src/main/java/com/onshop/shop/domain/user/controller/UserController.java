package com.onshop.shop.domain.user.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
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

import com.onshop.shop.domain.address.entity.Address;
import com.onshop.shop.domain.address.repository.AddressRepository;
import com.onshop.shop.domain.order.entity.Order;
import com.onshop.shop.domain.order.repository.OrderRepository;
import com.onshop.shop.domain.seller.repository.SellerRepository;
import com.onshop.shop.domain.user.dto.UserUpdateRequestDTO;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.domain.user.repository.UserRepository;
import com.onshop.shop.domain.user.service.UserService;
import com.onshop.shop.global.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // 프론트엔드에서 쿠키 사용 허용
@RequiredArgsConstructor
@Slf4j
public class UserController {


   private final UserRepository userRepository;
   
   private final SellerRepository sellerRepository;
   private final OrderRepository orderRepository;
   private final AddressRepository addressRepository;
   private final UserService userService;

   private final JwtUtil jwtUtil; 


   // 쿠키를 사용
   @GetMapping("/user-info")
   public ResponseEntity<Map<String, Object>> getUserInfo(@CookieValue(value = "jwt", required = false) String token) {
      if (token == null) {
         return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
      }

      Long userId = jwtUtil.extractUserId(token); // JWT에서 userId 추출
      User user = userRepository.findById(userId).orElse(null); // 인스턴스 방식으로 호출

      if (user == null) {
         return ResponseEntity.status(404).body(Map.of("error", "유저 정보를 찾을 수 없습니다."));
      }

      return ResponseEntity.ok(Map.of("userId", user.getUserId().toString(), 
             "userName", user.getUsername(),
             "userEmail", Optional.ofNullable(user.getEmail()).orElse(""),
             "userPhone1", Optional.ofNullable(user.getPhone1()).orElse(""),
             "userPhone2", Optional.ofNullable(user.getPhone2()).orElse(""),
             "userPhone3", Optional.ofNullable(user.getPhone3()).orElse(""),
             "userRole", user.getRole()
            ));
   }

   @GetMapping("/user-infoemail")
   public ResponseEntity<Map<String, String>> getUserInfoemail(
         @CookieValue(value = "jwt", required = false) String token) {
      if (token == null) {
         return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
      }

      Long userId = jwtUtil.extractUserId(token); // JWT에서 userId 추출
      User user = userRepository.findById(userId).orElse(null); // 인스턴스 방식으로 호출

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


   @Transactional
   @PutMapping("/update-userinfo")
   public ResponseEntity<String> updateUserInfo(@RequestBody UserUpdateRequestDTO request,
         @CookieValue(value = "jwt", required = false) String token) {

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
         return ResponseEntity.badRequest().body("유저를 찾을 수 없습니다.");
      }

      // 이메일 & 비밀번호 업데이트
      user.setEmail(request.getEmail());
      user.setPassword(request.getPassword());

      // 전화번호 업데이트
      List<String> phones = request.getPhones();
      user.setPhone1(phones.size() > 0 ? phones.get(0) : null);
      user.setPhone2(phones.size() > 1 ? phones.get(1) : null);
      user.setPhone3(phones.size() > 2 ? phones.get(2) : null);


       // 기존 주소 리스트
       List<Address> existingAddresses = addressRepository.findByUser(user);

       // 기본 주소 초기화
       for (Address addr : existingAddresses) {
           addr.setIsDefault(false);
       }

       // 프론트에서 전달된 address1 + post 조합 추출
       Set<String> incomingKeys = request.getAddresses().stream()
           .map(addr -> (addr.getAddress1().trim() + "::" + addr.getPost().trim()))
           .collect(Collectors.toSet());

       // 삭제 대상 추출
       List<Address> toDelete = existingAddresses.stream()
           .filter(addr -> !incomingKeys.contains(addr.getAddress1().trim() + "::" + addr.getPost().trim()))
           .collect(Collectors.toList());

       // User의 주소 리스트에서도 제거 (orphanRemoval을 위해)
       for (Address addr : toDelete) {
           user.getAddresses().remove(addr);
       }

       addressRepository.deleteAll(toDelete);
       System.out.println("🗑️ 삭제된 주소 수: " + toDelete.size());

       // 중복 아닌 새 주소만 저장하고, 기존 주소 중 기본 주소만 재설정
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

       // 기본 주소가 여러 개면 하나만 true로
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

       // 기존 주소에도 기본 주소가 없고, 새 주소도 기본이 없을 경우 → 첫 번째 새 주소를 기본으로 설정
       boolean dbHasDefault = addressRepository.findByUser(user).stream()
           .anyMatch(Address::getIsDefault);

       if (!dbHasDefault && !newAddresses.isEmpty()) {
           newAddresses.get(0).setIsDefault(true);
           System.out.println("⚠️ 기본 주소 없음 → 첫 번째 새 주소를 기본으로 설정");
       }

       addressRepository.saveAll(newAddresses);
       userRepository.save(user);


       return ResponseEntity.ok("회원 정보가 수정되었습니다!");
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
       }       orderRepository.deleteByUser(user);
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
   
	@PostMapping("/reset-password")
	public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
	    String email = request.get("email");
	    String password = request.get("password");

	    Optional<User> userOpt = userRepository.findByEmail(email);
	    if (userOpt.isEmpty()) return ResponseEntity.status(404).body("유저 없음");

	    User user = userOpt.get();
	    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	    user.setPassword(encoder.encode(password));
	    userRepository.save(user);

	    return ResponseEntity.ok("비밀번호 재설정 완료");
	}
	

}
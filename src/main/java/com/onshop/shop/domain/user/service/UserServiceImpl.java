package com.onshop.shop.domain.user.service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onshop.shop.domain.address.entity.Address;
import com.onshop.shop.domain.address.repository.AddressRepository;
import com.onshop.shop.domain.user.dto.ForgetResponseDTO;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.domain.user.enums.UserRole;
import com.onshop.shop.domain.user.repository.UserRepository;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final JavaMailSender javaMailSender;
    
    @Value("${main.sender.email}")
    private String senderEmail;
    


    // 이메일로 유저 조회
    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 (Lazy Loading 문제 방지)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }


    // 소셜 로그인 ID로 유저 조회
    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 (Lazy Loading 문제 방지)
    public User findBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId).orElse(null);
    }

    
    // 유저 주소 업데이트
    @Transactional
    public void updateUser(User updatedUser) {
        User existingUser = userRepository.findByUserId(updatedUser.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 기존 주소 삭제
        addressRepository.deleteAllByUserId(existingUser.getUserId());

        // 새로운 주소 저장
        List<Address> newAddresses = updatedUser.getAddresses().stream()
            .map(address -> {
                Address addressEntity = new Address();
                addressEntity.setUser(existingUser);
                addressEntity.setAddress1(address.getAddress1());
                addressEntity.setAddress2(address.getAddress2());
                addressEntity.setPost(address.getPost());
                addressEntity.setIsDefault(address.getIsDefault());
                return addressEntity;
            }).collect(Collectors.toList());

        addressRepository.saveAll(newAddresses);

        //유저 정보 업데이트
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhones(updatedUser.getPhones());
        userRepository.save(existingUser);
    }
    
    
    // 특정 유저 정보 조회
    @Override
    public User getUserById(Long userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }


   
    // 판매자 승인 메일링
	@Override
	@Transactional
	public void promoteToSellerAndNotify(Long userId, String storeName) {
	    User user = userRepository.findById(userId)
	        .orElseThrow(() -> new RuntimeException("유저 정보를 찾을 수 없습니다."));

	    // 1. 역할 변경
	    user.setRole(UserRole.SELLER);
	    userRepository.save(user);

	    // 2. 승인 메일 전송
	    try {
	        MimeMessage message = javaMailSender.createMimeMessage();
	        message.setFrom(senderEmail);
	        message.setRecipients(MimeMessage.RecipientType.TO, user.getEmail());
	        message.setSubject("[온샵] 판매자 등록 승인 안내");

	        String body = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
	            + "<h2 style='color: #2c3e50;'>🎉 판매자 등록 승인 완료</h2>"
	            + "<p>안녕하세요 <strong>" + user.getUsername() + "</strong>님,</p>"
	            + "<p>요청하신 스토어 <strong>[" + storeName + "]</strong>의 판매자 등록이 승인되었습니다.</p>"
	            + "<p>이제 상품 등록과 판매 기능을 자유롭게 사용하실 수 있습니다.</p>"
	            + "<p style='margin-top: 20px;'>감사합니다.</p>"
	            + "</div>";

	        message.setText(body, "UTF-8", "html");

	        javaMailSender.send(message);
	        log.info("✅ 판매자 승인 메일 전송 완료 → {}", user.getEmail());
	    } catch (Exception e) {
	        log.warn("❌ 판매자 승인 메일 전송 실패: {}", e.getMessage());
	    }
	}
	
	
	// 판매자 등록 거절 메일링
	@Override
	@Transactional
	public void rejectSellerAndNotify(Long userId, String storeName) {
	    User user = userRepository.findById(userId)
	        .orElseThrow(() -> new RuntimeException("유저 정보를 찾을 수 없습니다."));

	    // 메일 발송 (role 변경은 없음)
	    try {
	        MimeMessage message = javaMailSender.createMimeMessage();
	        message.setFrom(senderEmail);
	        message.setRecipients(MimeMessage.RecipientType.TO, user.getEmail());
	        message.setSubject("[온샵] 판매자 등록 거절 안내");

	        String body = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
	            + "<h2 style='color: #e74c3c;'>❌ 판매자 등록 거절 안내</h2>"
	            + "<p>안녕하세요 <strong>" + user.getUsername() + "</strong>님,</p>"
	            + "<p>요청하신 스토어 <strong>[" + storeName + "]</strong>의 판매자 등록이 <strong style='color:red;'>거절</strong>되었습니다.</p>"
	            + "<p>신청 내용 또는 서류를 다시 확인 후 재신청 부탁드립니다.</p>"
	            + "<p style='margin-top: 20px;'>감사합니다.</p>"
	            + "</div>";

	        message.setText(body, "UTF-8", "html");
	        javaMailSender.send(message);
	        log.info("📩 판매자 거절 메일 전송 완료 → {}", user.getEmail());

	    } catch (Exception e) {
	        log.warn("❌ 판매자 거절 메일 전송 실패: {}", e.getMessage());
	    }
	}



	 // 유저 정보 업데이트
	@Transactional
	public User saveOrUpdateUser(String socialId, String userName, String userEmail) {
		      User existingUser = userRepository.findBySocialId(socialId).orElse(null);
	
		      // 기존에 존재하는 유저라면 기존 정보를 업데이트
		      if (existingUser != null) {
		         existingUser.setUsername(userName);
		         existingUser.setEmail(userEmail);
		         System.out.println("Updating existing user: " + existingUser);
		         
		         return userRepository.save(existingUser); // 기존 사용자 업데이트
		         
		         // 새유저라면 새로 저장
		      } else {
		         User newUser = new User();
		         newUser.setSocialId(socialId);
		         newUser.setUsername(userName);
		         newUser.setEmail(userEmail);
		         System.out.println("Saving new user: " + newUser);
	
		         return userRepository.save(newUser); // 새 사용자 저장
		      }
		   }

		// 소셜 로그인 유저 정보 업데이트
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


	// 회원가입 처리
	@Override
	public User registerUser(User user) {
		return userRepository.save(user);
		
	}


	// 유저 존재 유무
	@Override
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}


	// 유저 이름과 비밀번호로 일치 정보 조회
	@Override
	public ForgetResponseDTO findByUsernameAndPassword(String username, String password) {
		return userRepository.findByUsernameAndPassword(username, password);
	}

}

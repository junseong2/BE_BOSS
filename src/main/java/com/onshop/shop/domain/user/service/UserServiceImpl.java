package com.onshop.shop.domain.user.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onshop.shop.domain.address.dto.AddressRequestDTO;
import com.onshop.shop.domain.address.entity.Address;
import com.onshop.shop.domain.address.repository.AddressRepository;
import com.onshop.shop.domain.order.entity.Order;
import com.onshop.shop.domain.order.repository.OrderRepository;
import com.onshop.shop.domain.seller.repository.SellerRepository;
import com.onshop.shop.domain.user.dto.ForgetResponseDTO;
import com.onshop.shop.domain.user.dto.UserUpdateRequestDTO;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.domain.user.enums.UserRole;
import com.onshop.shop.domain.user.repository.UserRepository;
import com.onshop.shop.global.exception.ResourceNotFoundException;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 구현 클래스입니다.
 * 사용자 조회, 등록, 업데이트, 삭제 및 판매자 승인/거절 등 다양한 사용자 관련 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    // === 의존성 주입 ===
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final JavaMailSender javaMailSender;

    @Value("${main.sender.email}")
    private String senderEmail;

    /**
     * 이메일로 사용자 정보를 조회합니다.
     *
     * @param email 조회할 사용자의 이메일
     * @return 해당 이메일을 가진 사용자 객체, 없으면 null 반환
     */
    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * 소셜 로그인 ID로 사용자 정보를 조회합니다.
     *
     * @param socialId 소셜 ID
     * @return 해당 소셜 ID를 가진 사용자 객체, 없으면 null 반환
     */
    @Override
    @Transactional(readOnly = true)
    public User findBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId).orElse(null);
    }

    /**
     * 주어진 사용자 정보로 주소와 이메일, 전화번호를 포함한 정보를 업데이트합니다.
     * 기존 주소는 삭제되고 새로운 주소로 대체됩니다.
     *
     * @param updatedUser 업데이트할 사용자 객체
     * @throws ResourceNotFoundException 존재하지 않는 사용자 ID일 경우
     */
    @Transactional
    public void updateUser(User updatedUser) {
        User existingUser = userRepository.findByUserId(updatedUser.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("유저 정보가 없습니다."));

        addressRepository.deleteAllByUserId(existingUser.getUserId());

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

        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhones(updatedUser.getPhones());
        userRepository.save(existingUser);
    }

    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 객체, 없으면 null
     */
    @Override
    public User getUserById(Long userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }

    /**
     * 사용자를 판매자(customer -> seller) 로 승격시키고, 승인 메일을 전송합니다.
     *
     * @param userId 판매자로 승격시킬 사용자 ID
     * @param storeName 승인된 스토어 이름
     * @throws ResourceNotFoundException 사용자 정보가 없을 경우
     */
    @Override
    @Transactional
    public void promoteToSellerAndNotify(Long userId, String storeName) {
        User user = userRepository.findById(userId)
        		  .orElseThrow(() -> new ResourceNotFoundException("유저 정보가 없습니다."));

        user.setRole(UserRole.SELLER); // 사용자를 판매자(customer -> seller) 로 승격
        userRepository.save(user);

        // 승인 메시지 전송
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

    /**
     * 판매자 등록 요청을 거절하고 메일을 전송합니다.
     *
     * @param userId 사용자 ID
     * @param storeName 거절된 스토어 이름
     * @throws ResourceNotFoundException 사용자 정보가 없을 경우
     */
    @Override
    @Transactional
    public void rejectSellerAndNotify(Long userId, String storeName) {
        User user = userRepository.findById(userId)
         		  .orElseThrow(() -> new ResourceNotFoundException("유저 정보가 없습니다."));

        
        // 승인 거절 메시지 전송
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

    /**
     * 소셜 로그인 사용자를 저장하거나 기존 사용자 정보를 업데이트합니다.
     *
     * @param socialId 소셜 ID
     * @param userName 사용자 이름
     * @param userEmail 사용자 이메일
     * @return 저장된 사용자 객체
     */
    @Transactional
    public User saveOrUpdateUser(String socialId, String userName, String userEmail) {
        User existingUser = userRepository.findBySocialId(socialId).orElse(null);

        if (existingUser != null) {
            existingUser.setUsername(userName);
            existingUser.setEmail(userEmail);
            System.out.println("Updating existing user: " + existingUser);
            return userRepository.save(existingUser);
        } else {
            User newUser = new User();
            newUser.setSocialId(socialId);
            newUser.setUsername(userName);
            newUser.setEmail(userEmail);
            System.out.println("Saving new user: " + newUser);
            return userRepository.save(newUser);
        }
    }

    /**
     * 소셜 로그인 사용자 저장 또는 기존 사용자 반환 (소셜 제공자 및 역할 포함).
     *
     * @param socialId 소셜 ID
     * @param userName 사용자 이름
     * @param userEmail 사용자 이메일
     * @param socialProvider 소셜 제공자 (ex. KAKAO, NAVER 등)
     * @param role 부여할 역할 (기본 USER 또는 SELLER 등)
     * @return 저장된 사용자 객체 또는 기존 사용자
     */
    @Transactional
    public User saveOrUpdateSocialUser(String socialId, String userName, String userEmail, String socialProvider, UserRole role) {
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


    /**
     * 사용자 등록 처리 (회원가입).
     */
    @Override
    public User registerUser(User user) {
        return userRepository.save(user);
    }

    
    /**
     * 주어진 이메일로 사용자가 존재하는지 확인합니다.
     */
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    /**
     * 사용자 이름과 비밀번호로 ForgetResponseDTO를 조회합니다.
     * 주로 비밀번호 찾기 시 사용됩니다.
     *
     * @param username 사용자 이름
     * @param password 사용자 비밀번호
     * @return 해당 조건을 만족하는 ForgetResponseDTO
     */
    @Override
    public ForgetResponseDTO findByUsernameAndPassword(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password);
    }

    /**
     * 사용자 탈퇴 처리. 주문 정보, 판매자 정보 등 관련 데이터도 함께 삭제합니다.
     *
     * @param userId 탈퇴할 사용자 ID
     * @throws RuntimeException 사용자가 존재하지 않을 경우
     */
    @Override
    @Transactional
    public void deleteById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 고객을 찾을 수 없습니다"));

        List<Order> orders = orderRepository.findByUser(user);
        for (Order order : orders) {
            order.setUser(null);
        }
        orderRepository.deleteByUser(user);
        sellerRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }

    /**
     * 사용자 비밀번호가 현재 비밀번호와 일치하는지 검증합니다.
     *
     * @param currentPassword 사용자가 입력한 현재 비밀번호
     * @param userId 사용자 ID
     * @return 결과 메시지를 담은 Map (성공 또는 오류 상태 코드 포함)
     */
    @Override
    public Map<String, String> verifyPassword(String currentPassword, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Map.of("404", "해당 유저는 존재하지 않습니다.");
        }

        if (currentPassword == null || !BCrypt.checkpw(currentPassword, user.getPassword())) {
            return Map.of("400", "현재 비밀번호가 일치하지 않습니다.");
        }

        return Map.of("success", "비밀번호가 일치합니다.");
    }

    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     */
    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * 사용자 정보를 업데이트합니다. (이메일, 비밀번호, 전화번호, 주소 등)
     *
     * @param request 사용자 요청 DTO
     * @param user 업데이트 대상 사용자 엔티티
     */
    @Override
    @Transactional
    public void updateUserInfo(UserUpdateRequestDTO request, User user) {
        user.setEmail(request.getEmail());

        // 비밀번호 암호화 적용
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(request.getPassword()));

        // 전화번호 처리
        List<String> phones = request.getPhones();
        user.setPhone1(phones.size() > 0 ? phones.get(0) : null);
        user.setPhone2(phones.size() > 1 ? phones.get(1) : null);
        user.setPhone3(phones.size() > 2 ? phones.get(2) : null);

        // 기존 주소 조회 및 초기화
        List<Address> existingAddresses = addressRepository.findByUser(user);
        existingAddresses.forEach(addr -> addr.setIsDefault(false));

        // 주소 식별 키 생성 메서드(ex. '주소::우편변호' 형태로 만들어서 incomingKeys와 비교함)
        Function<AddressRequestDTO, String> toKey = addr -> addr.getAddress1().trim() + "::" + addr.getPost().trim();

        Set<String> incomingKeys = request.getAddresses().stream()
            .map(toKey)
            .collect(Collectors.toSet());

        // 삭제할 주소 필터링(DB에는 있지만 요청에는 없는 주소들을 찾음)
        List<Address> toDelete = existingAddresses.stream()
            .filter(addr -> !incomingKeys.contains(addr.getAddress1().trim() + "::" + addr.getPost().trim()))
            .collect(Collectors.toList());

        // 더 이상 안 쓰는 옛 주소를 제거 후 DB 반영
        toDelete.forEach(addr -> user.getAddresses().remove(addr));
        addressRepository.deleteAll(toDelete);

        // 새 주소 처리 및 기본 주소 설정
        List<Address> newAddresses = new ArrayList<>();
        boolean hasDefault = false;

        for (AddressRequestDTO addrReq : request.getAddresses()) {
            boolean exists = addressRepository.existsByUserAndAddress1AndPost(user, addrReq.getAddress1(), addrReq.getPost());

            // 기존 주소가 기본이라면 기본값만 갱신
            if (exists && Boolean.TRUE.equals(addrReq.getIsDefault())) {
                Address existing = addressRepository.findByUserAndAddress1AndPost(user, addrReq.getAddress1(), addrReq.getPost());
                if (existing != null) {
                    existing.setIsDefault(true);
                    addressRepository.save(existing);
                    hasDefault = true;
                }
                continue;
            }

            // 새 주소 생성
            Address address = new Address();
            address.setUser(user);
            address.setAddress1(addrReq.getAddress1());
            address.setAddress2(addrReq.getAddress2());
            address.setPost(addrReq.getPost());

            // 기본 주소 처리
            boolean isDefault = Boolean.TRUE.equals(addrReq.getIsDefault());
            if (isDefault && !hasDefault) {
                address.setIsDefault(true);
                hasDefault = true;
            } else {
                address.setIsDefault(false);
            }

            newAddresses.add(address);
        }

        // 기본 주소가 전혀 없을 경우 → 첫 주소를 기본으로 지정
        if (!hasDefault && !newAddresses.isEmpty()) {
            newAddresses.get(0).setIsDefault(true);
            log.info("기본 주소 없음 → 첫 번째 새 주소를 기본으로 설정");
        }

        addressRepository.saveAll(newAddresses);
        userRepository.save(user);
    }
    
    

    /**
     * 사용자 전화번호를 하나의 문자열로 반환합니다.
     *
     * @param userId 사용자 ID
     * @return 전화번호 문자열 ("010-1234-5678" 형식)
     * @throws ResourceNotFoundException 유저가 없을 경우
     */
    @Override
    public String getUserPhones(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 고객을 찾을 수 없습니다"));

        return String.join("-", user.getPhones());
    }

    /**
     * 사용자 비밀번호를 현재 비밀번호 검증 후 새 비밀번호로 변경합니다.
     *
     * @param request 비밀번호 변경 요청 Map (currentPassword, newPassword, confirmNewPassword)
     * @param userId 사용자 ID
     * @return 결과 메시지를 담은 Map
     */
    @Override
    public Map<String, String> updatePassword(Map<String, String> request, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Map.of("404", "유저를 찾을 수 없습니다.");
        }

        String currentPassword = request.get("currentPassword");
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            return Map.of("400", "현재 비밀번호가 일치하지 않습니다.");
        }

        String newPassword = request.get("newPassword");
        String confirmNewPassword = request.get("confirmNewPassword");
        if (!newPassword.equals(confirmNewPassword)) {
            return Map.of("message", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);
        userRepository.save(user);

        return Map.of("200", "변경 되었습니다.");
    }

    /**
     * 이메일을 기반으로 비밀번호를 재설정합니다.
     *
     * @param email 사용자 이메일
     * @param password 새 비밀번호
     * @throws ResourceNotFoundException 유저를 찾을 수 없는 경우
     */
    @Override
    public void resetPassword(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("유저가 아닙니다.");
        }

        User user = userOpt.get();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(password));
        userRepository.save(user);
    }

}

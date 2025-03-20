package com.onshop.shop.user;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.onshop.shop.address.Address;
import com.onshop.shop.address.AddressRepository;
import com.onshop.shop.exception.BadRequestException;
import com.onshop.shop.util.EmailUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender javaMailSender;
    private final String senderEmail;
    private final EmailUtils emailUtils;
    
    @Autowired
    public UserServiceImpl(
    		UserRepository userRepository, 
    		AddressRepository addressRepository,  
    		RedisTemplate<String, Object> redisTemplate,
    		JavaMailSender javaMailSender,
    		EmailUtils emailUtils,
    		@Value("${main.sender.email}") String senderEmail
    		) {
    	
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.redisTemplate =redisTemplate;
        this.javaMailSender = javaMailSender;
        this.senderEmail = senderEmail;
        this.emailUtils = emailUtils;
        
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED) // ✅ 트랜잭션 적용
    public void registerUser(User user) {
        User savedUser = userRepository.save(user);

        List<Address> addresses = user.getAddresses() != null ? user.getAddresses() : List.of();
        if (!addresses.isEmpty()) {
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

            addressRepository.saveAll(addressEntities);
        }
    }

    @Override
    @Transactional(readOnly = true) // ✅ 읽기 전용 트랜잭션 (Lazy Loading 문제 방지)
    public User findByEmailAndPassword(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password).orElse(null);
    }

    @Override
    @Transactional(readOnly = true) // ✅ 읽기 전용 트랜잭션 (Lazy Loading 문제 방지)
    public User findBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId).orElse(null);
    }

    @Transactional
    public void updateUser(User updatedUser) {
        User existingUser = userRepository.findByUserId(updatedUser.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ✅ 기존 주소 삭제
        addressRepository.deleteAllByUserId(existingUser.getUserId());

        // ✅ 새로운 주소 저장
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

        // ✅ 유저 정보 업데이트
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhones(updatedUser.getPhones());
        userRepository.save(existingUser);
    }
    
    /** 이메일 인증*/

    @Override
    // 이메일 포맷 설정 
    public MimeMessage createMail(String email, String authCode) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.setFrom(senderEmail);
        message.setRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("이메일 인증");
        String body = "<html lang='ko'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>이메일 인증</title>" +
                "</head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f9; margin: 0; padding: 0; text-align: center;'>" +
                "<div style='width: 100%; max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1); padding: 30px; text-align: center;'>" +
                "<h3 style='font-size: 24px; color: #333333; margin-bottom: 20px; font-weight: bold;'>Mincho 사이트 인증 번호</h3>" +
                "<p style='font-size: 18px; color: #555555;'>요청하신 인증 번호는 아래와 같습니다.</p>" +
                "<div style='font-size: 48px; color: #4CAF50; font-weight: bold; background-color: #f1f8e9; padding: 20px; border-radius: 10px; display: inline-block; margin: 20px 0;'>" +
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

         Boolean isUser = userRepository.existsByEmail(toMail);

         // TODO: 커스텀 예외 적용해야 함.
         if(isUser){
             throw new BadRequestException("잘못된 요청임니다. 이전 단계를 완료 후 진행해주세요.");
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
	public User getUserById(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

}

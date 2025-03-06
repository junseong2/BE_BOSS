package com.onshop.shop.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.onshop.shop.user.AddressRepository;
import com.onshop.shop.user.UserRepository;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public UserServiceImpl(UserRepository userRepository, AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED) // ✅ 트랜잭션 적용
    public void registerUser(UserEntity user) {
        UserEntity savedUser = userRepository.save(user);

        List<AddressEntity> addresses = user.getAddresses() != null ? user.getAddresses() : List.of();
        if (!addresses.isEmpty()) {
            List<AddressEntity> addressEntities = addresses.stream()
                .map(address -> {
                    AddressEntity addressEntity = new AddressEntity();
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
    public UserEntity findByEmailAndPassword(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password).orElse(null);
    }

    @Override
    @Transactional(readOnly = true) // ✅ 읽기 전용 트랜잭션 (Lazy Loading 문제 방지)
    public UserEntity findBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId).orElse(null);
    }

    @Transactional
    public void updateUser(UserEntity updatedUser) {
        UserEntity existingUser = userRepository.findById(updatedUser.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ✅ 기존 주소 삭제
        addressRepository.deleteAllByUserId(existingUser.getUserId());

        // ✅ 새로운 주소 저장
        List<AddressEntity> newAddresses = updatedUser.getAddresses().stream()
            .map(address -> {
                AddressEntity addressEntity = new AddressEntity();
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


}

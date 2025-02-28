package com.onshop.shop.user;

import java.util.List;
import java.util.stream.Collectors;

import com.onshop.shop.user.AddressEntity; // ✅ 올바른 import 확인

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onshop.shop.user.repository.AddressRepository;

@Service
public class UserServiceImpl implements UserService { // ✅ UserService 인터페이스 구현
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public UserServiceImpl(UserRepository userRepository, AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional
    public void registerUser(UserEntity user) {
        // User 저장
        UserEntity savedUser = userRepository.save(user);

        // addresses가 null이면 빈 리스트 사용 (NullPointerException 방지)
        List<AddressEntity> addresses = user.getAddresses() != null ? user.getAddresses() : List.of();

        // 주소를 사용자와 매핑 후 저장
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
    public UserEntity findByEmailAndPassword(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password).orElse(null);
    }

}

package com.onshop.shop.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.onshop.shop.address.Address;
import com.onshop.shop.address.AddressRepository;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository, AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
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
    
    @Override
    public User getUserById(Long userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }


}

package com.onshop.shop.domain.address.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.domain.address.dto.AddressDTO;
import com.onshop.shop.domain.address.entity.Address;
import com.onshop.shop.domain.address.repository.AddressRepository;

import lombok.RequiredArgsConstructor;

/**
 * 주소 관련 요청을 처리하는 REST 컨트롤러입니다.
 */
@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepository;

    /**
     * 특정 사용자(userId)의 주소 목록을 조회합니다.
     *
     * @param userId 주소를 조회할 사용자 ID
     * @return 사용자 주소 리스트를 담은 {@link ResponseEntity}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(@PathVariable Long userId) {
        List<Address> addressList = addressRepository.findByUserUserId(userId);
        List<AddressDTO> dtoList = addressList.stream()
                                              .map(AddressDTO::fromEntity)
                                              .toList();
        return ResponseEntity.ok(dtoList);
    }

}

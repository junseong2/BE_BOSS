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

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController{
	
	private AddressRepository addressRepository;
	
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<AddressDTO>> getUserAddresses(@PathVariable Long userId) {
	    List<Address> addressList = addressRepository.findByUserUserId(userId);
	    List<AddressDTO> dtoList = addressList.stream()
	                                          .map(AddressDTO::fromEntity)
	                                          .toList();
	    return ResponseEntity.ok(dtoList);
	}

}

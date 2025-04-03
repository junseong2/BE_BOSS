package com.onshop.shop.address;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/address")
public class AddressController{
	
	private AddressRepository addressRepository;
	
	
    @Autowired
    public AddressController(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<AddressDTO>> getUserAddresses(@PathVariable Long userId) {
	    List<Address> addressList = addressRepository.findByUserUserId(userId);
	    List<AddressDTO> dtoList = addressList.stream()
	                                          .map(AddressDTO::fromEntity)
	                                          .toList();
	    return ResponseEntity.ok(dtoList);
	}
	
    

}

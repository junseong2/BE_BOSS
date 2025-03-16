package com.onshop.shop.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

	@GetMapping("/")
	ResponseEntity<?> root(){
		Map<String, String> map = new HashMap<>();
		map.put("message", "안녕하세요");
		return ResponseEntity.ok(map);
	}

}

	package com.onshop.shop.seller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onshop.shop.product.Product;
import com.onshop.shop.product.ProductsService;
import com.onshop.shop.security.JwtUtil;
 
@RestController
@RequestMapping("/seller")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SellerController {

	private static final Logger logger = LoggerFactory.getLogger(SellerController.class);

	@Autowired
	private SellerService sellerService;

	@Autowired
	private JwtUtil jwtUtil; // ✅ JWT 유틸리티 추가

	// ✅ Seller 정보 가져오기 (JWT 인증 기반, storename으로 검색)
	@GetMapping("/info/{storename}")
	public ResponseEntity<Map<String, Object>> getSellerInfoByStoreName(@PathVariable String storename,
			@CookieValue(value = "jwt", required = false) String token) {

		// ✅ 판매자 정보 조회
		Optional<Seller> sellerOptional = sellerService.getSellerByStorename(storename);
		if (sellerOptional.isPresent()) {
			Seller seller = sellerOptional.get();

			// 🔥 ✅ `userId` 검증 제거 (누구나 판매자 정보 조회 가능)
			Map<String, Object> response = Map.of(

					"storename", seller.getStorename(), "sellerId", seller.getSellerId());

			System.out.println("Response Data: " + response); // 응답 데이터 로그

			return ResponseEntity.ok(response);
		}

		return ResponseEntity.status(404).body(Map.of("error", "판매자를 찾을 수 없습니다."));
	}

	@PutMapping("/{sellerId}/updateAllSettings")
	public ResponseEntity<Map<String, Object>> updateAllSettings(@PathVariable Long sellerId,
			@RequestBody Map<String, Object> settingsData) {

		try {
			logger.info("🔧 받은 리퀘스트 " + settingsData);

			if (sellerId == null || sellerId <= 0) {
				return ResponseEntity.badRequest().body(Map.of("error", "Invalid sellerId"));
			}

			if (!settingsData.containsKey("settings") || !settingsData.containsKey("mobilesettings")) {
				return ResponseEntity.badRequest().body(Map.of("error", "설정 데이터가 없습니다."));
			}

			ObjectMapper objectMapper = new ObjectMapper();

			// JSON을 List<Map<String, Object>> 형태로 변환
			List<Map<String, Object>> settingsList = objectMapper.convertValue(settingsData.get("settings"),
					new TypeReference<>() {
					});
			List<Map<String, Object>> mobileSettingsList = objectMapper.convertValue(settingsData.get("mobilesettings"),
					new TypeReference<>() {
					});

			// 공통 처리 로직
			processSettingsList(settingsList, "PC");
			processSettingsList(mobileSettingsList, "Mobile");

			// settings와 mobilesettings를 DB에 저장
			sellerService.updateSellerAllSettings(sellerId, settingsList);
			sellerService.updateSellerAllMobileSettings(sellerId, mobileSettingsList);

			return ResponseEntity.ok(Map.of("message", "설정이 성공적으로 업데이트되었습니다."));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류 발생", "message", e.getMessage()));
		}
	}

	/**
	 * 설정 리스트를 처리하는 공통 메서드
	 */
	private void processSettingsList(List<Map<String, Object>> settingsList, String deviceType) {
		for (Map<String, Object> setting : settingsList) {
			String type = (String) setting.get("type");
			System.out.println(deviceType + " 처리 타입: " + type);

			switch (type) {
			case "header":
				System.out.println(deviceType + " 헤더 처리: " + setting);
				break;
			case "banner":
				System.out.println(deviceType + " 배너 처리: " + setting);
				break;
			case "grid":
				System.out.println(deviceType + " 그리드 처리: " + setting);
				break;
			default:
				System.out.println(deviceType + " 알 수 없는 타입: " + type);
				break;
			}
		}
	}

	@PutMapping("/{sellerId}/updateSettings")
	public ResponseEntity<Map<String, Object>> updateSellerSettings(@PathVariable Long sellerId,
			@RequestBody String settings) {

		try {
			logger.info("🔧 받은 리퀘스트 " + settings);

			if (sellerId == null || sellerId <= 0) {
				return ResponseEntity.badRequest().body(Map.of("error", "Invalid sellerId"));
			}

			if (settings == null || settings.isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "설정 데이터가 없습니다."));
			}

			// settings는 문자열로 들어오므로, 이를 List로 변환
			ObjectMapper objectMapper = new ObjectMapper();
			List<Map<String, Object>> settingsList = objectMapper.readValue(settings, List.class);

			// 받은 순서대로 처리
			for (Map<String, Object> setting : settingsList) {
				String type = (String) setting.get("type");
				logger.info("처리 타입: " + type); // banner → grid → header 순 출력

				// 각 type에 따른 처리
				if ("header".equals(type)) {
					// 헤더 처리
					logger.info("헤더 처리: " + setting);
					// 헤더 관련 로직 추가
				} else if ("banner".equals(type)) {
					// 배너 처리
					logger.info("배너 처리: " + setting);
					// 배너 관련 로직 추가
				} else if ("grid".equals(type)) {
					// 그리드 처리
					logger.info("그리드 처리: " + setting);
					// 그리드 관련 로직 추가
				} else {
					// 알 수 없는 type 처리
					logger.warn("알 수 없는 타입: " + type);
				}
			}

			// settings를 DB에 저장
			Seller updatedSeller = sellerService.updateSellerSettings(sellerId, settings);

			return ResponseEntity.ok(Map.of("message", "설정이 성공적으로 업데이트되었습니다."));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류 발생", "message", e.getMessage()));
		}
	}

	@PutMapping("/{sellerId}/updateMobileSettings")
	public ResponseEntity<Map<String, Object>> updateSellerMobileSettings(@PathVariable Long sellerId,
			@RequestBody String mobilesettings) {

		try {
			logger.info("🔧 받은 리퀘스트 " + mobilesettings);

			if (sellerId == null || sellerId <= 0) {
				return ResponseEntity.badRequest().body(Map.of("error", "Invalid sellerId"));
			}

			if (mobilesettings == null || mobilesettings.isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "설정 데이터가 없습니다."));
			}

			// settings는 문자열로 들어오므로, 이를 List로 변환
			ObjectMapper objectMapper = new ObjectMapper();
			List<Map<String, Object>> mobilesettingsList = objectMapper.readValue(mobilesettings, List.class);

			// 받은 순서대로 처리
			for (Map<String, Object> setting : mobilesettingsList) {
				String type = (String) setting.get("type");
				logger.info("처리 타입: " + type); // banner → grid → header 순 출력

				// 각 type에 따른 처리
				if ("header".equals(type)) {
					// 헤더 처리
					logger.info("헤더 처리: " + setting);
					// 헤더 관련 로직 추가
				} else if ("banner".equals(type)) {
					// 배너 처리
					logger.info("배너 처리: " + setting);
					// 배너 관련 로직 추가
				} else if ("grid".equals(type)) {
					// 그리드 처리
					logger.info("그리드 처리: " + setting);
					// 그리드 관련 로직 추가
				} else {
					// 알 수 없는 type 처리
					logger.warn("알 수 없는 타입: " + type);
				}
			}

			// settings를 DB에 저장
			Seller updatedSeller = sellerService.updateSellerMobilesettings(sellerId, mobilesettings);

			return ResponseEntity.ok(Map.of("message", "설정이 성공적으로 업데이트되었습니다."));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류 발생", "message", e.getMessage()));
		}
	}

	private final ObjectMapper objectMapper = new ObjectMapper(); // ✅ 인스턴스 추가

	@GetMapping("/page-data")
	public ResponseEntity<?> getSellerPageData(@RequestParam("seller_id") Long sellerId) {
	    return sellerService.getSellerById(sellerId).map(seller -> {
	        try {
	            // settings가 null이거나 빈 문자열이면 빈 리스트로 처리
	            List<Object> settings = Optional.ofNullable(seller.getSettings())
	                .filter(s -> !s.isBlank())
	                .map(s -> {
	                    try {
	                        return objectMapper.readValue(s, List.class);
	                    } catch (Exception e) {
	                        return new ArrayList<>();
	                    }
	                })
	                .orElse(new ArrayList<>());

	            // mobilesettings도 마찬가지로 처리
	            List<Object> mobilesettings = Optional.ofNullable(seller.getMobilesettings())
	                .filter(s -> !s.isBlank())
	                .map(s -> {
	                    try {
	                        return objectMapper.readValue(s, List.class);
	                    } catch (Exception e) {
	                        return new ArrayList<>();
	                    }
	                })
	                .orElse(new ArrayList<>());

	            Map<String, Object> response = Map.of(
	                "storename", seller.getStorename(),
	                "description", seller.getDescription(),
	                "settings", settings,
	                "mobilesettings", mobilesettings
	            );

	            return ResponseEntity.ok(response);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return ResponseEntity.badRequest().body("JSON 파싱 오류");
	        }
	    }).orElse(ResponseEntity.badRequest().body("판매자 데이터를 찾을 수 없습니다."));

	}

	private final String uploadDir = "C:/uploads/";

	@PostMapping("/upload")
	public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
			@RequestParam("sellerId") String sellerId, @RequestParam("type") String type) { // ✅ 파일 타입 추가

		if (file.isEmpty() || sellerId.isEmpty() || type.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "파일, 판매자 ID 또는 타입이 없습니다."));
		}

		try {
			// ✅ 파일명을 sellerId 기반으로 저장 (header 또는 banner 구분)
			String fileName;
			if ("header".equals(type)) {
				fileName = sellerId + "_headerlogo.png";
			} else if ("banner".equals(type)) {
				fileName = sellerId + "_banner.png";
			} else {
				return ResponseEntity.badRequest().body(Map.of("error", "잘못된 타입입니다."));
			}

			Path filePath = Paths.get("C:/uploads/" + fileName);
			Files.write(filePath, file.getBytes());

			String fileUrl = "/uploads/" + fileName; // ✅ 저장된 파일 URL 반환
			return ResponseEntity.ok(Map.of("url", fileUrl, "fileName", fileName));

		} catch (IOException e) {
			return ResponseEntity.status(500).body(Map.of("error", "파일 저장 실패: " + e.getMessage()));
		}
	}

	@GetMapping("/seller-info-byuserid/{userId}")
	public ResponseEntity<Map<String, Object>> getSellerInfoByUserId(@PathVariable Long userId) {
		try {
			logger.info("🔍 Received request for userId: " + userId);

			if (userId == null || userId <= 0) {
				return ResponseEntity.badRequest().body(Map.of("error", "Invalid userId"));
			}

			Optional<Seller> sellerOptional = sellerService.getSellerByUserId(userId);
			if (sellerOptional.isPresent()) {
				Seller seller = sellerOptional.get();
				Map<String, Object> response = new HashMap<>();
				response.put("sellerId", seller.getSellerId());
				response.put("storename", seller.getStorename());

				// ✅ settings 값 추가 (JSON 문자열이면 그대로 반환)
				String settings = seller.getSettings();
				if (settings == null || settings.trim().isEmpty()) {
					response.put("settings", "N/A");
				} else {
					response.put("settings", settings);
				}
				String mobilesettings = seller.getMobilesettings();

				if (mobilesettings == null || mobilesettings.trim().isEmpty()) {
					response.put("mobilesettings", "N/A");
				} else {
					response.put("mobilesettings", mobilesettings);
				}

				logger.info("📢 Seller Info Response: " + response);
				return ResponseEntity.ok(response);
			}

			return ResponseEntity.status(404).body(Map.of("error", "판매자를 찾을 수 없습니다."));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류 발생", "message", e.getMessage()));
		}
	}

	@Autowired
	private ProductsService productsService; // ✅ 올바른 Service 주입

	@GetMapping("/seller-info/{sellerId}")
	public ResponseEntity<Map<String, Object>> getSellerInfoById(@PathVariable Long sellerId) {
		try {
			logger.info("🔍 Received request for sellerId: " + sellerId);

			if (sellerId == null || sellerId <= 0) {
				return ResponseEntity.badRequest().body(Map.of("error", "Invalid sellerId"));
			}

			Optional<Seller> sellerOptional = sellerService.getSellerById(sellerId);
			if (sellerOptional.isPresent()) {
				Seller seller = sellerOptional.get();
				Map<String, Object> response = new HashMap<>();
				response.put("sellerId", seller.getSellerId());
				response.put("storename", seller.getStorename());

				// ✅ settings 값 추가 (JSON 문자열이면 그대로 반환)
				String settings = seller.getSettings();

				logger.info("📢 Seller's websettings: " + seller.getSettings());
				if (settings == null || settings.trim().isEmpty()) {
					response.put("settings", "N/A");
				} else {
					response.put("settings", settings);
				}

				String mobilesettings = seller.getMobilesettings();
				logger.info("📢 Seller's mobilesettings: " + seller.getMobilesettings());

				if (mobilesettings == null || mobilesettings.trim().isEmpty()) {
					response.put("mobilesettings", "N/A");
				} else {
					response.put("mobilesettings", mobilesettings);
				}
				logger.info("📢 Seller Info Response: " + response);
				return ResponseEntity.ok(response);
			}

			return ResponseEntity.status(404).body(Map.of("error", "판매자를 찾을 수 없습니다."));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류 발생", "message", e.getMessage()));
		}
	}

	@GetMapping("/product")
	public ResponseEntity<Map<String, Object>> getProductsBySeller(@RequestParam Long sellerId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "8") int size,
			@RequestParam(defaultValue = "asc") String sort) {
		try {
			logger.info("🔍 Received request for sellerId2: " + sellerId);

			// sellerId가 null이거나 음수인 경우
			if (sellerId == null || sellerId <= 0) {
				return ResponseEntity.badRequest().body(Map.of("error", "Invalid sellerId"));
			}

			Pageable pageable = PageRequest.of(page, size,
					sort.equals("asc") ? Sort.by("price").ascending() : Sort.by("price").descending());

			// 이 부분에서 오류가 발생할 가능성 있음
			Page<Product> productsPage = productsService.getProductsBySeller(sellerId, pageable);
			System.out.println("🔍 Products Page Data: " + productsPage); // 페이지 데이터 출력

			if (productsPage.isEmpty()) {
				return ResponseEntity.noContent().build();
			}

			Map<String, Object> response = new HashMap<>();
			response.put("products", productsPage.getContent());
			response.put("currentPage", productsPage.getNumber());
			response.put("totalItems", productsPage.getTotalElements());
			response.put("totalPages", productsPage.getTotalPages());
			response.put("sortOrder", sort);

			logger.info("📢 Querying products for sellerId: " + sellerId);
			logger.info("📢 Query result: "
					+ (productsPage == null ? "NULL" : productsPage.getTotalElements() + " items found"));

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			e.printStackTrace(); // 예외 출력
			return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류 발생", "message", e.getMessage()));
		}
	}

	@PostMapping("/register")
	public ResponseEntity<String> registerSeller(@RequestBody SellerRequest request) {
	    Long userId = request.getUserId();

	    // 판매자 등록 진행
	    Seller seller = sellerService.registerSeller(
	        request.getUserId(),
	        request.getStorename(),
	        request.getDescription(),
	        request.getRepresentativeName(),
	        request.getBusinessRegistrationNumber(),
	        request.getOnlineSalesNumber()
	    );

	    return ResponseEntity.status(201).body("판매자 등록 신청이 완료되었습니다.");
	}
	
	@GetMapping("/check/{userId}")
	public ResponseEntity<Map<String, Boolean>> checkIfUserIsSeller(@PathVariable Long userId) {
	    boolean isSeller = sellerService.isUserAlreadySeller(userId);
	    return ResponseEntity.ok(Map.of("isSeller", isSeller));
	}


	@GetMapping("/all")
	public ResponseEntity<List<Seller>> getAllSellers() {
		List<Seller> sellers = sellerService.getAllSellers();
		return ResponseEntity.ok(sellers);
	}
	
	@PatchMapping("/{sellerId}/approve")
	public ResponseEntity<?> approveSeller(@PathVariable Long sellerId) {
	    sellerService.approveSeller(sellerId);
	    return ResponseEntity.ok("승인 처리 완료");
	}
	
	@PatchMapping("/{sellerId}/reject")
	public ResponseEntity<?> rejectSeller(@PathVariable Long sellerId) {
	    sellerService.rejectSeller(sellerId);
	    return ResponseEntity.ok("거절 처리 완료");
	}
	
    @GetMapping("/seller-stats")
    public ResponseEntity<SellerStatsDTO> getSellerStats() {
        SellerStatsDTO stats = sellerService.getSellerStats();
        return ResponseEntity.ok(stats); // 통계 데이터를 반환
    }
}

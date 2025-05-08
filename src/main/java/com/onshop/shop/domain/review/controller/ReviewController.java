package com.onshop.shop.domain.review.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.onshop.shop.domain.review.dto.ReviewRequestDTO;
import com.onshop.shop.domain.review.dto.ReviewResponseDTO;
import com.onshop.shop.domain.review.dto.SellerAnswerRequestDTO;
import com.onshop.shop.domain.review.dto.SellerReviewResponseDTO;
import com.onshop.shop.domain.review.dto.SellerReviewSearchConditionDTO;
import com.onshop.shop.domain.review.service.ReviewService;
import com.onshop.shop.global.exception.BadRequestException;
import com.onshop.shop.global.exception.NotAuthException;
import com.onshop.shop.global.file.FileUploadService;
import com.onshop.shop.global.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping()
@Slf4j
public class ReviewController {
	
	private final FileUploadService fileUploadService;
	private final ReviewService reviewService;
	private final JwtUtil jwtUtil;
	
	
	// 리뷰 조회
	@GetMapping("/products/{productId}/reviews")
	public ResponseEntity<ReviewResponseDTO> getReviews(
			@RequestParam int page,
			@RequestParam int size,
			@RequestParam String sortby,
			@PathVariable Long productId
		) {
		

		ReviewResponseDTO reviews = reviewService.getReviews(productId, page, size);
		
		return ResponseEntity.ok(reviews);
		
	}
	
	
	// 리뷰 추가
	@PostMapping("/products/{productId}/reviews")
	public ResponseEntity<ReviewResponseDTO> createReview(
			@PathVariable Long productId,
		    @RequestParam(value = "ratings", required = false) Long ratings,
		    @RequestParam(value = "reviewText", required = false) String reviewText,
			@RequestParam(value = "images", required = false) List<MultipartFile> images,
			@CookieValue(value = "jwt", required = false) String token) {
		
	
		
        if (token == null) {
            throw new NotAuthException("요청권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token); // JWT에서 userId 추출
        log.info("ratings:{}", ratings);
        log.info("images:{}", images);
        log.info("reviewText:{}", reviewText);
        
        if(ratings <=0L) {
        	throw new BadRequestException("평점은 필수입니다.");
        }
        
        if(reviewText.isEmpty()) {
        	throw new BadRequestException("리뷰 내용은 필수입니다.");
        }
        
        
        List<String> uploadImages = images.stream().map(fileUploadService::upload).toList();
        
		reviewService.createReview(		
				ReviewRequestDTO.builder()
				.productId(productId)
				.reviewText(reviewText)
				.ratings(ratings.intValue())
				.images(uploadImages)
				.userId(userId)
				.build());
		
		return ResponseEntity.created(null).build();
		
	}
	
	// 리뷰 수정
	@PatchMapping("/products/{productId}/reviews/{reviewId}")
	public ResponseEntity<?> updateReview(
			@PathVariable Long productId,
			@PathVariable Long reviewId,
			@Valid @RequestBody ReviewRequestDTO reviewRequestDTO,
			@CookieValue(value = "jwt", required = false) String token) {
		
		
        if (token == null) {
            throw new NotAuthException("요청권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token); // ✅ JWT에서 userId 추출
		
		reviewRequestDTO.setReviewId(reviewId);
		reviewRequestDTO.setProductId(productId);
		reviewRequestDTO.setUserId(userId);
		
		reviewService.updateReview(reviewRequestDTO);
		
		return ResponseEntity.noContent().build();
		
	}
	
	// 리뷰 삭제
	@DeleteMapping("/products/{productId}/{reviewId}")
	public ResponseEntity<ReviewResponseDTO> deleteReview(
			@PathVariable Long reviewId,
			@CookieValue(value = "jwt", required = false) String token) {
		
        if (token == null) {
            throw new NotAuthException("요청권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token); 

		reviewService.deleteReview(reviewId, userId);
		
		return ResponseEntity.noContent().build();
	}
	
	/** 판매자 */
	// 판매자 리뷰 조회
	@GetMapping("/seller/reviews")
	public ResponseEntity<?> getReviews(
				@RequestParam int page,
				@RequestParam int size,
				@RequestParam String sortby,
	            @RequestParam(required = false) Integer rating,  
	            @RequestParam(required = false) Boolean isAnswered, 
				@CookieValue(value = "jwt", required = false) String token) {
		
        if (token == null) {
            throw new NotAuthException("요청권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token);
        
        
        SellerReviewResponseDTO review= reviewService.getSellerReviews(page, size, userId,SellerReviewSearchConditionDTO.builder()
        		.isAnswered(isAnswered)
        		.rating(rating)
        		.sortby(sortby)
        		.build());		
		
		
		return ResponseEntity.ok(review);
		
	}
	
	// 판매자 리뷰 답변 추가
	@PostMapping("/seller/reviews/{reviewId}/answer")
	public ResponseEntity<?> createReviewAnswer(
			@PathVariable Long reviewId,
			@Valid @RequestBody SellerAnswerRequestDTO sellerAnswerRequestDTO,
			@CookieValue(value = "jwt", required = false) String token) {
		
        if (token == null) {
            throw new NotAuthException("요청권한이 없습니다.");
        }
        
        Long userId = jwtUtil.extractUserId(token);

		
		if(reviewId == null) {
			throw new BadRequestException("리뷰 식별을 위한 ID는 필수입니다.");
		}
		
		reviewService.createSellerReview(sellerAnswerRequestDTO, userId, reviewId);

		
		return ResponseEntity.created(null).build();
		
	}
	
	// 판매자 리뷰 답변 수정
	@PatchMapping("/seller/reviews/{reviewId}/answer/{answerId}")
	public ResponseEntity<?> updateReviewAnswer(
			@PathVariable Long reviewId,
			@PathVariable Long answerId,
			@Valid @RequestBody SellerAnswerRequestDTO sellerAnswerRequestDTO,
			@CookieValue(value = "jwt", required = false) String token) {
		
        if (token == null) {
            throw new NotAuthException("요청권한이 없습니다.");
        }
        
        Long userId = jwtUtil.extractUserId(token);
        
        reviewService.updateSellerReview(sellerAnswerRequestDTO, userId, answerId);
        
        
        return ResponseEntity.noContent().build();

	}
	
	
	// 판매자 리뷰 답변 삭제
	@DeleteMapping("/seller/reviews/{reviewId}/answer/{answerId}")
	public ResponseEntity<?> deleteReviewAnswer(
			@PathVariable Long reviewId,
			@PathVariable Long answerId,
			@CookieValue(value = "jwt", required = false) String token) {
		
        if (token == null) {
            throw new NotAuthException("요청권한이 없습니다.");
        }
        
        Long userId = jwtUtil.extractUserId(token);
        
        reviewService.deleteReview(reviewId, userId);
        
        return ResponseEntity.noContent().build();

	}
	
}

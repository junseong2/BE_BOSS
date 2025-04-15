package com.onshop.shop.review;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.onshop.shop.exception.BadRequestException;
import com.onshop.shop.exception.DeleteFailureException;
import com.onshop.shop.exception.NotAuthException;
import com.onshop.shop.exception.ResourceNotFoundException;
import com.onshop.shop.product.Product;
import com.onshop.shop.product.ProductRepository;
import com.onshop.shop.seller.Seller;
import com.onshop.shop.seller.SellerRepository;
import com.onshop.shop.user.User;
import com.onshop.shop.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService  {
	
	private final ReviewRepsitory reviewRepsitory;
	private final ReviewAnswerRepository reviewAnswerRepository;
	private final UserRepository userRepository;
	private final SellerRepository sellerRepository;
	private final ProductRepository productRepository;
	
    @Value("${file.upload-dir}")  // application.properties에서 경로 정보를 읽어옴
    private String uploadDir;
    

	
	// 리뷰 조회
	@Override
	public ReviewResponseDTO getReviews(Long productId,Long userId, int page, int size) {
		
		Pageable pageable = (Pageable) PageRequest.of(page, size);
		
		log.info("productId:{}, userId:{}, page:{}, size:{}", productId, userId, page, size);
		
		
		List<ReviewsDTO> reviews = reviewRepsitory.findByProductId(productId,userId, pageable).toList();
		Long totalCount = reviewRepsitory.countByProductProductId(productId);
		AvgRatingResponseDTO avgRatingDTO = reviewRepsitory.findAvgRatingByProductId(productId);
		if(reviews.isEmpty()) {
			throw new ResourceNotFoundException("해당 상품의 리뷰를 조회할 수 없습니다.");
		}
		
		
		return ReviewResponseDTO.builder()
				.reviews(reviews)
				.totalCount(totalCount)
				.avgRating(avgRatingDTO.getAvgRating())
				.build();
	}
	

	// 리뷰 추가
	@Override
	public void createReview(ReviewRequestDTO reviewDTO) {
		
		Long userId = reviewDTO.getUserId();
		Long productId = reviewDTO.getProductId();
		
		User user =userRepository.findById(userId).orElseThrow(()->{
			 throw new NotAuthException("인증된 유저가 아닙니다.");
		 });
		 
		
		Product product = productRepository.findById(productId).orElseThrow(()->{
			 throw new ResourceNotFoundException("존재하지 않는 상품 정보입니다.");
		 });
		 
		if(reviewRepsitory.existsByProductProductIdAndUserUserId(productId, userId)) {
			throw new BadRequestException("이미 작성한 상품의 리뷰입니다.");
		}
		 
		 
		Review review = reviewRepsitory.save(Review.builder()
				 .product(product)
				 .rating(reviewDTO.getRatings())
				 .reviewText(reviewDTO.getReviewText())
				 .isAnswered(false)
				 .user(user)
				 .build());
		
	}

	// 리뷰 수정
	@Override
	public void updateReview(ReviewRequestDTO reviewDTO) {
		
		Long userId = reviewDTO.getUserId();
		Long productId = reviewDTO.getProductId();
		Long reviewId = reviewDTO.getReviewId();
		
		userRepository.findById(userId).orElseThrow(()->{
			 throw new NotAuthException("인증된 유저가 아닙니다.");
		 });
		 
		
		productRepository.findById(productId).orElseThrow(()->{
			 throw new ResourceNotFoundException("존재하지 않는 상품 정보입니다.");
		 });
		
		Review oldReview = reviewRepsitory.findById(reviewId).orElseThrow(()->{
			throw new ResourceNotFoundException("존재하지 않는 리뷰 입니다.");
		});
		
		oldReview.setRating(reviewDTO.getRatings());
//		oldReview.setGImages(reviewDTO.getImages());
		oldReview.setReviewText(reviewDTO.getReviewText());
		
		// 수정된 리뷰 저장
		reviewRepsitory.save(oldReview);
	
		
	}
	

	// 리뷰 삭제
	@Override
	@Transactional
	public void deleteReview(Long reviewId, Long userId) {
		
		 User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("요청 권한이 없습니다."));
		
		 int isDeleted = reviewRepsitory.deleteByReviewIdAndUser(reviewId, user);
		 
		 if(isDeleted != 1) {
			 throw new DeleteFailureException("리뷰:"+reviewId+"에 대한 삭제 요청을 처리하지 못 했습니다.");
		 }
	}

	// 리뷰 이미지 업로드
	@Override
	public void registerReviewImages(List<MultipartFile> images, Review review) {
	        // 이미지 파일 처리
	        List<String> imageNames = new ArrayList<>(); // g_image 저장용 리스트

	        for (MultipartFile image : images) {
	            String name = UUID.randomUUID() + "_" + image.getOriginalFilename(); // 랜덤 파일명 생성
	            String imageUrl = uploadDir + name;

	            // 파일을 서버에 저장하는 로직
	            File fileDir = new File(imageUrl);
	            try {
	                Files.createDirectories(Paths.get(uploadDir)); // 디렉토리 자동 생성
	                image.transferTo(fileDir); // 이미지 저장
	            } catch (IOException e) {
	                log.error(e.getMessage());
	            }

	            imageNames.add(name); // g_image 저장을 위해 파일명 추가
	        }

	        // g_image 업데이트 (파일명 리스트를 ,로 구분된 문자열로 변환하여 저장)
	        String gImageString = String.join(",", imageNames);
	        review.setGImages(gImageString);
	        reviewRepsitory.save(review);
	}


	// 평점 평균
	@Override
	public AvgRatingResponseDTO getAvgRating(Long productId) {
		AvgRatingResponseDTO avgRatingResponseDTO = reviewRepsitory.findAvgRatingByProductId(productId);
		return avgRatingResponseDTO ;
	}

	/** 판매자 */
	// 판매자 리뷰 조회
	@Override
	public SellerReviewResponseDTO getSellerReviews(int page, int size, Long userId, SellerReviewSearchConditionDTO condition) {
		
		Seller seller = sellerRepository.findByUserId(userId).orElseThrow(()-> new NotAuthException("판매자만 이용 가능합니다."));
		
		

		
		Pageable pageable = PageRequest.of(page, size);
		
		
		// 전체 개수
		Long totalCount = reviewRepsitory.countBySellerId(
				seller.getSellerId(),
				condition.getRating(),
				condition.getIsAnswered());
		
		// 리뷰 목록
		List<SellerReviewsDTO> reviews = reviewRepsitory.findAllBySellerId(
				seller.getSellerId(),
				condition.getRating(),
				condition.getIsAnswered(),
				pageable).toList();
		
		log.info("seller: {}, totalCount:{}, reviews:{}", seller, totalCount, reviews);
		 
		if(reviews.isEmpty()) {
			reviews = null;
		}
		
		return SellerReviewResponseDTO.builder()
				.reviews(reviews)
				.totalCount(totalCount)
				.build();
	}


	// 판매자 리뷰 추가
	@Override
	public void createSellerReview(SellerAnswerRequestDTO answerDTO, Long userId, Long reviewId) {
		Seller seller = sellerRepository.findByUserId(userId).orElseThrow(()-> new NotAuthException("판매자만 이용 가능합니다."));
		Review review = reviewRepsitory.findById(reviewId).orElseThrow(()-> new ResourceNotFoundException("해당 리뷰는 존재하지 않습니다."));
		
		review.setIsAnswered(true);
		
		ReviewAnswer unsavedAnswer = ReviewAnswer.builder()
		.answerText(answerDTO.getAnswerText())
		.seller(seller)
		.review(review)
		.build();
		
		reviewAnswerRepository.save(unsavedAnswer);
		reviewRepsitory.save(review);
	}


	// 판매자 리뷰 수정
	@Override
	public void updateSellerReview(SellerAnswerRequestDTO answerDTO, Long userId, Long reviewId) {
		sellerRepository.findByUserId(userId).orElseThrow(()-> new NotAuthException("판매자만 이용 가능합니다."));

		ReviewAnswer oldReviewAnswer = reviewAnswerRepository.findById(reviewId).orElseThrow(()-> new ResourceNotFoundException("해당 리뷰는 존재하지 않습니다."));
		oldReviewAnswer.setAnswerText(answerDTO.getAnswerText());
	}


	// 판매자 리뷰 삭제
	@Override
	public void deleteSellerReview(Long answerId, Long userId, Long reviewId) {
		Seller seller = sellerRepository.findByUserId(userId).orElseThrow(()-> new NotAuthException("판매자만 이용 가능합니다."));
		
		Boolean isAnswer = reviewAnswerRepository.existsByAnswerIdAndSeller(answerId, seller);
		if(!isAnswer) {
			throw new NotAuthException("해당 판매자만 가능한 요청입니다.");
		}
		reviewAnswerRepository.deleteById(answerId);
		
	}
}
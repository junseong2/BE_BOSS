package com.onshop.shop.review;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onshop.shop.seller.Seller;

public interface ReviewAnswerRepository extends JpaRepository<ReviewAnswer, Long>  {
	

	// 해당 응답 존재 유무
	Boolean existsByAnswerIdAndSeller(Long answerId, Seller seller);
	// 리뷰 답변 등록
	
	// 리뷰 답변 수정
	
	// 리뷰 답변 삭제

	
}

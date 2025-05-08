package com.onshop.shop.domain.orderDetail.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onshop.shop.domain.cart.entity.Cart;
import com.onshop.shop.domain.cart.repository.CartRepository;
import com.onshop.shop.domain.order.dto.OrderDTO;
import com.onshop.shop.domain.order.entity.Order;
import com.onshop.shop.domain.orderDetail.dto.OrderDetailResponseDTO;
import com.onshop.shop.domain.orderDetail.dto.ProductItemDTO;
import com.onshop.shop.domain.orderDetail.dto.SellerOrderDetailResponseDTO;
import com.onshop.shop.domain.orderDetail.entity.OrderDetail;
import com.onshop.shop.domain.orderDetail.repository.OrderDetailRepository;
import com.onshop.shop.domain.seller.entity.Seller;
import com.onshop.shop.domain.seller.repository.SellerRepository;
import com.onshop.shop.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * 주문 상세 서비스 구현 클래스
 * - 주문 상세 생성 및 조회 관련 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl implements OrderDetailService {

	private final OrderDetailRepository orderDetailRepository;
	private final CartRepository cartRepository;
	private final SellerRepository sellerRepository;

	/**
	 * 주문 상세 생성
	 * - 장바구니의 상품들을 기준으로 주문 상세(OrderDetail)를 생성하고 저장합니다.
	 *
	 * @param userId   주문한 사용자 ID
	 * @param orderDTO 주문 DTO (프론트 입력값)
	 * @param order    저장된 주문 객체
	 * @throws ResourceNotFoundException 장바구니가 비어 있는 경우
	 */
	@Override
	public void createOrderDetail(Long userId, OrderDTO orderDTO, Order order) {
		List<Cart> carts = cartRepository.findByUserId(userId);

		if (carts.isEmpty()) {
			throw new ResourceNotFoundException("조회할 장바구니 목록이 없습니다.");
		}

		List<OrderDetail> unsavedOrderDetails = carts.stream().map(cart ->
			OrderDetail.builder()
				.price(cart.getProduct().getPrice())
				.product(cart.getProduct())
				.totalPrice(cart.getQuantity() * cart.getProduct().getPrice())
				.quantity(cart.getQuantity())
				.order(order)
				.build()
		).toList();

		orderDetailRepository.saveAll(unsavedOrderDetails);
	}

	/**
	 * 주문 ID로 주문 상세 정보 조회
	 * - 주문 정보 및 상품 목록을 포함한 DTO 반환
	 *
	 * @param orderId 주문 ID
	 * @return 주문 상세 응답 DTO
	 * @throws ResourceNotFoundException 해당 주문 ID에 대한 정보가 없는 경우
	 */
	@Override
	public OrderDetailResponseDTO getDetailByOrderId(Long orderId) {
		OrderDetailResponseDTO dto = orderDetailRepository.findOrderMetaByOrderId(orderId);

		if (dto == null) {
			System.out.println("❌ 주문 상세 없음: orderId = " + orderId);
			throw new ResourceNotFoundException("주문 정보가 없습니다.");
		}

		List<ProductItemDTO> products = orderDetailRepository.findOrderProductsByOrderId(orderId);
		dto.setProducts(products);
		return dto;
	}

	/**
	 * 판매자별 주문 상세 내역 조회
	 * - 특정 주문 번호(orderId)에 대해, 해당 판매자(userId)가 판매한 상품만 추려 반환
	 *
	 * @param orderId 주문 ID
	 * @param userId  판매자 계정의 사용자 ID
	 * @return 판매자 주문 상세 응답 DTO
	 * @throws ResourceNotFoundException 판매자가 아니거나 주문 내역이 없는 경우
	 */
	@Override
	public SellerOrderDetailResponseDTO getOrderDetailByOrderId(Long orderId, Long userId) {
		Seller seller = sellerRepository.findByUserId(userId)
			.orElseThrow(() -> new ResourceNotFoundException("판매자만 가능합니다."));

		Long sellerId = seller.getSellerId();

		SellerOrderDetailResponseDTO orderDetail = orderDetailRepository.findOrderDetailsByOrderId(orderId, sellerId);

		if (orderDetail == null) {
			throw new ResourceNotFoundException("주문번호:" + orderId + "에 해당하는 주문상세 내역을 찾을 수 없습니다.");
		}

		return orderDetail;
	}
}

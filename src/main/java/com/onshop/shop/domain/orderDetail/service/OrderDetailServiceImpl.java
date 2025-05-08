package com.onshop.shop.domain.orderDetail.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onshop.shop.domain.cart.entity.Cart;
import com.onshop.shop.domain.cart.repository.CartRepository;
import com.onshop.shop.domain.order.dto.OrderDTO;
import com.onshop.shop.domain.order.entity.Order;
import com.onshop.shop.domain.order.repository.OrderRepository;
import com.onshop.shop.domain.orderDetail.dto.OrderDetailResponseDTO;
import com.onshop.shop.domain.orderDetail.dto.ProductItemDTO;
import com.onshop.shop.domain.orderDetail.dto.SellerOrderDetailResponseDTO;
import com.onshop.shop.domain.orderDetail.entity.OrderDetail;
import com.onshop.shop.domain.orderDetail.repository.OrderDetailRepository;
import com.onshop.shop.domain.seller.entity.Seller;
import com.onshop.shop.domain.seller.repository.SellerRepository;
import com.onshop.shop.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl implements OrderDetailService {
	private final OrderDetailRepository orderDetailRepository;
	private final OrderRepository orderRepository;
	private final CartRepository cartRepository;
	private final SellerRepository sellerRepository;
	
	
	// 주문 상세 저장
	@Override
	public void createOrderDetail(Long userId, OrderDTO orderDTO, Order order) {
		
		List<Cart> carts = cartRepository.findByUserId(userId);
		
		
		if(carts.isEmpty()) {
			throw new ResourceNotFoundException("조회할 장바구니 목록이 없습니다.");
		}
		
		
		// 저장되기 전 주문 상세 내역
		List<OrderDetail> unsavedOrderDetails = carts.stream().map((cart)->{
			
			
			return OrderDetail.builder()
					.price(cart.getProduct().getPrice())
					.product(cart.getProduct())
					.totalPrice(cart.getQuantity()*cart.getProduct().getPrice())
					.quantity(cart.getQuantity())
					.order(order)
					.build();
		}).toList();
		
		
		// 주문 상세내역 저장
		orderDetailRepository.saveAll(unsavedOrderDetails);
		
		
	}


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
	
	// 주문 번호, 판매자 별 주문 상세 내역 조회
	@Override
	public SellerOrderDetailResponseDTO getOrderDetailByOrderId(Long orderId, Long userId) {

		Seller seller = sellerRepository.findByUserId(userId).orElseThrow(()-> new ResourceNotFoundException("판매자만 가능합니다."));
		Long sellerId = seller.getSellerId();
		
		SellerOrderDetailResponseDTO orderDetail = orderDetailRepository.findOrderDetailsByOrderId(orderId, sellerId);
		if(orderDetail == null) {
			throw new ResourceNotFoundException("주문번호:"+orderId+"에 해당하는 주문상세 내역을 찾을 수 없습니다.");
		}
		return orderDetail;
	}
}

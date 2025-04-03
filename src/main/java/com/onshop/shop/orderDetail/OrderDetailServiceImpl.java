package com.onshop.shop.orderDetail;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onshop.shop.cart.Cart;
import com.onshop.shop.cart.CartRepository;
import com.onshop.shop.exception.ResourceNotFoundException;
import com.onshop.shop.order.Order;
import com.onshop.shop.order.OrderDTO;
import com.onshop.shop.order.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl implements OrderDetailService {
	
	
	private final OrderDetailRepository orderDetailRepository;
	private final OrderRepository orderRepository;
	private final CartRepository cartRepository;
	
	
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
	        throw new ResourceNotFoundException("주문 정보가 없습니다.");
	    }
	    List<ProductItemDTO> products = orderDetailRepository.findOrderProductsByOrderId(orderId);
	    dto.setProducts(products);
	    return dto;
	}



}

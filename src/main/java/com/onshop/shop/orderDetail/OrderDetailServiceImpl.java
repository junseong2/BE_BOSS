package com.onshop.shop.orderDetail;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onshop.shop.cart.Cart;
import com.onshop.shop.cart.CartRepository;
import com.onshop.shop.exception.ResourceNotFoundException;
import com.onshop.shop.order.Order;
import com.onshop.shop.order.OrderDTO;
import com.onshop.shop.product.Product;
import com.onshop.shop.product.ProductRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl implements OrderDetailService {
	
	
	private final OrderDetailRepository orderDetailRepository;
	private final CartRepository cartRepository;
	
	private final ProductRepository productRepository; 
	// 주문 상세 저장
	@Override
	public void createOrderDetail(Long userId, OrderDTO orderDTO, Order order) {
		
		List<Cart> carts = cartRepository.findByUserId(userId);
	

		
		if(carts.isEmpty()) {
			throw new ResourceNotFoundException("조회할 장바구니 목록이 없습니다.");
		}
		
		
		// 저장되기 전 주문 상세 내역
		List<OrderDetail> unsavedOrderDetails = carts.stream().map((cart)->{
		    Product product = cart.getProduct();
		    product.increaseSales(cart.getQuantity());
		    System.out.println("🟡 상품 ID: " + product.getProductId() + ", 가격: " + product.getPrice());
		    Integer unitPrice = product.getPrice();
			productRepository.save(product);
		    if (unitPrice == null) {
		        System.out.println("❌ 상품 가격이 null입니다! 상품 ID: " + product.getProductId());
		        unitPrice = 0; // 또는 throw new IllegalStateException(...) 하셔도 됩니다.
		    }
		    
			return OrderDetail.builder()
					.price(unitPrice)
					.product(cart.getProduct())
			        .totalPrice(unitPrice * cart.getQuantity()) // ✅ 여기!
					.quantity(cart.getQuantity())
					.order(order)
					.build();
		}).toList();
		
	
		// 주문 상세내역 저장
		orderDetailRepository.saveAll(unsavedOrderDetails);
		
		
	}


	// 주문 번호 별 주문 상세 내역 조회
	@Override
	public OrderDetailResponseDTO getOrderDetailByOrderId(Long orderId) {
		
		OrderDetailResponseDTO orderDetails = orderDetailRepository.findOrderDetailsByOrderId(orderId);
		if(orderDetails == null) {
			throw new ResourceNotFoundException("주문번호:"+orderId+"에 해당하는 주문상세 내역을 찾을 수 없습니다.");
		}
		return orderDetails;
	}
	
}

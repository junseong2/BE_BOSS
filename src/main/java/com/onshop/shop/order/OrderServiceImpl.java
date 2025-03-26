package com.onshop.shop.order;

import com.onshop.shop.exception.ResourceNotFoundException;
import com.onshop.shop.orderDetail.OrderDetailRepository;
import com.onshop.shop.user.User;
import com.onshop.shop.user.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public Order createOrder(OrderDTO orderDTO) {
        Long userId = orderDTO.getUserId();

        // ✅ 변환된 `userId` 사용
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + userId));

        Order order = Order.builder()
            .user(user)
            .totalPrice(orderDTO.getTotalPrice())
            .status(OrderStatus.PENDING)
            .createdDate(LocalDateTime.now())
            .build();

        return orderRepository.save(order);
    }


    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId: " + orderId));
    }

    /** 판매자 */
    // 주문 내역 조회
	@Override
	public SellerOrderResponseDTO getOrders(int page, int size, String search, String status) {
		// TODO: 실제 판매자 ID 로 대체 해야 함
		Long sellerId = 999L;
		Pageable pageable = (Pageable) PageRequest.of(page, size);
		
		
		
    	 // 주문 목록
		List<SellerOrderDTO> orders = orderDetailRepository.findOrderSummaryBySellerIdAndStatus(sellerId, search, status, pageable);

		if(orders.isEmpty()) {
			throw new ResourceNotFoundException("조회할 주문목록이 없습니다.");
		}
		
		Long totalCount = orderRepository.countOrdersBySeller(sellerId); // 전체 주문 목록 수

		return SellerOrderResponseDTO.builder()
				.orders(orders)
				.totalCount(totalCount)
				.build();
	}


}

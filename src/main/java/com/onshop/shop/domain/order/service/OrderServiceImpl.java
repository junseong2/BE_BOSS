package com.onshop.shop.domain.order.service;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onshop.shop.domain.order.dto.OrderDTO;
import com.onshop.shop.domain.order.dto.OrderResponseDTO;
import com.onshop.shop.domain.order.dto.SellerOrderDTO;
import com.onshop.shop.domain.order.dto.SellerOrderResponseDTO;
import com.onshop.shop.domain.order.entity.Order;
import com.onshop.shop.domain.order.enums.OrderStatus;
import com.onshop.shop.domain.order.repository.OrderRepository;
import com.onshop.shop.domain.orderDetail.repository.OrderDetailRepository;
import com.onshop.shop.domain.orderDetail.service.OrderDetailService;
import com.onshop.shop.domain.seller.entity.Seller;
import com.onshop.shop.domain.seller.repository.SellerRepository;
import com.onshop.shop.domain.user.dto.UserResponseDTO;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.domain.user.repository.UserRepository;
import com.onshop.shop.global.exception.NotAuthException;
import com.onshop.shop.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderDetailService orderDetailService;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;


    @Override
    @Transactional
    public Order createOrder(OrderDTO orderDTO) {
        Long userId = orderDTO.getUserId();

        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + userId));

        Order order = Order.builder()
            .user(user)
            .totalPrice(orderDTO.getTotalPrice())
            .status(OrderStatus.PENDING)
            .createdDate(LocalDateTime.now())
            .build();

        Order savedOrder = orderRepository.save(order);

        // ✅ 주문 상세 저장도 꼭 호출!
        orderDetailService.createOrderDetail(userId, orderDTO, savedOrder);

        return savedOrder;
    }



    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId: " + orderId));
    }



    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findOrdersByUserId(userId);
        return orders.stream().map(order -> {
            OrderResponseDTO dto = new OrderResponseDTO();
            dto.setOrderId(order.getOrderId());
            dto.setTotalPrice(order.getTotalPrice());
            dto.setStatus(order.getStatus().name());
            dto.setCreatedDate(order.getCreatedDate());

            // User 정보를 별도의 DTO로 변환
            User user = order.getUser();
            UserResponseDTO userDto = new UserResponseDTO();
            userDto.setUserId(user.getUserId());
            userDto.setUsername(user.getUsername());
            userDto.setEmail(user.getEmail());
            // 필요시 추가 필드 처리

            dto.setUser(userDto);
            return dto;
        }).toList();
    }

    /** 판매자 */
    // 주문 내역 조회
	@Override
	public SellerOrderResponseDTO getOrders(int page, int size, String search, String orderStatus, String paymentStatus,Long userId) {

		Pageable pageable = (Pageable) PageRequest.of(page, size);
		Seller seller = sellerRepository.findByUserId(userId).orElseThrow(()->new NotAuthException("요청 권한이 없습니다."));

		
    	 // 주문 목록
		List<SellerOrderDTO> orders = orderDetailRepository.findOrderSummaryBySellerIdAndStatus(
				seller.getSellerId(), 
				search, 
				orderStatus, 
				paymentStatus , 
				pageable);

		if(orders.isEmpty()) {
			throw new ResourceNotFoundException("조회할 주문목록이 없습니다.");
		}
		
		Long totalCount = orderRepository.countOrdersBySeller(seller.getSellerId()); // 전체 주문 목록 수

		return SellerOrderResponseDTO.builder()
				.orders(orders)
				.totalCount(totalCount)
				.build();
	}
}

package com.onshop.shop.domain.order.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onshop.shop.domain.order.dto.OrderDTO;
import com.onshop.shop.domain.order.dto.OrderResponseDTO;
import com.onshop.shop.domain.order.entity.Order;
import com.onshop.shop.domain.order.enums.OrderStatus;
import com.onshop.shop.domain.order.repository.OrderRepository;
import com.onshop.shop.domain.orderDetail.service.OrderDetailService;
import com.onshop.shop.domain.user.dto.UserResponseDTO;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 구매자 주문 서비스 구현체
 * - 주문 생성, 조회 기능 제공
 */
@Service
@RequiredArgsConstructor
public class OrderBuyerServiceImpl implements OrderBuyerService {

    private final OrderRepository orderRepository;
    private final OrderDetailService orderDetailService;
    private final UserRepository userRepository;

    /**
     * 주문 생성
     *
     * @param orderDTO 주문 생성에 필요한 정보 (userId, totalPrice 등)
     * @return 생성된 주문 엔티티
     * @throws IllegalArgumentException 사용자 정보를 찾을 수 없는 경우
     */
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

        // 주문 상세 정보 저장
        orderDetailService.createOrderDetail(userId, orderDTO, savedOrder);

        return savedOrder;
    }

    /**
     * 주문 ID로 단일 주문 조회
     *
     * @param orderId 조회할 주문 ID
     * @return 조회된 주문 엔티티
     * @throws IllegalArgumentException 주문이 존재하지 않는 경우
     */
    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId: " + orderId));
    }

    /**
     * 특정 사용자 ID로 주문 목록 조회
     *
     * @param userId 사용자 ID
     * @return 사용자에 해당하는 주문 목록 DTO
     */
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

            // 사용자 정보 포함
            User user = order.getUser();
            UserResponseDTO userDto = new UserResponseDTO();
            userDto.setUserId(user.getUserId());
            userDto.setUsername(user.getUsername());
            userDto.setEmail(user.getEmail());

            dto.setUser(userDto);
            return dto;
        }).toList();
    }
}

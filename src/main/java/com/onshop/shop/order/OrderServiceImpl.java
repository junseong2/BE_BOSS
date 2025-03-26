package com.onshop.shop.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onshop.shop.user.User;
import com.onshop.shop.user.UserRepository;
import com.onshop.shop.user.UserResponseDTO;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

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


}

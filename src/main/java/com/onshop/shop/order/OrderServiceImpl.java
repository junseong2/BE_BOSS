package com.onshop.shop.order;

import com.onshop.shop.user.User;
import com.onshop.shop.user.UserRepository;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // ✅ `Long` → `Integer` 변환
        Integer userId = orderDTO.getUserId().intValue();

        // ✅ 변환된 `userId` 사용
        User user = userRepository.findById(userId)
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


}

package com.onshop.shop.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onshop.shop.user.User;
import com.onshop.shop.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public Order createOrder(Long userId, int totalPrice) {
    	User user = userRepository.findById(userId.intValue()) // 🔹 Long → Integer 변환
    	        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));



        Order order = Order.builder()
                .user(user)
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING)
                .createdDate(LocalDateTime.now())
                .build();

        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
    }

    @Override
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getUser().getUserId().equals(userId))
                .toList();
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        order.setStatus(status);
        orderRepository.save(order);
    }
}

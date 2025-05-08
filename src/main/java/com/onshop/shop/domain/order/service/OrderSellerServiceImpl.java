package com.onshop.shop.domain.order.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.onshop.shop.domain.order.dto.SellerOrderDTO;
import com.onshop.shop.domain.order.dto.SellerOrderResponseDTO;
import com.onshop.shop.domain.order.repository.OrderRepository;
import com.onshop.shop.domain.orderDetail.repository.OrderDetailRepository;
import com.onshop.shop.domain.seller.entity.Seller;
import com.onshop.shop.domain.seller.repository.SellerRepository;
import com.onshop.shop.global.exception.NotAuthException;
import com.onshop.shop.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * 판매자 주문 서비스 구현체
 * - 판매자용 주문 조회 기능 제공
 */
@Service
@RequiredArgsConstructor
public class OrderSellerServiceImpl implements OrderSellerService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SellerRepository sellerRepository;

    /**
     * 판매자 주문 목록 조회
     *
     * @param page           페이지 번호 (0부터 시작)
     * @param size           페이지당 항목 수
     * @param search         상품명/주문자명 등 검색 키워드
     * @param orderStatus    주문 상태 필터 (예: PENDING, SHIPPED 등)
     * @param paymentStatus  결제 상태 필터 (예: PAID, UNPAID 등)
     * @param userId         요청한 사용자 ID (판매자 인증용)
     * @return 판매자 주문 목록 및 총 개수를 포함한 응답 DTO
     * @throws NotAuthException          판매자 정보가 없거나 권한이 없는 경우
     * @throws ResourceNotFoundException 조건에 해당하는 주문이 존재하지 않는 경우
     */
    @Override
    public SellerOrderResponseDTO getOrders(int page, int size, String search, String orderStatus, String paymentStatus, Long userId) {
        Pageable pageable = PageRequest.of(page, size);

        // 판매자 인증
        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new NotAuthException("요청 권한이 없습니다."));

        // 주문 목록 검색
        List<SellerOrderDTO> orders = orderDetailRepository.findOrderSummaryBySellerIdAndStatus(
            seller.getSellerId(),
            search,
            orderStatus,
            paymentStatus,
            pageable
        );

        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("조회할 주문목록이 없습니다.");
        }

        // 전체 주문 수
        Long totalCount = orderRepository.countOrdersBySeller(seller.getSellerId());

        return SellerOrderResponseDTO.builder()
            .orders(orders)
            .totalCount(totalCount)
            .build();
    }
}

package com.onshop.shop.domain.inventory.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.onshop.shop.domain.inventory.dto.InventoryOrderRequestDTO;
import com.onshop.shop.domain.inventory.dto.SellerInventoryDTO;
import com.onshop.shop.domain.inventory.dto.SellerInventoryResponseDTO;
import com.onshop.shop.domain.inventory.entity.Inventory;
import com.onshop.shop.domain.inventory.repository.InventoryRepository;
import com.onshop.shop.domain.seller.entity.Seller;
import com.onshop.shop.domain.seller.repository.SellerRepository;
import com.onshop.shop.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 판매자 재고 서비스 구현체.
 * 판매자의 재고 목록 조회 및 재고 업데이트 기능을 제공한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final SellerRepository sellerRepository;

    /**
     * 판매자의 재고 목록을 페이징 및 상태 조건(state)에 따라 필터링하여 조회한다.
     *
     * @param page     현재 페이지 번호 (0부터 시작)
     * @param size     페이지 당 항목 수
     * @param search   검색 키워드 (상품명 등)
     * @param state    재고 상태 필터 ("all", "soldout", "warn" 중 하나)
     * @param userId   요청한 사용자 ID (판매자 식별용)
     * @return         필터링된 재고 목록과 전체 개수를 포함한 DTO
     * @throws ResourceNotFoundException 판매자 계정이 없거나 재고가 없는 경우
     */
    @Override
    public SellerInventoryResponseDTO getAllInventory(int page, int size, String search, String state, Long userId) {
        Pageable pageable = PageRequest.of(page, size);

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 유저의 판매자 계정을 찾을 수 없습니다."));

        Long sellerId = seller.getSellerId();

        List<SellerInventoryDTO> inventories = inventoryRepository.findAllBySellerIdAndSearch(sellerId, search, state, pageable).toList();
        Long totalCount = inventoryRepository.countBySellerIdAndSearch(sellerId, search, state);

        if (inventories.isEmpty()) {
            throw new ResourceNotFoundException("해당 재고 목록은 존재하지 않습니다.");
        }

        return SellerInventoryResponseDTO.builder()
            .inventories(inventories)
            .totalCount(totalCount)
            .build();
    }

    /**
     * 판매자의 재고 정보를 일괄 업데이트한다.
     * 재고 수량 및 최소 재고 임계치를 수정한다.
     *
     * @param orderRequestDTOs 업데이트할 재고 목록 요청 DTO들
     * @param userId           요청한 사용자 ID (권한 검증용)
     * @throws ResourceNotFoundException 존재하지 않는 상품 ID가 포함된 경우
     */
    @Override
    public void updateInventory(List<InventoryOrderRequestDTO> orderRequestDTOs, Long userId) {
        // 요청된 상품 ID 목록 추출
        List<Long> productIds = orderRequestDTOs.stream()
            .map(InventoryOrderRequestDTO::getProductId)
            .toList();

        log.info("productIds: {}", productIds);

        // 기존 재고 정보 조회
        List<Inventory> prevInventories = inventoryRepository.findAllByProductIds(productIds);
        if (prevInventories.isEmpty()) {
            throw new ResourceNotFoundException("누락된 상품 목록이 존재하여 요청을 취소하였습니다.");
        }

        log.info("이전 인벤토리: {}", prevInventories);

        // 요청 데이터 기반으로 재고 업데이트
        List<Inventory> updatedInventories = orderRequestDTOs.stream().map(orderRequestDTO -> {
            Long productId = orderRequestDTO.getProductId();
            Long currentStock = orderRequestDTO.getStock();
            Long minStock = orderRequestDTO.getMinStock();

            Inventory inventory = prevInventories.stream()
                .filter(inven -> inven.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("상품 ID " + productId + "에 해당하는 품목을 찾을 수 없습니다."));

            inventory.setStock(currentStock);
            inventory.setMinStock(minStock);
            return inventory;
        }).toList();

        log.info("수정된 인벤토리: {}", updatedInventories);

        inventoryRepository.saveAll(updatedInventories);
    }
}

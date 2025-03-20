package com.onshop.shop.inventory;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.onshop.shop.exception.OverStockException;
import com.onshop.shop.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {
	
	private final InventoryRepository inventoryRepository;

	
	// 판매자(점주) 재고 조회
	@Override
	public List<SellerInventoryResponseDTO> getAllInventory(int page, int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		
		//TODO: 실제 인증된 판매자 ID 를 기반으로 인증되어야 함
		Long sellerId = 1L;
		
		List<SellerInventoryResponseDTO> inventories = inventoryRepository.findAllBySellerId(sellerId, pageable).toList();
		
		if(inventories.isEmpty() || inventories == null) {
			throw new ResourceNotFoundException("해당 재고 목록은 존재하지 않습니다.");
		}
		
		return inventories;
	}


	// 재고 업데이트
	@Override
	public void updateInventory(List<InventoryOrderRequestDTO> orderRequestDTOs) {
		
		
		// 상품 ID 목록화
		List<Long> productIds =  orderRequestDTOs.stream().map(dto -> {return 
			dto.getProductId();
		}).toList();
		
		log.info("productIds: {}", productIds);
		
		
		// 각 목록에 해당하는 모든 재고목록 조회
		List<Inventory> prevInventories = inventoryRepository.findAllByProductIds(productIds);
		
		if(prevInventories.isEmpty() || prevInventories == null) {
			throw new ResourceNotFoundException("누락된 상품 목록이 존재하여 요청을 취소하였습니다.");
		}
		
		log.info("이전 인벤토리:{}", prevInventories);
		
		
		// orderRequestDTOs의 각 아이템에 대해 재고 업데이트
		List<Inventory> updatedInventories = orderRequestDTOs.stream().map(orderRequestDTO -> {
		    Long productId = orderRequestDTO.getProductId();  
		    Long currentStock = orderRequestDTO.getStock();  // 현재재고
		    Long minStock = orderRequestDTO.getMinStock();  // 최소재고
		    
		    
		    // 해당 상품에 대한 Inventory 조회
		    Inventory inventory = prevInventories.stream()
		            .filter(inven -> inven.getProduct().getProductId().equals(productId))
		            .findFirst()
		            .orElseThrow(() -> new ResourceNotFoundException("상품 ID " + productId + "에 해당하는 품목을 찾을 수 없습니다."));


		    // 재고 업데이트
		    inventory.setStock(currentStock);
		    inventory.setMinStock(minStock);

		    return inventory;
		    
		}).toList();
		
		log.info("수정된 인벤토리:{}", updatedInventories);
		
		
		inventoryRepository.saveAll(updatedInventories);
		
	}


	// 재고 상품 검색
	@Override
	public List<SellerInventoryResponseDTO> searchInventories(String search, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		
		//TODO: 실제 인증된 판매자 ID 를 기반으로 인증되어야 함
		Long sellerId = 1L;
		
		List<SellerInventoryResponseDTO> inventories = inventoryRepository.findAllByNameAndSellerId(search, sellerId, pageable).toList();
		
		if(inventories.isEmpty() || inventories == null) {
			throw new ResourceNotFoundException("해당 재고 목록은 존재하지 않습니다.");
		}
		
		return inventories;
	}
}

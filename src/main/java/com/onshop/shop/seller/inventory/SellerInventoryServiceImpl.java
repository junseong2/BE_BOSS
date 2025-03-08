package com.onshop.shop.seller.inventory;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.onshop.shop.exception.ResourceNotFoundException;
import com.onshop.shop.inventory.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerInventoryServiceImpl implements SellerInventoryService {
	
	private final InventoryRepository inventoryRepository;

	@Override
	public List<SellerInventoryResponseDTO> getAllInventory(int page, int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		
		//TODO: 실제 인증된 판매자 ID 를 기반으로 인증되어야 함
		Long sellerId = 1L;
		
		List<SellerInventoryResponseDTO> inventories = inventoryRepository.findBySellerId(sellerId, pageable).toList();
		
		if(inventories.isEmpty() || inventories == null) {
			throw new ResourceNotFoundException("해당 재고 목록은 존재하지 않습니다.");
		}
		
		return inventories;
	}

}

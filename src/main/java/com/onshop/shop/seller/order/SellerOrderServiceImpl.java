package com.onshop.shop.seller.order;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onshop.shop.seller.inventory.InventoryOrderRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerOrderServiceImpl implements SellerOrderService {
	
	
	
	@Override
	public void createOrder(List<InventoryOrderRequestDTO> orderDTOs) {
		
		
	
	}

}

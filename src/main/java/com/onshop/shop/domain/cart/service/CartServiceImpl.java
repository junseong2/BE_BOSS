package com.onshop.shop.domain.cart.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.onshop.shop.domain.cart.dto.CartDTO;
import com.onshop.shop.domain.cart.dto.CartItemRequestDTO;
import com.onshop.shop.domain.cart.dto.CartTotalCountResponseDTO;
import com.onshop.shop.domain.cart.entity.Cart;
import com.onshop.shop.domain.cart.repository.CartRepository;
import com.onshop.shop.domain.product.entity.Product;
import com.onshop.shop.domain.product.repository.ProductRepository;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.domain.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

	private final CartRepository cartRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;


	// 사용자 ID로 장바구니 아이템 조회
	@Transactional
	public List<CartDTO> getCartByUserId(Long userId) {
		List<Cart> carts = cartRepository.findByUserId(userId);

		return carts.stream().map(cart -> {
			Product product = cart.getProduct();
			return new CartDTO(cart.getCartId(),cart.getQuantity(),cart.getUserId(), cart.getProduct().getProductId(),  
					product.getName(), // productName 추가
					product.getPrice(), // productPrice 추가
					product.getImageList().get(0) // product 썸네일 추가
			);
		}).collect(Collectors.toList());
	}

	// 장바구니에 상품 추가
	public Cart addItemToCart(Long userId, CartItemRequestDTO request) {
		Long productId =request.getProductId();
		Product product =  productRepository.findById(productId).orElse(null);
		Cart cart = new Cart();
		cart.setUserId(userId);
		cart.setProduct(product);
		cart.setQuantity(request.getQuantity());
		return cartRepository.save(cart);
	}

	// CartService에서 상품 수량 갱신 처리
	public boolean updateItemQuantity(Long userId, Long productId, Integer quantity) {
	    Cart cart = cartRepository.findByUserIdAndProduct(userId,productRepository.findById(productId).orElse(null) );

	    if (cart == null) {
	        return false;  // 해당 상품이 장바구니에 없으면 실패
	    }

	    cart.setQuantity(quantity);  // 수량 업데이트
	    cartRepository.save(cart);   // 갱신된 장바구니 저장

	    return true;
	}

	
	
	
	// cartId로 아이템 제거
	public boolean removeItemFromCart(Long cartId) {
		if (cartRepository.existsById(cartId)) {
			cartRepository.deleteById(cartId);
			return true;
		}
		return false;
	}
	
	
	
	 public boolean removeItemFromCartbyProductId(Long userId, Long productId) {
	        List<Cart> carts = cartRepository.findByUserId(userId);
	        Cart cartToRemove = null;

	        for (Cart cart : carts) {
	            if (cart.getProduct().getProductId().equals(productId)) {
	                cartToRemove = cart;
	                break;
	            }
	        }

	        if (cartToRemove != null) {
	            cartRepository.delete(cartToRemove);
	            return true;
	        }
	        return false;
	    }

	 
	 
	// 장바구니 비우기
	public void clearCart(Long userId) {
		List<Cart> carts = cartRepository.findByUserId(userId);
		if (!carts.isEmpty()) {
			cartRepository.deleteAll(carts);
		}
	}

	// 유저별 장바구니 아이템 개수
	@Override
	public CartTotalCountResponseDTO totalCountByUserId(Long userId) {
		
		User user = userRepository.findById(userId).orElse(null);
		Long itemsCount =0L;
		
		if(user ==null) {
			itemsCount = 0L;
		} else {
			itemsCount = cartRepository.countByUserId(userId);	
		}
		
		
		return CartTotalCountResponseDTO.builder()
				.totalCount(itemsCount)
				.build() ;
	}

}

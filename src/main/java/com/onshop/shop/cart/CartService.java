package com.onshop.shop.cart;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.onshop.shop.product.Product;
import com.onshop.shop.product.ProductRepository;
import com.onshop.shop.user.User;

@Service
public class CartService {

	private final CartRepository cartRepository;
	private final ProductRepository productRepository;

	 public CartService(CartRepository cartRepository, ProductRepository productRepository) {
	        this.cartRepository = cartRepository;
	        this.productRepository = productRepository;
	    }

	// 사용자 ID로 장바구니 아이템 조회
	public List<CartDTO> getCartByUserId(Long userId) {
		List<Cart> carts = cartRepository.findByUserId(userId);

		return carts.stream().map(cart -> {
			Product product = cart.getProduct();
			return new CartDTO(cart.getCartId(),cart.getQuantity(),cart.getUserId(), cart.getProduct().getProductId(),  
					product.getName(), // ✅ productName 추가
					product.getPrice() // ✅ productPrice 추가
			);
		}).collect(Collectors.toList());
	}

	// 장바구니에 상품 추가
	public Cart addItemToCart(Long userId, CartItemRequest request) {
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
	
	
	

}

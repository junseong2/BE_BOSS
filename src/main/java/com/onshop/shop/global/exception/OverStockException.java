package com.onshop.shop.global.exception;

public class OverStockException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OverStockException(String message) {
        super(message);
    }
}

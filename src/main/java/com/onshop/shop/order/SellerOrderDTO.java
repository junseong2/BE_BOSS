package com.onshop.shop.order;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  
public class SellerOrderDTO {
	private Long orderId;
	private Object username;
	private Object createdDate;
	private Long orderCount;
	private Object paymentMethod;
	private int totalPrice;
	private Object status;
	
    public SellerOrderDTO(Long orderId, Object username, Object createdDate, Long orderCount, Object paymentMethod, int totalPrice, Object status) {
        this.orderId = orderId;
        this.username = username;
        this.createdDate = createdDate;
        this.orderCount = orderCount;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
        this.status = status;
    }

}

package com.onshop.shop.order;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  
public class SellerOrderDTO {
	private Long orderId;
	private String username;
	private Object createdDate;
	private Long orderCount;
	private Object paymentMethod;
	private int totalPrice;
	private Object paymentStatus;
	private Object status;
	
    public SellerOrderDTO(Long orderId, String username, Object createdDate, Long orderCount, Object paymentMethod, int totalPrice,Object paymentStatus ,Object status) {
        this.orderId = orderId;
        this.username = username;
        this.createdDate = createdDate;
        this.orderCount = orderCount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.totalPrice = totalPrice;
        
        this.status = status;
    }

}

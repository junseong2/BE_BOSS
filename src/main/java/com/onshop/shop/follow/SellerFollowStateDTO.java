package com.onshop.shop.follow;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerFollowStateDTO {
	private Long followId;
	private Boolean isFollow;

}

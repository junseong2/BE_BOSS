package com.onshop.shop.product;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerProductIdsDTO {
	@NotNull(message = "상품 ID 선택은 필수입니다.")
	private List<Long> ids;
}

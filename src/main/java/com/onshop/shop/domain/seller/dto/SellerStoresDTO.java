
package com.onshop.shop.domain.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerStoresDTO {
   
   private String storeName;
   private String description;
   private String logoUrl;
   private int sellerId;

   

}
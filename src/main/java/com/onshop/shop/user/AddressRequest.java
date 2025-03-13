package com.onshop.shop.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor  // 🔥 기본 생성자 추가 (Jackson 매핑 오류 방지)
public class AddressRequest {
    
    @JsonProperty("address1")  // 🔥 JSON 필드명과 정확히 매칭
    private String address1;

    @JsonProperty("address2")
    private String address2;

    @JsonProperty("post")
    private String post;

    @JsonProperty("isDefault")
    private Boolean isDefault;

    @Override
    public String toString() {
        return "AddressRequest(address1=" + address1 + ", address2=" + address2 +
               ", post=" + post + ", isDefault=" + isDefault + ")";
    }
}

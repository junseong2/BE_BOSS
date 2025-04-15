package com.onshop.shop.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    KAKAOPAY("kakaopay"),
    TOSS_PAY("tosspay"),
    TOTALPAY("totalpay"),
    PAYCOPAY("paycopay");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PaymentMethod fromString(String value) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.value.equalsIgnoreCase(value)) {
                return method;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 결제 방법: " + value);
    }
}

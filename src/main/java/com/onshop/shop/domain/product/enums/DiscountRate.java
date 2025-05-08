package com.onshop.shop.domain.product.enums;

public enum DiscountRate {
	RATE_0(0),
    RATE_2(2),
    RATE_3(3),
    RATE_5(5),
    RATE_10(10),
    RATE_15(15),
    RATE_20(20),
    RATE_25(25),
    RATE_30(30);

    private final int rate;

    DiscountRate(int rate) {
        this.rate = rate;
    }

    public int getRate() {
        return rate;
    }

    public static DiscountRate fromRate(int rate) {
        for (DiscountRate discountRate : values()) {
            if (discountRate.rate == rate) {
                return discountRate;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 할인율입니다: " + rate);
    }
}

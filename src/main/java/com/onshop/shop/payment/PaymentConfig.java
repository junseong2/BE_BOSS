package com.onshop.shop.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class PaymentConfig {

    @Value("${portone.api-key}")
    private String portOneApiKey;

    @Value("${portone.secret-key}")
    private String portOneSecretKey;

    @Value("${portone.api-url}")
    private String portOneApiUrl;

    @Value("${payment.channel.kakaopay}")
    private String kakaopayChannel;

    @Value("${payment.channel.tosspay}")
    private String tosspayChannel;

    @Value("${payment.channel.totalpay}")
    private String totalpayChannel;

    @Value("${payment.channel.paycopay}")
    private String paycopayChannel;

    public String getChannelKey(String paymentMethod) {
        switch (paymentMethod.toLowerCase()) {
            case "kakaopay": return kakaopayChannel;
            case "tosspay": return tosspayChannel;
            case "totalpay": return totalpayChannel;
            case "paycopay": return paycopayChannel;
            default: throw new IllegalArgumentException("유효하지 않은 결제 방법: " + paymentMethod);
        }
    }
}

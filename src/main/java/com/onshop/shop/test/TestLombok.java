package com.onshop.shop.test;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestLombok {
    private String message;
    
    public static void main(String[] args) {
        TestLombok test = new TestLombok();
        test.setMessage("Hello Lombok!");
        System.out.println(test.getMessage());  // 정상 출력되면 Lombok 적용 완료
    }
}

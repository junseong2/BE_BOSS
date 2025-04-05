package com.onshop.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class DotEnvConfig {

    @Bean
    public Dotenv dotenv() {
        // .env 파일을 읽어서 환경변수로 사용
        return Dotenv.configure().directory("./")
                .ignoreIfMissing() // .env 파일이 없어도 에러 발생 안함
                .load();
    }
}
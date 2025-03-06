package com.onshop.shop.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private final String SECRET_KEY = "your_secret_key_your_secret_key"; // 🔹 최소 32바이트 이상 필요
    private final long EXPIRATION_TIME = 86400000; // 1일

    // 🔹 SECRET_KEY를 HMAC-SHA 키로 변환
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ✅ JWT 생성 메서드
    public String generateToken(Long userId) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512);
        return builder.compact();
    }

    // ✅ JWT 검증 메서드 수정
    public Long validateToken(String token) {
        Claims claims = Jwts.parser() // 🔹 parser() → parserBuilder() 사용
                .setSigningKey(getSigningKey()) // 🔹 Key 객체 사용
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }
}

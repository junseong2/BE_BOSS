package com.onshop.shop.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String SECRET_KEY = "bossbossbossbossbossbossbossboss"; // ✅ 최소 32바이트 이상

    private static final long EXPIRATION_TIME = 360000000; // ✅ 1시간 (밀리초)


    // 🔹 SECRET_KEY를 HMAC-SHA 키로 변환
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes()); // Base64 디코딩 없이 사용
    }
    
    
 // ✅ JWT에서 userId 추출
    public Long extractUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    // ✅ JWT 생성 메서드
    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // userId 저장
                .setIssuedAt(new Date()) // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 만료 시간 설정
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // ✅ 최신 방식 적용
                .compact();
    }

    // ✅ JWT 검증 메서드 (로그 + 예외 던지기)
    public boolean validateToken(String token) {
        try {
            getClaims(token); // ✅ 토큰이 유효한 경우 예외 발생 안 함
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("🔴  재훈에게 문의JWT 토큰이 만료됨: {}", e.getMessage());
            sendSlackAlert("🚨 JWT 토큰 만료: " + e.getMessage());
            throw new JwtException("JWT 토큰이 만료되었습니다.");
        } catch (UnsupportedJwtException e) {
            logger.warn("⚠️ 지원되지 않는 JWT 토큰 재훈에게 문의: {}", e.getMessage());
            sendSlackAlert("🚨 지원되지 않는 JWT 토큰: " + e.getMessage());
            throw new JwtException("지원되지 않는 JWT 토큰입니다.");
        } catch (MalformedJwtException e) {
            logger.warn("⚠️ 재훈에게 문의 잘못된 JWT 토큰: {}", e.getMessage());
            sendSlackAlert("🚨 잘못된 JWT 토큰: " + e.getMessage());
            throw new JwtException("잘못된 JWT 토큰입니다.");
        } catch (SignatureException e) {
            logger.warn("⚠️ 재훈에게 문의 JWT 서명 검증 실패: {}", e.getMessage());
            sendSlackAlert("🚨 JWT 서명 검증 실패: " + e.getMessage());
            throw new JwtException("JWT 서명 검증에 실패했습니다.");
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ 재훈에게 문의 JWT 토큰이 제공되지 않음: {}", e.getMessage());
            sendSlackAlert("🚨 JWT 토큰이 제공되지 않음: " + e.getMessage());
            throw new JwtException("JWT 토큰이 필요합니다.");
        }
    }

    // ✅ Claims 추출 메서드
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ Slack 알림 보내기 (선택 사항)
    private void sendSlackAlert(String message) {
        String webhookUrl = "https://hooks.slack.com/services/XXXX/YYYY/ZZZZ"; // 🔥 Slack Webhook URL 입력 필요
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> payload = Map.of("text", message);
        restTemplate.postForObject(webhookUrl, payload, String.class);
    }
}
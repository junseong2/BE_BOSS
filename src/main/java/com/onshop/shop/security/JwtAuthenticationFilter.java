package com.onshop.shop.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String token = null;

        // ✅ 1. 쿠키에서 JWT 찾기
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    System.out.println("🔍 JWT 쿠키 발견: " + token);
                    break;
                }
            }
        }

        // ✅ 2. Authorization 헤더에서도 찾기
        if (token == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                token = header.replace("Bearer ", "");
                System.out.println("🔍 Authorization 헤더에서 JWT 발견: " + token);
            }
        }

        // ✅ 3. 토큰 없으면 필터 통과
        if (token == null) {
            System.out.println("❌ JWT 토큰 없음");
            chain.doFilter(request, response);
            return;
        }

        // ✅ 4. JWT 검증 및 인증 처리
        try {
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.extractUserId(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("✅ JWT 인증 성공 - userId: " + userId);
            } else {
                System.out.println("❌ JWT 검증 실패 - 유효하지 않은 토큰");
            }
        } catch (Exception e) {
            // ✅ 인증 실패해도 요청은 그대로 진행
            System.out.println("⚠️ JWT 인증 예외 발생: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        // ✅ 필터 계속 진행
        chain.doFilter(request, response);
    }
}

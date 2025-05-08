package com.onshop.shop.global.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.onshop.shop.global.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.equals("/") || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = null;

        // ✅ 1. 먼저 쿠키에서 JWT를 찾음
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    System.out.println("🔍 JWT 쿠키 발견: " + token);
                    break;
                }
            }
        }

        // ✅ 2. Authorization 헤더에서 JWT를 찾음 (쿠키가 없을 경우)
        else if (token == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                token = header.replace("Bearer ", "");
                System.out.println("🔍 Authorization 헤더에서 JWT 발견: " + token);
            }
        }

        // ✅ 3. 토큰이 없는 경우
        if (token == null) {
            System.out.println("❌ JWT 토큰 없음");
            chain.doFilter(request, response);
            return;
        }

        // ✅ 4. JWT 검증 수행
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
            System.out.println("JWT Exception 아이디: " + e.getMessage());
        }

        chain.doFilter(request, response);
    }

}

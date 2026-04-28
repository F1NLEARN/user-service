package com.finlearn.userservice.infrastructure.jwt;

import com.finlearn.userservice.infrastructure.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** JWT를 검증하고 SecurityContext에 인증 정보를 저장하는 필터. */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = jwtProvider.resolveToken(request.getHeader(JwtProvider.AUTHORIZATION_HEADER));

        if (token != null) {
            Claims claims = jwtProvider.getClaims(token);
            CustomUserDetails userDetails = new CustomUserDetails(
                    jwtProvider.getUserId(claims),
                    jwtProvider.getEmail(claims)
            );
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
            );
        }

        filterChain.doFilter(request, response);
    }

    // login, reissue, signup은 토큰 없이 통과.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/v1/users/auth/login") ||
               path.equals("/api/v1/users/auth/reissue") ||
               path.equals("/api/v1/users/signup");
    }
}

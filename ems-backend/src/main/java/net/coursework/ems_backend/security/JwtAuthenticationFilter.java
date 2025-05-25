package net.coursework.ems_backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret:Xdks8h1aF2Ieqf9Nc9jztnKZoU9rSbmUpxkBtiQgLlVYFuydq1S4uDgF9ikR6dvw}")
    private String JWT_SECRET;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        System.out.println("Processing request: " + path + " " + request.getMethod());

        if (isPublicPath(path)) {
            System.out.println("Skipping JWT check for public path: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No Authorization header or not Bearer token for path: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            System.out.println("Validating JWT token");

            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(JWT_SECRET.getBytes()))
                    .build()
                    .verify(token);

            String username = decodedJWT.getSubject();
            String role = decodedJWT.getClaim("role").asString();

            System.out.println("JWT valid for user: " + username + " with role: " + role);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    role != null ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)) : Collections.emptyList()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("Authentication set in SecurityContext for user: " + username);

        } catch (Exception e) {
            System.err.println("JWT validation failed: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.equals("/login") ||
                path.equals("/register") ||
                path.equals("/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/");
    }
}
package net.coursework.ems_backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {

    private final String JWT_SECRET = "Xdks8h1aF2Ieqf9Nc9jztnKZoU9rSbmUpxkBtiQgLlVYFuydq1S4uDgF9ikR6dvw";
    private final long JWT_EXPIRATION_TIME = 86400000;

    public String generateToken(String username, String role) {
        return JWT.create()
                .withSubject(username)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(JWT_SECRET.getBytes()));
    }

}

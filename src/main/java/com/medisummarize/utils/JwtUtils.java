package com.medisummarize.utils;

import com.medisummarize.model.User;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    private static String SECRET;
    private static long EXPIRATION_TIME;

    @PostConstruct
    public void init() {
        SECRET = secret;
        EXPIRATION_TIME = expiration;
    }

    public static boolean validateToken(String token) {
        return parseToken(token).isPresent() && !parseToken(token).get().getExpiration().before(new Date());
    }

    public static Key getSigningKey() {
        byte[] keyBytes= Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static Optional<Claims> parseToken (String token) {
        return Optional.ofNullable(Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody());
    }

    public static Optional<String> getRoleFromToken(String token) {
        return parseToken(token).map(claims -> (String) claims.get("role"));
    }

    public static Optional<String> getEmailFromToken(String token) {
        return parseToken(token).map(Claims::getSubject);
    }

    public static String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuer("MediSummarize")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}

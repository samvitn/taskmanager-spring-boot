package com.samvit.taskmanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // Reading from application.properties - refraining from hardcoding secrets
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    // Convert the secret string into a cryptographic HMAC SHA key
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ===== GENERATE a new token =====
    // Called after successful login or registration
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)                                    // WHO this token belongs to
                .issuedAt(new Date())                              // WHEN it was created
                .expiration(new Date(System.currentTimeMillis() + expiration))  // WHEN it expires
                .signWith(getSigningKey())                         // SIGN with our secret key
                .compact();                                        // BUILD the token string
    }

    // ===== EXTRACT email from token =====
    // Called by the filter to identify who is making the request
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ===== VALIDATE the token =====
    // Checks: is the signature valid? Has it expired?
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);  // this throws if signature is bad or token expired
            return true;
        } catch (Exception e) {
            return false;  // any problem = invalid token
        }
    }

    // ===== INTERNAL: parse all claims from the token =====
    // Claims = the data inside the token (subject, issuedAt, expiration, etc.)
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())     // verify signature with our key
                .build()
                .parseSignedClaims(token)        // parse and validate
                .getPayload();                   // get the data
    }
}
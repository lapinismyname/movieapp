package com.lapin.movieapp.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
    private final SecretKey secretKey;

    public JWTService() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            this.secretKey = keyGen.generateKey(); // returns a random key for the algorithm HS256
            //System.out.println(Base64.getEncoder().encodeToString(secretKey.getEncoded())); // prints a string
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(String username) {

        Map<String, Object> claims = new HashMap<>(); // a claim is a field in the payload part of a jwt token
        long currentTime = System.currentTimeMillis();

        return Jwts.builder()
                .claims()
                .add(claims) //add claims of type HashMap to hold claims for the JWT
                .subject(username)
                .issuedAt(new Date(currentTime))
                .expiration(new Date(currentTime + 60 * 60 * 1000)) //make it valid for 1 hour
                .and() //chain methods together in the builder pattern
                .signWith(secretKey)
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claimResolver.apply(claims);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String extractUserName(String token) {
        // extract the username from the jwt token
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenExpired(String token) {
        Date expDate = extractClaim(token, Claims::getExpiration);
        return expDate.before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}

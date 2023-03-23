package com.mypolls.polls.security;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwtSecret}")
    private Key jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + this.jwtExpirationInMs);

        return (
            Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(expiry)
                .signWith(jwtSecret, SignatureAlgorithm.HS512)
                .compact()
        );
    }

    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(this.jwtSecret)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

        // Claims claims = Jwts.parser()
        //                     .setSigningKey(jwtSecret)
        //                     .parseClaimsJws(token)
        //                     .getBody();

        return(Long.parseLong(claims.getSubject()));
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(this.jwtSecret).build().parseClaimsJws(authToken);
            return(true);
        }
        catch(SecurityException e) {
            logger.error("Invalid JWT security signature.");
        }
        catch(MalformedJwtException e) {
            logger.error("Invalid JWT token.");
        }
        catch(ExpiredJwtException e) {
            logger.error("Expired JWT token.");
        }
        catch(UnsupportedJwtException e) {
            logger.error("Unsupported JWT token.");
        }
        catch(IllegalArgumentException e) {
            logger.error("JWT claims string is empty.");
        }
        
        return(false);
    }
}

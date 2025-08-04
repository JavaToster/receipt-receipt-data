package com.example.receipt_data.security;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;
import com.auth0.jwt.JWT;


@Component
public class JwtUtil {
    @Value("${security.jwt.secret}")
    private String secret;

    public String generateToken(long telegramId){
        Date expirationDate = Date.from(ZonedDateTime.now().plusWeeks(1).toInstant());
        return JWT.create()
                .withSubject("user details")
                .withClaim("telegramId", telegramId)
                .withIssuedAt(new Date())
                .withIssuer("receipt project")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public Long validateTokenAndRetrieveClaim(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("user details")
                .withIssuer("receipt project")
                .build();

        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("telegramId").asLong();
    }
}

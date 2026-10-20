package com.coffeebabe.backend.adapter.security;

import com.coffeebabe.backend.core.entities.User;
import com.coffeebabe.backend.core.gateways.TokenGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class JwtTokenAdapter implements TokenGateway {
    private final JwtEncoder jwtEncoder;
    private final Duration expiration;

    public JwtTokenAdapter(JwtEncoder jwtEncoder, @Value("${security.jwt.token.expirationMinutes}") long expirationMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    @Override
    public String generateToken(User user) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .build();

        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

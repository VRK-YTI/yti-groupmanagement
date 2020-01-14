package fi.vm.yti.groupmanagement.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fi.vm.yti.groupmanagement.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

@Service
public class TokenServiceImpl implements TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

    private final String jwtSecret;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

    public TokenServiceImpl(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public String generateToken(final UUID userId,
                                final Map<String, Object> claims,
                                final Date createdAt,
                                final Date invalidatedAt) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userId.toString())
            .setIssuedAt(createdAt)
            .setExpiration(invalidatedAt)
            .signWith(signatureAlgorithm, jwtSecret).compact();
    }

    public TokenData getTokenData(final String token) {
        final Claims claims = getAllClaimsFromToken(token);
        if (claims != null) {
            final UUID userId;
            try {
                userId = UUID.fromString(claims.getSubject());
            } catch (final IllegalArgumentException e) {
                logger.info("Token userId cannot be resolved");
                return null;
            }
            final Date tokenCreatedAt = claims.getIssuedAt();
            final Date tokenInvalidationAt = claims.getExpiration();
            final String type = claims.get("type").toString();
            return new TokenData(userId, tokenCreatedAt, tokenInvalidationAt, type);
        }
        return null;
    }

    private Claims getAllClaimsFromToken(final String token) {
        try {
            return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        } catch (final SignatureException e) {
            logger.info("Token signature verification failed!");
        } catch (final ExpiredJwtException e) {
            logger.info("Token has expired!");
        } catch (final Exception e) {
            logger.info("Parsing token failed!");
        }
        return null;
    }
}

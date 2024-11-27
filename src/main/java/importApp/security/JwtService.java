package importApp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;

@PropertySource(value = {"classpath:application-local.yml"})
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1時間後に期限切れ
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String extractUserId(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();
        } catch (MalformedJwtException e) {
            throw new IllegalArgumentException("Invalid JWT token format", e);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("JWT token has expired", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JWT token", e);
        }
    }
}

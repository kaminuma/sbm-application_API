package importApp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final String SECRET_KEY = "your_secret_key"; // 環境変数または設定ファイルから取得するのが望ましい
    private final long EXPIRATION_TIME = 86400000; // 1日（ミリ秒）

    // トークン生成メソッド
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    // トークンを作成するためのヘルパーメソッド
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // トークンからユーザー名を取得するメソッド
    public String getUsernameFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    // トークンの有効性を検証するメソッド
    public boolean validateToken(String token) {
        return !extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }
}
